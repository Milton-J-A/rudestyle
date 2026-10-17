package pe.cibertec.rudestyle.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.cibertec.rudestyle.model.Cupon;
import pe.cibertec.rudestyle.model.ItemCarrito;
import pe.cibertec.rudestyle.repository.CuponRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@RestController
@RequestMapping("/api/cupon")
public class CuponRestController {

    @Autowired
    private CuponRepository cuponRepo;

    @PostMapping("/aplicar")
    public ResponseEntity<Map<String, Object>> aplicarCupon(@RequestParam("codigo") String codigo, HttpSession session) {
        Map<String, Object> resp = new HashMap<>();

        Optional<Cupon> cuponOpt = cuponRepo.findByCodigoAndEstado(codigo.trim().toUpperCase(), 1);
        if (cuponOpt.isEmpty()) {
            resp.put("success", false);
            resp.put("mensaje", "Cupón no válido o inactivo.");
            return ResponseEntity.badRequest().body(resp);
        }

        Cupon cupon = cuponOpt.get();

        @SuppressWarnings("unchecked")
        List<ItemCarrito> carrito = (List<ItemCarrito>) session.getAttribute("carrito");
        if (carrito == null || carrito.isEmpty()) {
            resp.put("success", false);
            resp.put("mensaje", "El carrito está vacío.");
            return ResponseEntity.badRequest().body(resp);
        }

        BigDecimal subtotal = carrito.stream()
                .map(ItemCarrito::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

     // Calcular descuento según el tipo (PORCENTAJE o MONTO_FIJO)
        BigDecimal descuento = BigDecimal.ZERO;
        if ("PORCENTAJE".equalsIgnoreCase(cupon.getTipo())) {
            descuento = subtotal.multiply(cupon.getValorDescuento().divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP));
        } else {
            descuento = cupon.getValorDescuento();
        }

        // Guardar en sesión
        session.setAttribute("cuponAplicado", cupon.getCodigo());
        session.setAttribute("descuentoMonto", descuento);

        resp.put("success", true);
        resp.put("codigo", cupon.getCodigo());
        resp.put("descuento", descuento);
        resp.put("mensaje", "¡Cupón " + cupon.getCodigo() + " aplicado con éxito!");
        return ResponseEntity.ok(resp);
    }
}