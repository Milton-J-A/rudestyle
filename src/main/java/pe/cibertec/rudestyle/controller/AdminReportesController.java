package pe.cibertec.rudestyle.controller;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
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
    public String indexReportes(HttpSession session, Model model) {
        if (!esAdmin(session)) {
            return "redirect:/login";
        }

        // Rango de inicio y fin del día actual
        LocalDate hoy = LocalDate.now();
        LocalDateTime inicioDia = hoy.atStartOfDay();
        LocalDateTime finDia = hoy.atTime(LocalTime.MAX);

        List<Pedido> pedidosHoy = pedidoRepo.findByFechaPedidoBetweenOrderByFechaPedidoDesc(inicioDia, finDia);

        // Cálculos estadísticos
        BigDecimal totalIngresosHoy = pedidosHoy.stream()
                .filter(p -> !"CANCELADO".equalsIgnoreCase(p.getEstadoPago()))
                .map(Pedido::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int totalClientesCompradores = (int) pedidosHoy.stream()
                .map(Pedido::getCorreoCliente)
                .distinct()
                .count();

        BigDecimal ticketPromedio = pedidosHoy.isEmpty() || totalIngresosHoy.compareTo(BigDecimal.ZERO) == 0 
                ? BigDecimal.ZERO 
                : totalIngresosHoy.divide(BigDecimal.valueOf(pedidosHoy.size()), 2, RoundingMode.HALF_UP);

        model.addAttribute("fechaHoy", hoy);
        model.addAttribute("pedidosHoy", pedidosHoy);
        model.addAttribute("totalIngresosHoy", totalIngresosHoy);
        model.addAttribute("totalVentasHoy", pedidosHoy.size());
        model.addAttribute("totalClientesCompradores", totalClientesCompradores);
        model.addAttribute("ticketPromedio", ticketPromedio);

        return "admin/reportes";
    }

    @GetMapping("/descargar-csv")
    public void descargarReporteCSV(HttpServletResponse response, HttpSession session) throws IOException {
        if (!esAdmin(session)) {
            response.sendRedirect("/login");
            return;
        }

        LocalDate hoy = LocalDate.now();
        LocalDateTime inicioDia = hoy.atStartOfDay();
        LocalDateTime finDia = hoy.atTime(LocalTime.MAX);
        List<Pedido> pedidosHoy = pedidoRepo.findByFechaPedidoBetweenOrderByFechaPedidoDesc(inicioDia, finDia);

        // Cabeceras HTTP para descarga de archivo
        response.setContentType("text/csv; charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"reporte_ventas_" + hoy + ".csv\"");

        // Uso exclusivo de OutputStream para evitar choques con getWriter()
        OutputStream out = response.getOutputStream();

        // BOM UTF-8 (EF BB BF) para que Excel reconozca tildes y caracteres especiales
        out.write(0xEF);
        out.write(0xBB);
        out.write(0xBF);

        PrintWriter writer = new PrintWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8));

        // Cabecera CSV
        writer.println("Código Orden;Hora;Cliente;Correo;Teléfono;Método Pago;Monto (S/.);Estado");

        // Filas del reporte
        for (Pedido p : pedidosHoy) {
            String hora = p.getFechaPedido() != null ? p.getFechaPedido().toLocalTime().toString().substring(0, 5) : "--:--";
            String cliente = p.getNombresCliente() != null ? p.getNombresCliente() : "";
            String correo = p.getCorreoCliente() != null ? p.getCorreoCliente() : "";
            String telefono = p.getTelefonoCliente() != null ? p.getTelefonoCliente() : "";
            String metodo = p.getMetodoPago() != null ? p.getMetodoPago() : "";
            String total = p.getTotal() != null ? String.format(Locale.US, "%.2f", p.getTotal()) : "0.00";
            String estado = p.getEstadoPago() != null ? p.getEstadoPago() : "";

            writer.println(String.format("%s;%s;%s;%s;%s;%s;%s;%s",
                    p.getCodigoOrden(),
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