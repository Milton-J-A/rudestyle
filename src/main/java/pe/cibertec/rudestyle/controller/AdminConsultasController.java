package pe.cibertec.rudestyle.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import pe.cibertec.rudestyle.model.Pedido;
import pe.cibertec.rudestyle.model.Usuario;
import pe.cibertec.rudestyle.repository.PedidoRepository;

import java.util.Collections;
import java.util.List;

@Controller
@RequestMapping("/admin/consultas")
public class AdminConsultasController {

    @Autowired
    private PedidoRepository pedidoRepo;

    private boolean esAdmin(HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        return usuario != null && usuario.getRol() != null && usuario.getRol().toUpperCase().contains("ADMIN");
    }

    @GetMapping
    public String indexConsultas(@RequestParam(name = "busqueda", required = false) String busqueda,
                                 HttpSession session,
                                 Model model) {
        if (!esAdmin(session)) {
            return "redirect:/login";
        }

        List<Pedido> resultados;
        if (busqueda != null && !busqueda.trim().isEmpty()) {
            resultados = pedidoRepo.buscarPorFiltro(busqueda.trim());
        } else {
            resultados = Collections.emptyList();
        }

        model.addAttribute("busqueda", busqueda != null ? busqueda : "");
        model.addAttribute("pedidos", resultados);
        return "admin/consultas";
    }
}