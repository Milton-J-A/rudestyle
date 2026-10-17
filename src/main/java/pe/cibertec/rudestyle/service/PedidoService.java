package pe.cibertec.rudestyle.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.cibertec.rudestyle.model.*;
import pe.cibertec.rudestyle.repository.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class PedidoService {

    public static final BigDecimal MONTO_MINIMO_ENVIO_GRATIS = new BigDecimal("150.00");
    public static final BigDecimal COSTO_DELIVERY_ESTANDAR = new BigDecimal("15.00");

    @Autowired private PedidoRepository pedidoRepo;
    @Autowired private DetallePedidoRepository detallePedidoRepo;
    @Autowired private ProductoRepository productoRepo;
    @Autowired private CuponRepository cuponRepo;

    @Transactional(rollbackFor = Exception.class)
    public Pedido registrarPedido(Usuario usuario, List<ItemCarrito> carrito, String codigoCupon,
                                  String nombres, String correo, String telefono,
                                  String direccion, String departamento,
                                  String metodoAdelanto) throws StockInsuficienteException {

        BigDecimal subtotal = carrito.stream()
                .map(ItemCarrito::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Cupón validado en servidor (no se confía en el monto guardado en sesión)
        BigDecimal descuento = calcularDescuento(codigoCupon, subtotal);

        boolean envioGratis = subtotal.compareTo(MONTO_MINIMO_ENVIO_GRATIS) >= 0;
        BigDecimal costoEnvio = envioGratis ? BigDecimal.ZERO : COSTO_DELIVERY_ESTANDAR;
        BigDecimal total = subtotal.subtract(descuento).add(costoEnvio).max(BigDecimal.ZERO);

        // 1. Cabecera
        Pedido pedido = new Pedido();
        pedido.setCodigoOrden("RUDE-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        pedido.setFechaPedido(LocalDateTime.now());
        pedido.setNombresCliente(nombres);
        pedido.setCorreoCliente(correo);
        pedido.setTelefonoCliente(telefono);
        pedido.setDireccionEnvio(direccion);
        pedido.setDepartamento(departamento);
        pedido.setMoneda("PEN");
        pedido.setSubtotal(subtotal);
        pedido.setDescuento(descuento);
        pedido.setCostoEnvio(costoEnvio);
        pedido.setTotal(total);
        pedido.setUsuario(usuario);

        if (envioGratis) {
            pedido.setMetodoPago("CONTRAENTREGA");
            pedido.setMontoAdelanto(BigDecimal.ZERO);
            pedido.setSaldoContraentrega(total);
            pedido.setEstadoEnvioPago("NO_APLICA");
            pedido.setEstadoPago("PENDIENTE");
        } else {
            pedido.setMetodoPago("DELIVERY_" + metodoAdelanto.toUpperCase() + "_CONTRAENTREGA");
            pedido.setMontoAdelanto(costoEnvio);
            pedido.setSaldoContraentrega(subtotal.subtract(descuento).max(BigDecimal.ZERO));
            pedido.setEstadoEnvioPago("PAGADO");
            pedido.setEstadoPago("ENVIO_PAGADO_SALDO_PENDIENTE");
        }
        pedidoRepo.save(pedido);

        // 2. Detalle + 3. Descuento atómico de stock (si falla algo, rollback total)
        for (ItemCarrito item : carrito) {
            int filas = productoRepo.descontarStock(item.getProducto().getIdProducto(), item.getCantidad());
            if (filas == 0) {
                throw new StockInsuficienteException(item.getProducto().getNomProducto());
            }

            DetallePedido detalle = new DetallePedido();
            detalle.setPedido(pedido);
            detalle.setProducto(item.getProducto());
            detalle.setCantidad(item.getCantidad());
            detalle.setPrecioUnitario(item.getProducto().getPrecio());
            detalle.setSubtotal(item.getSubtotal());
            detallePedidoRepo.save(detalle);
        }

        return pedido;
    }

    private BigDecimal calcularDescuento(String codigoCupon, BigDecimal subtotal) {
        if (codigoCupon == null || codigoCupon.isBlank()) {
            return BigDecimal.ZERO;
        }
        return cuponRepo.findByCodigoAndEstado(codigoCupon.trim().toUpperCase(), 1)
                .map(c -> {
                    BigDecimal d = "PORCENTAJE".equalsIgnoreCase(c.getTipo())
                            ? subtotal.multiply(c.getValorDescuento())
                                      .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP)
                            : c.getValorDescuento();
                    return d.min(subtotal);
                })
                .orElse(BigDecimal.ZERO);
    }
}