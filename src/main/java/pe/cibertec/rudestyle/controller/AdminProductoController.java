package pe.cibertec.rudestyle.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import pe.cibertec.rudestyle.model.Producto;
import pe.cibertec.rudestyle.model.Usuario;
import pe.cibertec.rudestyle.repository.CategoriaRepository;
import pe.cibertec.rudestyle.repository.ProductoRepository;

import java.util.Optional;

@Controller
@RequestMapping("/admin/productos")
public class AdminProductoController {

    @Autowired
    private ProductoRepository productoRepo;

    @Autowired
    private CategoriaRepository categoriaRepo;

    private boolean esAdmin(HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuario == null || usuario.getRol() == null) {
            return false;
        }
        return usuario.getRol().toUpperCase().contains("ADMIN");
    }

    // 1. Vista principal: Formulario a la izquierda y Tabla a la derecha
    @GetMapping
    public String indexAdmin(HttpSession session, Model model) {
        if (!esAdmin(session)) {
            return "redirect:/login";
        }
        model.addAttribute("productos", productoRepo.findAll());
        model.addAttribute("totalRegistros", productoRepo.count());
        model.addAttribute("producto", new Producto());
        model.addAttribute("categorias", categoriaRepo.findAll());
        model.addAttribute("esEdicion", false);
        return "admin/productos";
    }

    // 2. Cargar datos de un producto en el formulario para editar
    @GetMapping("/editar/{id}")
    public String editarProducto(@PathVariable("id") Integer id, HttpSession session, Model model) {
        if (!esAdmin(session)) {
            return "redirect:/login";
        }
        Optional<Producto> prodOpt = productoRepo.findById(id);
        if (prodOpt.isEmpty()) {
            return "redirect:/admin/productos";
        }
        model.addAttribute("productos", productoRepo.findAll());
        model.addAttribute("totalRegistros", productoRepo.count());
        model.addAttribute("producto", prodOpt.get());
        model.addAttribute("categorias", categoriaRepo.findAll());
        model.addAttribute("esEdicion", true);
        return "admin/productos";
    }

    // 3. Guardar o actualizar
    @PostMapping("/guardar")
    public String guardarProducto(@ModelAttribute("producto") Producto producto, HttpSession session) {
        if (!esAdmin(session)) {
            return "redirect:/login";
        }
        if (producto.getDestacado() == null) {
            producto.setDestacado(false);
        }
        productoRepo.save(producto);
        return "redirect:/admin/productos";
    }

    // 4. Eliminar producto
    @GetMapping("/eliminar/{id}")
    public String eliminarProducto(@PathVariable("id") Integer id, HttpSession session) {
        if (!esAdmin(session)) {
            return "redirect:/login";
        }
        productoRepo.deleteById(id);
        return "redirect:/admin/productos";
    }
}