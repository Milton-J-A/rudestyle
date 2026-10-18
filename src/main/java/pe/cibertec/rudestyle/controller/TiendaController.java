
package pe.cibertec.rudestyle.controller;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import pe.cibertec.rudestyle.model.Producto;
import pe.cibertec.rudestyle.model.SolicitudPostventa;
import pe.cibertec.rudestyle.repository.CategoriaRepository;
import pe.cibertec.rudestyle.repository.ProductoRepository;
import pe.cibertec.rudestyle.repository.SolicitudPostventaRepository;
import pe.cibertec.rudestyle.repository.EspecificacionProductoRepository;
import pe.cibertec.rudestyle.repository.CalificacionProductoRepository;

@Controller
public class TiendaController {

    @Autowired
    private ProductoRepository productoRepo;

    @Autowired
    private CategoriaRepository categoriaRepo;

    @Autowired
    private SolicitudPostventaRepository solicitudRepo;

    // ===== INICIO DE LA MODIFICACIÓN HECHA POR MARCO =====
    // Consultar los cuidados de cada producto.
    @Autowired
    private EspecificacionProductoRepository especificacionRepo;

    // Consultar las calificaciones, el promedio y las etiquetas.
    @Autowired
    private CalificacionProductoRepository calificacionRepo;
    // ===== FIN DE LA MODIFICACIÓN HECHA POR MARCO =====

    @GetMapping({"/", "/index"})
    public String index(Model model) {
        model.addAttribute("categorias", categoriaRepo.findAll());

        // Filtrar productos destacados con stock disponible y tomar los primeros 4.
        var destacados = productoRepo.findByDestacadoTrue()
                .stream()
                .filter(p -> p.getStock() != null && p.getStock() > 0)
                .limit(4)
                .toList();

        model.addAttribute("destacados", destacados);
        return "index";
    }

    // =========================================================================
    // CATÁLOGO CON FILTRO COMBINADO: CATEGORÍA, TEXTO, RANGO DE PRECIOS Y ORDEN
    // =========================================================================
    @GetMapping("/catalogo")
    public String catalogo(
            @RequestParam(name = "categoria", required = false) Integer idCategoria,
            @RequestParam(name = "q", required = false) String busqueda,
            @RequestParam(name = "precioMin", required = false) BigDecimal precioMin,
            @RequestParam(name = "precioMax", required = false) BigDecimal precioMax,
            @RequestParam(name = "orden", required = false, defaultValue = "relevancia") String orden,
            Model model) {

        Integer catIdLimpio = (idCategoria != null && idCategoria > 0)
                ? idCategoria : null;

        String textoLimpio = (busqueda != null && !busqueda.trim().isBlank())
                ? busqueda.trim() : null;

        List<Producto> productos = productoRepo.filtrarProductos(
                catIdLimpio, textoLimpio, precioMin, precioMax);

        if ("precio_asc".equalsIgnoreCase(orden)) {
            productos.sort(Comparator.comparing(Producto::getPrecio));
        } else if ("precio_desc".equalsIgnoreCase(orden)) {
            productos.sort(Comparator.comparing(Producto::getPrecio).reversed());
        }

        model.addAttribute("categorias", categoriaRepo.findAll());
        model.addAttribute("productos", productos);
        model.addAttribute("totalResultados", productos.size());

        model.addAttribute("categoriaSeleccionada", catIdLimpio);
        model.addAttribute("busquedaActual", textoLimpio);
        model.addAttribute("precioMinActual", precioMin);
        model.addAttribute("precioMaxActual", precioMax);
        model.addAttribute("ordenActual", orden);

        return "catalogo";
    }

    // ===== INICIO DE LA MODIFICACIÓN HECHA POR MARCO =====
    // Detalle del producto con calificaciones y etiquetas reales.
    @GetMapping("/producto/{id}")
    public String verDetalleProducto(@PathVariable("id") Integer id, Model model) {

        Optional<Producto> productoOpt = productoRepo.findById(id);

        if (productoOpt.isEmpty()) {
            return "redirect:/catalogo";
        }

        Producto producto = productoOpt.get();
        model.addAttribute("producto", producto);

        // Consultar los cuidados asociados al producto.
        especificacionRepo.findByProducto_IdProducto(id)
                .ifPresent(especificacion ->
                        model.addAttribute("especificacion", especificacion));

        // Cargar las tallas disponibles.
        if (producto.getTallasDisponibles() != null
                && !producto.getTallasDisponibles().isBlank()) {

            String[] tallasArray = producto.getTallasDisponibles().split(",");

            List<String> listaTallas = Arrays.stream(tallasArray)
                    .map(String::trim)
                    .toList();

            model.addAttribute("tallas", listaTallas);

        } else {
            model.addAttribute("tallas", List.of("Estándar"));
        }

        // Obtener la cantidad real de calificaciones del producto.
        long totalCalificaciones =
                calificacionRepo.countByProducto_IdProducto(id);

        model.addAttribute("totalCalificaciones", totalCalificaciones);

        // Obtener el promedio real. Si no hay calificaciones, AVG devuelve null.
        Double promedioCalificacion =
                calificacionRepo.calcularPromedioPorProducto(id);

        model.addAttribute("promedioCalificacion", promedioCalificacion);

        // Obtener las etiquetas y sus cantidades.
        List<Object[]> filasEtiquetas =
                calificacionRepo.obtenerEtiquetasPorProducto(id);

        List<Map<String, Object>> etiquetasCalificacion = filasEtiquetas.stream()
                .map(fila -> {
                    Map<String, Object> etiqueta = new HashMap<>();
                    etiqueta.put("nombreEtiqueta", fila[0]);
                    etiqueta.put("cantidad", fila[1]);
                    return etiqueta;
                })
                .toList();

        model.addAttribute("etiquetasCalificacion", etiquetasCalificacion);

        return "producto-detalle";
    }
    // ===== FIN DE LA MODIFICACIÓN HECHA POR MARCO =====

