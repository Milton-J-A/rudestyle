
package pe.cibertec.rudestyle.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import pe.cibertec.rudestyle.model.ItemCarrito;
import pe.cibertec.rudestyle.model.Producto;
import pe.cibertec.rudestyle.repository.ProductoRepository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/carrito")
public class CarritoController {

    @Autowired
    private ProductoRepository productoRepo;

    @SuppressWarnings("unchecked")
    private List<ItemCarrito> obtenerCarrito(HttpSession session) {
        List<ItemCarrito> carrito =
                (List<ItemCarrito>) session.getAttribute("carrito");

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

    // ===== INICIO DE LA MODIFICACIÓN HECHA POR MARCO =====
    // Agrega desde el detalle sin redirigir a la página del carrito.
    @PostMapping("/agregar-ajax")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> agregarDesdeDetalleAjax(
            @RequestParam("idProducto") Integer idProducto,
            @RequestParam(name = "cantidad", defaultValue = "1") Integer cantidad,
            @RequestParam(name = "talla", defaultValue = "Estándar") String talla,
            HttpSession session) {

        Map<String, Object> respuesta = new HashMap<>();

        if (cantidad == null || cantidad < 1) {
            respuesta.put("ok", false);
            respuesta.put("mensaje", "Selecciona una cantidad válida.");
            return ResponseEntity.badRequest().body(respuesta);
        }

        List<ItemCarrito> carrito = obtenerCarrito(session);
        Optional<Producto> productoOpt = productoRepo.findById(idProducto);

        if (productoOpt.isEmpty()) {
            respuesta.put("ok", false);
            respuesta.put("mensaje", "No se encontró el producto.");
            return ResponseEntity.badRequest().body(respuesta);
        }

        Producto prod = productoOpt.get();
        int stock = prod.getStock() != null ? prod.getStock() : 0;

        if (stock <= 0) {
            respuesta.put("ok", false);
            respuesta.put("mensaje", "Este producto está agotado.");
            return ResponseEntity.badRequest().body(respuesta);
        }

        boolean existe = false;

        for (ItemCarrito item : carrito) {
            if (item.getProducto().getIdProducto().equals(idProducto)
                    && item.getTalla() != null
                    && item.getTalla().equalsIgnoreCase(talla)) {

                int nuevaCantidad = item.getCantidad() + cantidad;

                if (nuevaCantidad > stock) {
                    respuesta.put("ok", false);
                    respuesta.put("mensaje",
                            "Solo hay " + stock + " unidades disponibles de este producto.");
                    return ResponseEntity.badRequest().body(respuesta);
                }

                item.setCantidad(nuevaCantidad);
                existe = true;
                break;
            }
        }

        if (!existe) {
            if (cantidad > stock) {
                respuesta.put("ok", false);
                respuesta.put("mensaje",
                        "Solo hay " + stock + " unidades disponibles de este producto.");
                return ResponseEntity.badRequest().body(respuesta);
            }

            carrito.add(new ItemCarrito(prod, cantidad, talla));
        }

        int totalPrendas = carrito.stream()
                .mapToInt(ItemCarrito::getCantidad)
                .sum();

        respuesta.put("ok", true);
        respuesta.put("mensaje", "¡Producto añadido a tu bolsa!");
        respuesta.put("totalPrendas", totalPrendas);

        return ResponseEntity.ok(respuesta);
    }
    // ===== FIN DE LA MODIFICACIÓN HECHA POR MARCO =====

    // Ruta original: conserva la redirección para los formularios existentes.
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
            int stock = prod.getStock() != null ? prod.getStock() : 0;

            if (cantidad != null && cantidad > 0 && stock > 0 && cantidad <= stock) {
                boolean existe = false;

                for (ItemCarrito item : carrito) {
                    if (item.getProducto().getIdProducto().equals(idProducto)
                            && item.getTalla() != null
                            && item.getTalla().equalsIgnoreCase(talla)) {

                        int nuevaCantidad = item.getCantidad() + cantidad;

                        if (nuevaCantidad <= stock) {
                            item.setCantidad(nuevaCantidad);
                        }

                        existe = true;
                        break;
                    }
                }

                if (!existe) {
                    carrito.add(new ItemCarrito(prod, cantidad, talla));
                }
            }
        }

        return "redirect:/carrito";
    }

    @GetMapping("/agregar/{id}")
    public String agregarProducto(
            @PathVariable("id") Integer idProducto,
            HttpSession session) {

        List<ItemCarrito> carrito = obtenerCarrito(session);
        Optional<Producto> productoOpt = productoRepo.findById(idProducto);

        if (productoOpt.isPresent()) {
            Producto prod = productoOpt.get();
            int stock = prod.getStock() != null ? prod.getStock() : 0;
            boolean existe = false;

            for (ItemCarrito item : carrito) {
                if (item.getProducto().getIdProducto().equals(idProducto)) {
                    if (item.getCantidad() < stock) {
                        item.setCantidad(item.getCantidad() + 1);
                    }
                    existe = true;
                    break;
                }
            }

            if (!existe && stock > 0) {
                carrito.add(new ItemCarrito(prod, 1, "Estándar"));
            }
        }

        return "redirect:/carrito";
    }

    @GetMapping("/disminuir/{id}")
    public String disminuirProducto(
            @PathVariable("id") Integer idProducto,
            HttpSession session) {

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
    public String eliminarProducto(
            @PathVariable("id") Integer idProducto,
            HttpSession session) {

        List<ItemCarrito> carrito = obtenerCarrito(session);
        carrito.removeIf(
                item -> item.getProducto().getIdProducto().equals(idProducto));

        return "redirect:/carrito";
    }

    @GetMapping("/vaciar")
    public String vaciarCarrito(HttpSession session) {
        session.removeAttribute("carrito");
        return "redirect:/carrito";
    }
}
