package pe.cibertec.rudestyle.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import pe.cibertec.rudestyle.model.DetallePedido;
import pe.cibertec.rudestyle.model.Pedido;
import pe.cibertec.rudestyle.model.Usuario;
import pe.cibertec.rudestyle.repository.DetallePedidoRepository;
import pe.cibertec.rudestyle.repository.PedidoRepository;

import java.util.List;
import java.util.Optional;

@Controller
public class ClientePedidoController {

    @Autowired
    private PedidoRepository pedidoRepo;

    @Autowired
    private DetallePedidoRepository detallePedidoRepo;

    @GetMapping("/mis-pedidos")
    public String misPedidosView(HttpSession session, Model model) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuario == null) {
            return "redirect:/login";
        }

        List<Pedido> misPedidos = pedidoRepo.findByUsuario_IdUsuarioOrderByFechaPedidoDesc(usuario.getIdUsuario());
        model.addAttribute("pedidos", misPedidos);
        return "mis-pedidos";
    }

    @GetMapping("/mis-pedidos/detalle/{id}")
    public String verDetalleMiPedido(@PathVariable("id") Integer idPedido, HttpSession session, Model model) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuario == null) {
            return "redirect:/login";
        }

        Optional<Pedido> pedidoOpt = pedidoRepo.findById(idPedido);
        if (pedidoOpt.isEmpty() || !pedidoOpt.get().getUsuario().getIdUsuario().equals(usuario.getIdUsuario())) {
            return "redirect:/mis-pedidos";
        }

        List<DetallePedido> detalles = detallePedidoRepo.findByPedido_IdPedido(idPedido);
        model.addAttribute("pedido", pedidoOpt.get());
        model.addAttribute("detalles", detalles);
        return "mi-pedido-detalle";
    }
}