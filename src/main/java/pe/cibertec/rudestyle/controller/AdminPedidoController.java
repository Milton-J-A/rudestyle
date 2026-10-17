package pe.cibertec.rudestyle.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import pe.cibertec.rudestyle.model.DetallePedido;
import pe.cibertec.rudestyle.model.Pedido;
import pe.cibertec.rudestyle.model.Usuario;
import pe.cibertec.rudestyle.repository.DetallePedidoRepository;
import pe.cibertec.rudestyle.repository.PedidoRepository;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin/pedidos")
public class AdminPedidoController {

    @Autowired
    private PedidoRepository pedidoRepo;

    @Autowired
    private DetallePedidoRepository detallePedidoRepo;

    // Validación de seguridad para verificar si es ADMIN
    private boolean esAdmin(HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuario == null || usuario.getRol() == null) {
            return false;
        }
        String rol = usuario.getRol().toUpperCase();
        return rol.contains("ADMIN");
    }

    // 1. Listar todas las órdenes de compra
    @GetMapping
    public String listarPedidos(HttpSession session, Model model) {
        if (!esAdmin(session)) {
            return "redirect:/login";
        }
        model.addAttribute("pedidos", pedidoRepo.findAllByOrderByFechaPedidoDesc());
        return "admin/pedidos";
    }

    // 2. Ver el detalle de una orden específica
    @GetMapping("/detalle/{id}")
    public String verDetallePedido(@PathVariable("id") Integer idPedido, HttpSession session, Model model) {
        if (!esAdmin(session)) {
            return "redirect:/login";
        }

        Optional<Pedido> pedidoOpt = pedidoRepo.findById(idPedido);
        if (pedidoOpt.isEmpty()) {
            return "redirect:/admin/pedidos";
        }

        List<DetallePedido> detalles = detallePedidoRepo.findByPedido_IdPedido(idPedido);
        model.addAttribute("pedido", pedidoOpt.get());
        model.addAttribute("detalles", detalles);
        return "admin/detalle-pedido";
    }

    // 3. Modificar el estado del pedido (EN PROCESO, ENTREGADO, CANCELADO)
    @PostMapping("/actualizar-estado")
    public String actualizarEstado(@RequestParam("idPedido") Integer idPedido,
                                   @RequestParam("nuevoEstado") String nuevoEstado,
                                   HttpSession session) {
        if (!esAdmin(session)) {
            return "redirect:/login";
        }

        Optional<Pedido> pedidoOpt = pedidoRepo.findById(idPedido);
        if (pedidoOpt.isPresent()) {
            Pedido pedido = pedidoOpt.get();
            pedido.setEstadoPago(nuevoEstado);
            pedidoRepo.save(pedido);
        }
        return "redirect:/admin/pedidos";
    }
}