package pe.cibertec.rudestyle.controller;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import pe.cibertec.rudestyle.model.Pedido;
import pe.cibertec.rudestyle.model.Usuario;
import pe.cibertec.rudestyle.repository.PedidoRepository;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Locale;

@Controller
@RequestMapping("/admin/reportes")
public class AdminReportesController {

    @Autowired
    private PedidoRepository pedidoRepo;

    private boolean esAdmin(HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        return usuario != null && usuario.getRol() != null && usuario.getRol().toUpperCase().contains("ADMIN");
    }

    @GetMapping
    public String indexReportes(
            @RequestParam(name = "fechaInicio", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(name = "fechaFin", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            HttpSession session, Model model) {

        if (!esAdmin(session)) {
            return "redirect:/login";
        }

        // Si no se envían fechas, por defecto toma desde el día 1 del mes actual hasta la fecha actual
        LocalDate hoy = LocalDate.now();
        if (fechaInicio == null) {
            fechaInicio = hoy.withDayOfMonth(1);
        }
        if (fechaFin == null) {
            fechaFin = hoy;
        }

        LocalDateTime inicioRango = fechaInicio.atStartOfDay();
        LocalDateTime finRango = fechaFin.atTime(LocalTime.MAX);

        // Consulta de pedidos entre ambas fechas
        List<Pedido> pedidosFiltrados = pedidoRepo.findByFechaPedidoBetweenOrderByFechaPedidoDesc(inicioRango, finRango);

        // Cálculos estadísticos
        BigDecimal totalIngresos = pedidosFiltrados.stream()
                .filter(p -> !"CANCELADO".equalsIgnoreCase(p.getEstadoPago()))
                .map(Pedido::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int totalClientesCompradores = (int) pedidosFiltrados.stream()
                .map(Pedido::getCorreoCliente)
                .distinct()
                .count();

        BigDecimal ticketPromedio = pedidosFiltrados.isEmpty() || totalIngresos.compareTo(BigDecimal.ZERO) == 0 
                ? BigDecimal.ZERO 
                : totalIngresos.divide(BigDecimal.valueOf(pedidosFiltrados.size()), 2, RoundingMode.HALF_UP);

        // Mantenemos los nombres originales para compatibilidad con la vista reportes.html
        model.addAttribute("fechaHoy", hoy);
        model.addAttribute("fechaInicio", fechaInicio);
        model.addAttribute("fechaFin", fechaFin);
        model.addAttribute("pedidosHoy", pedidosFiltrados);
        model.addAttribute("totalIngresosHoy", totalIngresos);
        model.addAttribute("totalVentasHoy", pedidosFiltrados.size());
        model.addAttribute("totalClientesCompradores", totalClientesCompradores);
        model.addAttribute("ticketPromedio", ticketPromedio);

        return "admin/reportes";
    }

    @GetMapping("/descargar-csv")
    public void descargarReporteCSV(
            @RequestParam(name = "fechaInicio", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(name = "fechaFin", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            HttpServletResponse response, HttpSession session) throws IOException {

        if (!esAdmin(session)) {
            response.sendRedirect("/login");
            return;
        }

        LocalDate hoy = LocalDate.now();
        if (fechaInicio == null) {
            fechaInicio = hoy.withDayOfMonth(1);
        }
        if (fechaFin == null) {
            fechaFin = hoy;
        }

        LocalDateTime inicioRango = fechaInicio.atStartOfDay();
        LocalDateTime finRango = fechaFin.atTime(LocalTime.MAX);

        List<Pedido> pedidosFiltrados = pedidoRepo.findByFechaPedidoBetweenOrderByFechaPedidoDesc(inicioRango, finRango);

        // Cabeceras HTTP para descarga de archivo
        response.setContentType("text/csv; charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"reporte_ventas_" + fechaInicio + "_al_" + fechaFin + ".csv\"");

        OutputStream out = response.getOutputStream();

        // BOM UTF-8 (EF BB BF) para que Excel reconozca tildes y caracteres especiales
        out.write(0xEF);
        out.write(0xBB);
        out.write(0xBF);

        PrintWriter writer = new PrintWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8));

        // Cabecera CSV
        writer.println("Código Orden;Fecha;Hora;Cliente;Correo;Teléfono;Método Pago;Monto (S/.);Estado");

        // Filas del reporte
        for (Pedido p : pedidosFiltrados) {
            String fecha = p.getFechaPedido() != null ? p.getFechaPedido().toLocalDate().toString() : "--";
            String hora = p.getFechaPedido() != null ? p.getFechaPedido().toLocalTime().toString().substring(0, 5) : "--:--";
            String cliente = p.getNombresCliente() != null ? p.getNombresCliente() : "";
            String correo = p.getCorreoCliente() != null ? p.getCorreoCliente() : "";
            String telefono = p.getTelefonoCliente() != null ? p.getTelefonoCliente() : "";
            String metodo = p.getMetodoPago() != null ? p.getMetodoPago() : "";
            String total = p.getTotal() != null ? String.format(Locale.US, "%.2f", p.getTotal()) : "0.00";
            String estado = p.getEstadoPago() != null ? p.getEstadoPago() : "";

            writer.println(String.format("%s;%s;%s;%s;%s;%s;%s;%s;%s",
                    p.getCodigoOrden(),
                    fecha,
                    hora,
                    cliente,
                    correo,
                    telefono,
                    metodo,
                    total,
                    estado));
        }

        writer.flush();
        writer.close();
    }
}