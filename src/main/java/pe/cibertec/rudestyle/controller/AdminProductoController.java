package pe.cibertec.rudestyle.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
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

    // 1. Vista principal: Listado y Formulario
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

    // 2. Cargar datos de un producto para edición
    @GetMapping("/editar/{id}")
    public String editarProducto(@PathVariable("id") Integer id, HttpSession session, Model model, RedirectAttributes redirectAttrs) {
        if (!esAdmin(session)) {
            return "redirect:/login";
        }
        Optional<Producto> prodOpt = productoRepo.findById(id);
        if (prodOpt.isEmpty()) {
            redirectAttrs.addFlashAttribute("error", "El producto solicitado no existe.");
            return "redirect:/admin/productos";
        }
        model.addAttribute("productos", productoRepo.findAll());
        model.addAttribute("totalRegistros", productoRepo.count());
        model.addAttribute("producto", prodOpt.get());
        model.addAttribute("categorias", categoriaRepo.findAll());
        model.addAttribute("esEdicion", true);
        return "admin/productos";
    }

    // 3. Guardar o actualizar con preservación de imagen y alertas
    @PostMapping("/guardar")
    public String guardarProducto(@ModelAttribute("producto") Producto producto, 
                                  HttpSession session, 
                                  RedirectAttributes redirectAttrs) {
        if (!esAdmin(session)) {
            return "redirect:/login";
        }

        try {
            // Protección al editar: Si la imagen viene vacía, conservar la imagen previa
            if (producto.getIdProducto() != null) {
                Optional<Producto> prodExistente = productoRepo.findById(producto.getIdProducto());
                if (prodExistente.isPresent()) {
                    if (producto.getImagen() == null || producto.getImagen().trim().isEmpty()) {
                        producto.setImagen(prodExistente.get().getImagen());
                    }
                }
            }

            if (producto.getDestacado() == null) {
                producto.setDestacado(false);
            }

            productoRepo.save(producto);
            redirectAttrs.addFlashAttribute("success", "Producto guardado correctamente en el catálogo.");
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("error", "Ocurrió un error al procesar el producto: " + e.getMessage());
        }

        return "redirect:/admin/productos";
    }

    // 4. Eliminación protegida contra fallos de integridad referencial
    @GetMapping("/eliminar/{id}")
    public String eliminarProducto(@PathVariable("id") Integer id, 
                                   HttpSession session, 
                                   RedirectAttributes redirectAttrs) {
        if (!esAdmin(session)) {
            return "redirect:/login";
        }

        try {
            productoRepo.deleteById(id);
            redirectAttrs.addFlashAttribute("success", "Prenda eliminada con éxito del inventario.");
        } catch (DataIntegrityViolationException ex) {
            // Previene la pantalla blanca de error 500 si el producto ya está en pedidos o reseñas
            redirectAttrs.addFlashAttribute("error", "No se puede eliminar la prenda porque ya cuenta con pedidos o registros asociados.");
        } catch (Exception ex) {
            redirectAttrs.addFlashAttribute("error", "Error inesperado al intentar eliminar: " + ex.getMessage());
        }

        return "redirect:/admin/productos";
    }
}