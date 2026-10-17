package pe.cibertec.rudestyle.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import pe.cibertec.rudestyle.model.ItemCarrito;
import pe.cibertec.rudestyle.model.Producto;
import pe.cibertec.rudestyle.repository.ProductoRepository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/carrito")
public class CarritoController {

    @Autowired
    private ProductoRepository productoRepo;

    @SuppressWarnings("unchecked")
    private List<ItemCarrito> obtenerCarrito(HttpSession session) {
        List<ItemCarrito> carrito = (List<ItemCarrito>) session.getAttribute("carrito");
        if (carrito == null) {
            carrito = new ArrayList<>();
            session.setAttribute("carrito", carrito);
        }
        return carrito;
    }

    @GetMapping
    public String verCarrito(HttpSession session, Model model) {
        List<ItemCarrito> carrito = obtenerCarrito(session);

        BigDecimal total = carrito.stream()
                .map(ItemCarrito::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int totalPrendas = carrito.stream()
                .mapToInt(ItemCarrito::getCantidad)
                .sum();

        model.addAttribute("items", carrito);
        model.addAttribute("total", total);
        model.addAttribute("totalPrendas", totalPrendas);
        return "carrito";
    }

    // Endpoint POST: recibe el formulario con talla y cantidad desde producto-detalle.html
    @PostMapping("/agregar")
    public String agregarDesdeDetalle(
            @RequestParam("idProducto") Integer idProducto,
            @RequestParam(name = "cantidad", defaultValue = "1") Integer cantidad,
            @RequestParam(name = "talla", defaultValue = "Estándar") String talla,
            HttpSession session) {

        List<ItemCarrito> carrito = obtenerCarrito(session);
        Optional<Producto> productoOpt = productoRepo.findById(idProducto);

        if (productoOpt.isPresent()) {
            Producto prod = productoOpt.get();
            boolean existe = false;

            // Busca si ya existe la misma prenda CON LA MISMA TALLA
            for (ItemCarrito item : carrito) {
                if (item.getProducto().getIdProducto().equals(idProducto) && 
                    item.getTalla() != null && item.getTalla().equalsIgnoreCase(talla)) {
                    
                    int nuevaCantidad = item.getCantidad() + cantidad;
                    if (nuevaCantidad <= prod.getStock()) {
                        item.setCantidad(nuevaCantidad);
                    } else {
                        item.setCantidad(prod.getStock());
                    }
                    existe = true;
                    break;
                }
            }

            if (!existe) {
                int cantFinal = Math.min(cantidad, prod.getStock());
                carrito.add(new ItemCarrito(prod, cantFinal, talla));
            }
        }
        return "redirect:/carrito";
    }

    // Endpoint GET: soporte para adición rápida de 1 unidad con talla estándar
    @GetMapping("/agregar/{id}")
    public String agregarProducto(@PathVariable("id") Integer idProducto, HttpSession session) {
        List<ItemCarrito> carrito = obtenerCarrito(session);
        Optional<Producto> productoOpt = productoRepo.findById(idProducto);

        if (productoOpt.isPresent()) {
            Producto prod = productoOpt.get();
            boolean existe = false;

            for (ItemCarrito item : carrito) {
                if (item.getProducto().getIdProducto().equals(idProducto)) {
                    if (item.getCantidad() < prod.getStock()) {
                        item.setCantidad(item.getCantidad() + 1);
                    }
                    existe = true;
                    break;
                }
            }

            if (!existe) {
                carrito.add(new ItemCarrito(prod, 1, "Estándar"));
            }
        }
        return "redirect:/carrito";
    }

    @GetMapping("/disminuir/{id}")
    public String disminuirProducto(@PathVariable("id") Integer idProducto, HttpSession session) {
        List<ItemCarrito> carrito = obtenerCarrito(session);

        for (int i = 0; i < carrito.size(); i++) {
            ItemCarrito item = carrito.get(i);
            if (item.getProducto().getIdProducto().equals(idProducto)) {
                if (item.getCantidad() > 1) {
                    item.setCantidad(item.getCantidad() - 1);
                } else {
                    carrito.remove(i);
                }
                break;
            }
        }
        return "redirect:/carrito";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminarProducto(@PathVariable("id") Integer idProducto, HttpSession session) {
        List<ItemCarrito> carrito = obtenerCarrito(session);
        carrito.removeIf(item -> item.getProducto().getIdProducto().equals(idProducto));
        return "redirect:/carrito";
    }

    @GetMapping("/vaciar")
    public String vaciarCarrito(HttpSession session) {
        session.removeAttribute("carrito");
        return "redirect:/carrito";
    }
}