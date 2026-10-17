package pe.cibertec.rudestyle.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pe.cibertec.rudestyle.model.Cupon;
import pe.cibertec.rudestyle.model.ItemCarrito;
import pe.cibertec.rudestyle.model.Pedido;
import pe.cibertec.rudestyle.model.Usuario;
import pe.cibertec.rudestyle.repository.CuponRepository;
import pe.cibertec.rudestyle.repository.PedidoRepository;
import pe.cibertec.rudestyle.service.PedidoService;
import pe.cibertec.rudestyle.service.StockInsuficienteException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/checkout")
public class CheckoutController {

    @Autowired
    private PedidoRepository pedidoRepo;

    @Autowired
    private PedidoService pedidoService;

    @Autowired
    private CuponRepository cuponRepo;

    @GetMapping
    public String verCheckout(HttpSession session, Model model) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuario == null) {
            return "redirect:/login";
        }

        @SuppressWarnings("unchecked")
        List<ItemCarrito> carrito = (List<ItemCarrito>) session.getAttribute("carrito");
        if (carrito == null || carrito.isEmpty()) {
            return "redirect:/carrito";
        }

        BigDecimal subtotal = carrito.stream()
                .map(ItemCarrito::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Cupón aplicado en sesión
        String codigoCupon = (String) session.getAttribute("cuponAplicado");
        BigDecimal descuento = (BigDecimal) session.getAttribute("descuentoMonto");

        if (descuento == null) {
            descuento = BigDecimal.ZERO;
        }
        descuento = descuento.min(subtotal);

        boolean envioGratis = subtotal.compareTo(PedidoService.MONTO_MINIMO_ENVIO_GRATIS) >= 0;
        BigDecimal costoEnvio = envioGratis ? BigDecimal.ZERO : PedidoService.COSTO_DELIVERY_ESTANDAR;
        BigDecimal total = subtotal.subtract(descuento).add(costoEnvio).max(BigDecimal.ZERO);

        model.addAttribute("usuario", usuario);
        model.addAttribute("carrito", carrito);
        model.addAttribute("subtotal", subtotal);
        model.addAttribute("codigoCupon", codigoCupon);
        model.addAttribute("descuento", descuento);
        model.addAttribute("costoEnvio", costoEnvio);
        model.addAttribute("envioGratis", envioGratis);
        model.addAttribute("total", total);

        return "checkout";
    }

    // =========================================================================
    // NUEVO: PROCESAR EL INGRESO MANUAL DEL CUPÓN CON VALIDACIÓN DE ERROR
    // =========================================================================
    @PostMapping("/aplicar-cupon")
    public String aplicarCupon(@RequestParam("codigoCupon") String codigoCupon,
                               HttpSession session,
                               RedirectAttributes flash) {

        // 1. Validar que no llegue vacío
        if (codigoCupon == null || codigoCupon.trim().isEmpty()) {
            flash.addFlashAttribute("errorCupon", "Por favor, ingresa un código de descuento.");
            return "redirect:/checkout";
        }

        String codigoLimpio = codigoCupon.trim().toUpperCase();

        // 2. Buscar si el cupón existe en la base de datos
        Optional<Cupon> cuponOpt = cuponRepo.findByCodigo(codigoLimpio);

        if (cuponOpt.isEmpty()) {
            // Error si el usuario escribe mal el código
            session.removeAttribute("cuponAplicado");
            session.removeAttribute("descuentoMonto");
            flash.addFlashAttribute("errorCupon", "El cupón \"" + codigoLimpio + "\" no existe o no es válido.");
            return "redirect:/checkout";
        }

        Cupon cupon = cuponOpt.get();

        // 3. Validar estado (1 = Activo)
        if (cupon.getEstado() == null || cupon.getEstado() != 1) {
            session.removeAttribute("cuponAplicado");
            session.removeAttribute("descuentoMonto");
            flash.addFlashAttribute("errorCupon", "El cupón \"" + codigoLimpio + "\" ya no se encuentra vigente.");
            return "redirect:/checkout";
        }

        // 4. Calcular el monto de descuento según el subtotal del carrito
        @SuppressWarnings("unchecked")
        List<ItemCarrito> carrito = (List<ItemCarrito>) session.getAttribute("carrito");
        if (carrito == null || carrito.isEmpty()) {
            return "redirect:/carrito";
        }

        BigDecimal subtotal = carrito.stream()
                .map(ItemCarrito::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal montoDescuento;
        if ("PORCENTAJE".equalsIgnoreCase(cupon.getTipo())) {
            BigDecimal porcentaje = cupon.getValorDescuento().divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP);
            montoDescuento = subtotal.multiply(porcentaje).setScale(2, RoundingMode.HALF_UP);
        } else {
            // MONTO_FIJO
            montoDescuento = cupon.getValorDescuento().min(subtotal);
        }

        // 5. Guardar en sesión para que persista en el flujo
        session.setAttribute("cuponAplicado", cupon.getCodigo());
        session.setAttribute("descuentoMonto", montoDescuento);

        flash.addFlashAttribute("exitoCupon", "¡Cupón " + cupon.getCodigo() + " aplicado con éxito! Descuento: S/ " + montoDescuento);
        return "redirect:/checkout";
    }

    // =========================================================================
    // NUEVO: QUITAR EL CUPÓN SI EL CLIENTE DECIDE NO USARLO
    // =========================================================================
    @GetMapping("/quitar-cupon")
    public String quitarCupon(HttpSession session, RedirectAttributes flash) {
        session.removeAttribute("cuponAplicado");
        session.removeAttribute("descuentoMonto");
        flash.addFlashAttribute("infoCupon", "El cupón ha sido removido.");
        return "redirect:/checkout";
    }

    @PostMapping("/procesar")
    public String procesarOrden(
            @RequestParam(value = "nombresCliente", required = false) String nombresCliente,
            @RequestParam(value = "nombres", required = false) String nombres,
            @RequestParam(value = "correoCliente", required = false) String correoCliente,
            @RequestParam(value = "correo", required = false) String correo,
            @RequestParam(value = "telefonoCliente", required = false) String telefonoCliente,
            @RequestParam(value = "telefono", required = false) String telefono,
            @RequestParam(value = "direccionEnvio", required = false) String direccionEnvio,
            @RequestParam(value = "direccion", required = false) String direccion,
            @RequestParam(value = "departamento", defaultValue = "Lima") String departamento,
            @RequestParam(value = "metodoAdelanto", required = false, defaultValue = "YAPE") String metodoAdelanto,
            @RequestParam(value = "nroOperacion", required = false, defaultValue = "CONTRAENTREGA") String nroOperacion,
            HttpSession session,
            RedirectAttributes flash) {

        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuario == null) {
            return "redirect:/login";
        }

        @SuppressWarnings("unchecked")
        List<ItemCarrito> carrito = (List<ItemCarrito>) session.getAttribute("carrito");
        if (carrito == null || carrito.isEmpty()) {
            return "redirect:/carrito";
        }

        String finalNombres = (nombresCliente != null && !nombresCliente.isBlank()) ? nombresCliente : (nombres != null ? nombres : usuario.getNombre());
        String finalCorreo = (correoCliente != null && !correoCliente.isBlank()) ? correoCliente : (correo != null ? correo : usuario.getCorreo());
        String finalTelefono = (telefonoCliente != null && !telefonoCliente.isBlank()) ? telefonoCliente : (telefono != null ? telefono : "999888777");
        String finalDireccion = (direccionEnvio != null && !direccionEnvio.isBlank()) ? direccionEnvio : (direccion != null ? direccion : "Dirección estándar");

        String codigoCupon = (String) session.getAttribute("cuponAplicado");

        try {
            Pedido pedido = pedidoService.registrarPedido(usuario, carrito, codigoCupon,
                    finalNombres, finalCorreo, finalTelefono, finalDireccion,
                    departamento, metodoAdelanto);

            session.removeAttribute("carrito");
            session.removeAttribute("cuponAplicado");
            session.removeAttribute("descuentoMonto");

            return "redirect:/checkout/exito?codigo=" + pedido.getCodigoOrden();

        } catch (StockInsuficienteException e) {
            flash.addFlashAttribute("errorStock", e.getMessage());
            return "redirect:/carrito";
        }
    }

    @GetMapping("/exito")
    public String ordenExitosa(@RequestParam("codigo") String codigo, Model model) {
        pedidoRepo.findByCodigoOrden(codigo).ifPresent(p -> model.addAttribute("pedido", p));
        return "checkout-exito";
    }
}