    @GetMapping("/nosotros")
    public String nosotros() {
        return "nosotros";
    }

    @GetMapping("/contacto")
    public String contacto() {
        return "contacto";
    }

    // Procesa el formulario de contacto, cambios de prenda y devoluciones.
    @PostMapping("/contacto/enviar")
    public String procesarSolicitudContacto(
            @RequestParam("nombre") String nombre,
            @RequestParam("correo") String correo,
            @RequestParam("telefono") String telefono,
            @RequestParam("motivo") String motivo,
            @RequestParam(value = "codigoOrdenCambio", required = false) String codigoOrdenCambio,
            @RequestParam(value = "tallaNueva", required = false) String tallaNueva,
            @RequestParam(value = "codigoOrdenDev", required = false) String codigoOrdenDev,
            @RequestParam(value = "medioReembolso", required = false) String medioReembolso,
            @RequestParam(value = "motivoDevolucion", required = false) String motivoDevolucion,
            @RequestParam(value = "fotoPrendaCambio", required = false) MultipartFile fotoPrendaCambio,
            @RequestParam(value = "fotoComprobante", required = false) MultipartFile fotoComprobante,
            @RequestParam("mensaje") String mensaje,
            RedirectAttributes flash) {

        String ticket = "TCK-" + UUID.randomUUID().toString()
                .substring(0, 6).toUpperCase();

        SolicitudPostventa sol = new SolicitudPostventa();
        sol.setCodigoTicket(ticket);
        sol.setFechaRegistro(LocalDateTime.now());
        sol.setNombreCliente(nombre);
        sol.setCorreoCliente(correo);
        sol.setTelefonoCliente(telefono);
        sol.setMotivo(motivo);
        sol.setMensaje(mensaje);
        sol.setEstado("PENDIENTE");

        String ordenAsociada = "";
        String detalleAdicional = "";

        if ("CAMBIO_TALLA".equals(motivo)) {
            ordenAsociada = (codigoOrdenCambio != null) ? codigoOrdenCambio : "";
            detalleAdicional = "Talla solicitada: "
                    + ((tallaNueva != null) ? tallaNueva : "No especificada");

        } else if ("DEVOLUCION".equals(motivo)) {
            ordenAsociada = (codigoOrdenDev != null) ? codigoOrdenDev : "";
            detalleAdicional = "Medio Reembolso: "
                    + ((medioReembolso != null) ? medioReembolso : "");
            sol.setMotivoDevolucion(motivoDevolucion);
        }

        sol.setCodigoOrden(ordenAsociada);
        sol.setDetalleGestion(detalleAdicional);

        MultipartFile archivoFoto = ("CAMBIO_TALLA".equals(motivo))
                ? fotoPrendaCambio : fotoComprobante;

        if (archivoFoto != null && !archivoFoto.isEmpty()) {
            try {
                byte[] bytes = archivoFoto.getBytes();
                String base64 = java.util.Base64.getEncoder().encodeToString(bytes);
                String mimeType = (archivoFoto.getContentType() != null)
                        ? archivoFoto.getContentType() : "image/jpeg";

                sol.setFotoEvidencia("data:" + mimeType + ";base64," + base64);

            } catch (Exception e) {
                sol.setFotoEvidencia(null);
            }
        }

        solicitudRepo.save(sol);

        StringBuilder textoWa = new StringBuilder();
        textoWa.append("Hola RudeStyle, acabo de registrar una solicitud con el Ticket: ")
                .append(ticket).append("\n");
        textoWa.append("• Cliente: ").append(nombre).append("\n");
        textoWa.append("• Teléfono: ").append(telefono).append("\n");
        textoWa.append("• Motivo: ").append(motivo).append("\n");

        if (!ordenAsociada.isBlank()) {
            textoWa.append("• N° Orden: ").append(ordenAsociada).append("\n");
        }

        if (!detalleAdicional.isBlank()) {
            textoWa.append("• Detalle: ").append(detalleAdicional).append("\n");
        }

        textoWa.append("• Mensaje: ").append(mensaje);

        String enlaceWhatsApp = "https://wa.me/51999888777?text="
                + URLEncoder.encode(textoWa.toString(), StandardCharsets.UTF_8);

        String mensajeExito;

        if ("CAMBIO_TALLA".equals(motivo)) {
            mensajeExito = "Tu solicitud de cambio de talla ha sido registrada y guardada en el sistema. "
                    + "Puedes agilizar la coordinación de la entrega de inmediato vía WhatsApp.";

        } else if ("DEVOLUCION".equals(motivo)) {
            mensajeExito = "Tu solicitud de devolución y reembolso ha sido almacenada bajo garantía. "
                    + "Puedes continuar con la validación de tu boleta por WhatsApp.";

        } else {
            mensajeExito = "Tu consulta ha sido registrada en el sistema. "
                    + "Nos pondremos en contacto o puedes escribirnos al WhatsApp.";
        }

        flash.addFlashAttribute("mensajeExito", mensajeExito);
        flash.addFlashAttribute("ticketGenerado", ticket);
        flash.addFlashAttribute("enlaceWhatsApp", enlaceWhatsApp);

        return "redirect:/contacto";
    }
}
