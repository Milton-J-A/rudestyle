package pe.cibertec.rudestyle.controller;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import pe.cibertec.rudestyle.model.CalificacionProducto;
import pe.cibertec.rudestyle.model.DetallePedido;
import pe.cibertec.rudestyle.model.EtiquetaMaestro;
import pe.cibertec.rudestyle.model.Pedido;
import pe.cibertec.rudestyle.model.Usuario;
import pe.cibertec.rudestyle.repository.CalificacionProductoRepository;
import pe.cibertec.rudestyle.repository.DetallePedidoRepository;
import pe.cibertec.rudestyle.repository.EtiquetaMaestroRepository;
import pe.cibertec.rudestyle.repository.PedidoRepository;
import pe.cibertec.rudestyle.model.SolicitudPostventa;
import pe.cibertec.rudestyle.repository.SolicitudPostventaRepository;

@Controller
public class ClientePedidoController {

    @Autowired
    private PedidoRepository pedidoRepo;

    @Autowired
    private DetallePedidoRepository detallePedidoRepo;

    // ===== MODIFICACIÓN DE CALIFICACIONES =====
    @Autowired
    private CalificacionProductoRepository calificacionRepo;

    @Autowired
    private EtiquetaMaestroRepository etiquetaRepo;
    
    @Autowired
    private SolicitudPostventaRepository solicitudPostventaRepo;
    
    @GetMapping("/mis-reclamos")
    public String misReclamosView(HttpSession session, Model model) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");

        if (usuario == null) {
            return "redirect:/login";
        }

        // Buscamos todas las solicitudes asociadas al correo del usuario en sesión
        List<SolicitudPostventa> misSolicitudes = 
                solicitudPostventaRepo.findByCorreoClienteOrderByFechaRegistroDesc(usuario.getCorreo());

        model.addAttribute("solicitudes", misSolicitudes);

        return "mis-reclamos";
    }

    @GetMapping("/mis-pedidos")
    public String misPedidosView(HttpSession session, Model model) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");

        if (usuario == null) {
            return "redirect:/login";
        }

        List<Pedido> misPedidos =
                pedidoRepo.findByUsuario_IdUsuarioOrderByFechaPedidoDesc(usuario.getIdUsuario());

        model.addAttribute("pedidos", misPedidos);

        return "mis-pedidos";
    }

    @GetMapping("/mis-pedidos/detalle/{id}")
    public String verDetalleMiPedido(
            @PathVariable("id") Integer idPedido,
            HttpSession session,
            Model model) {

        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");

        if (usuario == null) {
            return "redirect:/login";
        }

        Optional<Pedido> pedidoOpt = pedidoRepo.findById(idPedido);

        if (pedidoOpt.isEmpty()
                || !pedidoOpt.get().getUsuario().getIdUsuario().equals(usuario.getIdUsuario())) {
            return "redirect:/mis-pedidos";
        }

        List<DetallePedido> detalles =
                detallePedidoRepo.findByPedido_IdPedido(idPedido);

        model.addAttribute("pedido", pedidoOpt.get());
        model.addAttribute("detalles", detalles);

        // Cargar catálogo de etiquetas
        List<EtiquetaMaestro> etiquetas = etiquetaRepo.findAll();
        model.addAttribute("etiquetasCalificacion", etiquetas);

        // Identificar detalles ya calificados
        List<Integer> idsDetalle = new ArrayList<>();
        for (DetallePedido detalle : detalles) {
            if (detalle.getIdDetalle() != null) {
                idsDetalle.add(detalle.getIdDetalle());
            }
        }

        Set<Integer> detallesCalificados = new HashSet<>();
        if (!idsDetalle.isEmpty()) {
            List<CalificacionProducto> calificacionesExistentes =
                    calificacionRepo.findByIdDetalleIn(idsDetalle);

            for (CalificacionProducto calificacion : calificacionesExistentes) {
                if (calificacion.getIdDetalle() != null) {
                    detallesCalificados.add(calificacion.getIdDetalle());
                }
            }
        }

        model.addAttribute("detallesCalificados", detallesCalificados);

        return "mi-pedido-detalle";
    }

    // =========================================================================
    // GENERACIÓN DE COMPROBANTE DE PAGO EN PDF (BOLETA ELECTRÓNICA)
    // REGLA DE NEGOCIO: EXCLUSIVO PARA PEDIDOS EN ESTADO "ENTREGADO"
    // =========================================================================
    @GetMapping("/mis-pedidos/comprobante-pdf/{id}")
    public void descargarComprobantePDF(
            @PathVariable("id") Integer idPedido,
            HttpSession session,
            HttpServletResponse response) throws IOException {

        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuario == null) {
            response.sendRedirect("/login");
            return;
        }

        Optional<Pedido> pedidoOpt = pedidoRepo.findById(idPedido);
        if (pedidoOpt.isEmpty()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Pedido no encontrado.");
            return;
        }

        Pedido pedido = pedidoOpt.get();

        // Control de autorización: debe ser el dueño del pedido o un Administrador
        boolean esDuenio = pedido.getUsuario() != null && pedido.getUsuario().getIdUsuario().equals(usuario.getIdUsuario());
        boolean esAdmin = usuario.getRol() != null && usuario.getRol().toUpperCase().contains("ADMIN");

        if (!esDuenio && !esAdmin) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "No tienes permiso para ver este comprobante.");
            return;
        }

        // Regla contable estricta: NO emitir comprobante si no figura ENTREGADO
        if (!"ENTREGADO".equalsIgnoreCase(pedido.getEstadoPago())) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "El comprobante solo está disponible para pedidos entregados.");
            return;
        }

        List<DetallePedido> detalles = detallePedidoRepo.findByPedido_IdPedido(idPedido);

        // Cabeceras HTTP para renderizar el PDF en el navegador
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "inline; filename=\"Boleta_" + pedido.getCodigoOrden() + ".pdf\"");

        Document document = new Document(PageSize.A4, 36, 36, 40, 40);

        try {
            OutputStream out = response.getOutputStream();
            PdfWriter.getInstance(document, out);
            document.open();

            // Tipografías
            Font fontEmpresa = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
            Font fontRuc = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11);
            Font fontBold = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9);
            Font fontNormal = FontFactory.getFont(FontFactory.HELVETICA, 9);
            Font fontSmall = FontFactory.getFont(FontFactory.HELVETICA, 8);

            // 1. Cabecera con datos de la empresa y recuadro RUC
            PdfPTable headerTable = new PdfPTable(2);
            headerTable.setWidthPercentage(100);
            headerTable.setWidths(new float[]{60f, 40f});

            PdfPCell cellLeft = new PdfPCell();
            cellLeft.setBorder(PdfPCell.NO_BORDER);
            cellLeft.addElement(new Paragraph("RUDESTYLE S.A.C.", fontEmpresa));
            cellLeft.addElement(new Paragraph("Moda Urbana & Calzado Exclusivo", fontSmall));
            cellLeft.addElement(new Paragraph("Av. Javier Prado Este 4200, Lima - Perú", fontSmall));
            cellLeft.addElement(new Paragraph("Teléfono: +51 999 888 777 | ventas@rudestyle.pe", fontSmall));
            headerTable.addCell(cellLeft);

            PdfPCell cellRight = new PdfPCell();
            cellRight.setPadding(8);
            cellRight.setHorizontalAlignment(Element.ALIGN_CENTER);
            Paragraph pRuc = new Paragraph("R.U.C. 20608974512\nBOLETA DE VENTA ELECTRÓNICA\nN° " + pedido.getCodigoOrden(), fontRuc);
            pRuc.setAlignment(Element.ALIGN_CENTER);
            cellRight.addElement(pRuc);
            headerTable.addCell(cellRight);

            document.add(headerTable);
            document.add(new Paragraph(" "));

            // 2. Información del cliente y la transacción
            PdfPTable infoTable = new PdfPTable(2);
            infoTable.setWidthPercentage(100);
            infoTable.setWidths(new float[]{50f, 50f});

            String fechaStr = pedido.getFechaPedido() != null 
                    ? pedido.getFechaPedido().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                    : "--";

            PdfPCell cellCliente = new PdfPCell();
            cellCliente.setPadding(6);
            cellCliente.addElement(new Paragraph("CLIENTE: " + pedido.getNombresCliente(), fontBold));
            cellCliente.addElement(new Paragraph("CORREO: " + pedido.getCorreoCliente(), fontNormal));
            cellCliente.addElement(new Paragraph("TELÉFONO: " + pedido.getTelefonoCliente(), fontNormal));
            infoTable.addCell(cellCliente);

            PdfPCell cellOperacion = new PdfPCell();
            cellOperacion.setPadding(6);
            cellOperacion.addElement(new Paragraph("FECHA EMISIÓN: " + fechaStr, fontNormal));
            cellOperacion.addElement(new Paragraph("MÉTODO DE PAGO: " + pedido.getMetodoPago(), fontNormal));
            cellOperacion.addElement(new Paragraph("ESTADO: " + pedido.getEstadoPago(), fontBold));
            infoTable.addCell(cellOperacion);

            document.add(infoTable);
            document.add(new Paragraph(" "));

            // 3. Tabla con el desglose de prendas compradas
            PdfPTable itemsTable = new PdfPTable(4);
            itemsTable.setWidthPercentage(100);
            itemsTable.setWidths(new float[]{50f, 15f, 15f, 20f});

            String[] titulos = {"DESCRIPCIÓN DE LA PRENDA", "CANTIDAD", "P. UNIT.", "SUBTOTAL"};
            for (String t : titulos) {
                PdfPCell th = new PdfPCell(new Phrase(t, fontBold));
                th.setHorizontalAlignment(Element.ALIGN_CENTER);
                th.setPadding(5);
                itemsTable.addCell(th);
            }

            for (DetallePedido item : detalles) {
                String nombrePrenda = (item.getProducto() != null && item.getProducto().getNomProducto() != null)
                        ? item.getProducto().getNomProducto()
                        : "Prenda RudeStyle";

                PdfPCell cNombre = new PdfPCell(new Phrase(nombrePrenda, fontNormal));
                cNombre.setPadding(5);
                itemsTable.addCell(cNombre);

                PdfPCell cCant = new PdfPCell(new Phrase(String.valueOf(item.getCantidad()), fontNormal));
                cCant.setHorizontalAlignment(Element.ALIGN_CENTER);
                cCant.setPadding(5);
                itemsTable.addCell(cCant);

                PdfPCell cPrecio = new PdfPCell(new Phrase("S/ " + String.format(Locale.US, "%.2f", item.getPrecioUnitario()), fontNormal));
                cPrecio.setHorizontalAlignment(Element.ALIGN_RIGHT);
                cPrecio.setPadding(5);
                itemsTable.addCell(cPrecio);

                PdfPCell cSubtotal = new PdfPCell(new Phrase("S/ " + String.format(Locale.US, "%.2f", item.getSubtotal()), fontNormal));
                cSubtotal.setHorizontalAlignment(Element.ALIGN_RIGHT);
                cSubtotal.setPadding(5);
                itemsTable.addCell(cSubtotal);
            }

            document.add(itemsTable);

            // 4. Liquidación e impuestos desglosados (Subtotal, Descuento, Delivery, IGV y Total)
            BigDecimal subtotalPrendas = pedido.getSubtotal() != null ? pedido.getSubtotal() : BigDecimal.ZERO;
            BigDecimal descuento = pedido.getDescuento() != null ? pedido.getDescuento() : BigDecimal.ZERO;
            BigDecimal costoEnvio = pedido.getCostoEnvio() != null ? pedido.getCostoEnvio() : BigDecimal.ZERO;
            BigDecimal totalFinal = pedido.getTotal() != null ? pedido.getTotal() : BigDecimal.ZERO;

            BigDecimal baseImponible = totalFinal.divide(new BigDecimal("1.18"), 2, RoundingMode.HALF_UP);
            BigDecimal igv = totalFinal.subtract(baseImponible);

            PdfPTable totalesTable = new PdfPTable(2);
            totalesTable.setWidthPercentage(45);
            totalesTable.setHorizontalAlignment(Element.ALIGN_RIGHT);
            totalesTable.setWidths(new float[]{55f, 45f});

            // Subtotal prendas
            totalesTable.addCell(new Phrase("SUBTOTAL PRENDAS:", fontNormal));
            PdfPCell cSub = new PdfPCell(new Phrase("S/ " + String.format(Locale.US, "%.2f", subtotalPrendas), fontNormal));
            cSub.setHorizontalAlignment(Element.ALIGN_RIGHT);
            totalesTable.addCell(cSub);

            // Descuento / Cupón (si aplica)
            if (descuento.compareTo(BigDecimal.ZERO) > 0) {
                totalesTable.addCell(new Phrase("DESCUENTO / CUPÓN:", fontNormal));
                PdfPCell cDesc = new PdfPCell(new Phrase("- S/ " + String.format(Locale.US, "%.2f", descuento), fontNormal));
                cDesc.setHorizontalAlignment(Element.ALIGN_RIGHT);
                totalesTable.addCell(cDesc);
            }

            // Envíos / Delivery (si aplica)
            if (costoEnvio.compareTo(BigDecimal.ZERO) > 0) {
                totalesTable.addCell(new Phrase("ENVÍO / DELIVERY:", fontNormal));
                PdfPCell cEnvio = new PdfPCell(new Phrase("S/ " + String.format(Locale.US, "%.2f", costoEnvio), fontNormal));
                cEnvio.setHorizontalAlignment(Element.ALIGN_RIGHT);
                totalesTable.addCell(cEnvio);
            }

            // Operación Gravada
            totalesTable.addCell(new Phrase("OP. GRAVADA:", fontNormal));
            PdfPCell cOp = new PdfPCell(new Phrase("S/ " + String.format(Locale.US, "%.2f", baseImponible), fontNormal));
            cOp.setHorizontalAlignment(Element.ALIGN_RIGHT);
            totalesTable.addCell(cOp);

            // IGV (18%)
            totalesTable.addCell(new Phrase("I.G.V. (18%):", fontNormal));
            PdfPCell cIgv = new PdfPCell(new Phrase("S/ " + String.format(Locale.US, "%.2f", igv), fontNormal));
            cIgv.setHorizontalAlignment(Element.ALIGN_RIGHT);
            totalesTable.addCell(cIgv);

            // Total Cancelado
            totalesTable.addCell(new Phrase("TOTAL CANCELADO:", fontBold));
            PdfPCell cTotal = new PdfPCell(new Phrase("S/ " + String.format(Locale.US, "%.2f", totalFinal), fontBold));
            cTotal.setHorizontalAlignment(Element.ALIGN_RIGHT);
            totalesTable.addCell(cTotal);

            document.add(new Paragraph(" "));
            document.add(totalesTable);

            // 5. Pie de página legal
            Paragraph pie = new Paragraph("\n\nRepresentación impresa de la Boleta de Venta Electrónica.\nConsulte su validez en www.rudestyle.pe\n¡Gracias por vestir con actitud!", fontSmall);
            pie.setAlignment(Element.ALIGN_CENTER);
            document.add(pie);

            document.close();
        } catch (DocumentException e) {
            throw new IOException(e.getMessage());
        }
    }

    // ===== ACCIÓN AJAX DE CALIFICACIÓN =====
    @PostMapping("/mis-pedidos/detalle/{idPedido}/calificar")
    @ResponseBody
    @Transactional
    public ResponseEntity<Map<String, Object>> calificarProducto(
            @PathVariable("idPedido") Integer idPedido,
            @RequestParam("idDetalle") Integer idDetalle,
            @RequestParam("puntuacion") Integer puntuacion,
            @RequestParam(value = "etiquetas", required = false) List<Long> etiquetasSeleccionadas,
            HttpSession session) {

        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");

        if (usuario == null) {
            return respuesta(HttpStatus.UNAUTHORIZED, false, "Tu sesión ha terminado. Inicia sesión nuevamente.");
        }

        if (puntuacion == null || puntuacion < 1 || puntuacion > 5) {
            return respuesta(HttpStatus.BAD_REQUEST, false, "Selecciona una puntuación de 1 a 5 estrellas.");
        }

        Optional<Pedido> pedidoOpt = pedidoRepo.findById(idPedido);

        if (pedidoOpt.isEmpty() || !pedidoOpt.get().getUsuario().getIdUsuario().equals(usuario.getIdUsuario())) {
            return respuesta(HttpStatus.FORBIDDEN, false, "No tienes permiso para calificar este pedido.");
        }

        Optional<DetallePedido> detalleOpt = detallePedidoRepo.findById(idDetalle);

        if (detalleOpt.isEmpty() || detalleOpt.get().getPedido() == null
                || !idPedido.equals(detalleOpt.get().getPedido().getIdPedido())) {
            return respuesta(HttpStatus.BAD_REQUEST, false, "La prenda no pertenece a este pedido.");
        }

        DetallePedido detalle = detalleOpt.get();

        if (calificacionRepo.findByIdDetalle(idDetalle).isPresent()) {
            return respuesta(HttpStatus.CONFLICT, false, "Esta prenda ya fue calificada.");
        }

        Set<Long> idsEtiquetas = new LinkedHashSet<>();
        if (etiquetasSeleccionadas != null) {
            for (Long idEtiqueta : etiquetasSeleccionadas) {
                if (idEtiqueta == null || idEtiqueta <= 0) {
                    return respuesta(HttpStatus.BAD_REQUEST, false, "Una de las etiquetas seleccionadas no es válida.");
                }
                idsEtiquetas.add(idEtiqueta);
            }
        }

        List<EtiquetaMaestro> etiquetasValidas = etiquetaRepo.findAllById(idsEtiquetas);
        if (etiquetasValidas.size() != idsEtiquetas.size()) {
            return respuesta(HttpStatus.BAD_REQUEST, false, "Una o más etiquetas seleccionadas no existen.");
        }

        CalificacionProducto calificacion = new CalificacionProducto();
        calificacion.setProducto(detalle.getProducto());
        calificacion.setIdDetalle(detalle.getIdDetalle());
        calificacion.setPuntuacion(puntuacion);
        calificacion.setFechaCalificacion(LocalDateTime.now());

        CalificacionProducto guardada = calificacionRepo.saveAndFlush(calificacion);

        for (Long idEtiqueta : idsEtiquetas) {
            calificacionRepo.guardarEtiqueta(guardada.getIdCalificacion(), idEtiqueta);
        }

        Map<String, Object> resultado = new HashMap<>();
        resultado.put("success", true);
        resultado.put("mensaje", "¡Gracias por calificar tu prenda!");
        resultado.put("idDetalle", idDetalle);

        return ResponseEntity.ok(resultado);
    }

    private ResponseEntity<Map<String, Object>> respuesta(HttpStatus estado, boolean success, String mensaje) {
        Map<String, Object> resultado = new HashMap<>();
        resultado.put("success", success);
        resultado.put("mensaje", mensaje);
        return ResponseEntity.status(estado).body(resultado);
    }
}