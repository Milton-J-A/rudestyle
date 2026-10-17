package pe.cibertec.rudestyle.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "pedido")
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pedido")
    private Integer idPedido;

    @Column(name = "codigo_orden", nullable = false, unique = true, length = 40)
    private String codigoOrden;

    @Column(name = "fecha_pedido", nullable = false)
    private LocalDateTime fechaPedido;

    @Column(name = "nombres_cliente", nullable = false, length = 100)
    private String nombresCliente;

    @Column(name = "correo_cliente", nullable = false, length = 100)
    private String correoCliente;

    @Column(name = "telefono_cliente", nullable = false, length = 20)
    private String telefonoCliente;

    @Column(name = "direccion_envio", nullable = false, length = 255)
    private String direccionEnvio;

    @Column(name = "departamento", nullable = false, length = 50)
    private String departamento;

    @Column(name = "metodo_pago", nullable = false, length = 50)
    private String metodoPago;

    @Column(name = "moneda", length = 5)
    private String moneda = "PEN";

    @Column(name = "subtotal", nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "descuento", precision = 10, scale = 2)
    private BigDecimal descuento = BigDecimal.ZERO;

    @Column(name = "costo_envio", nullable = false, precision = 10, scale = 2)
    private BigDecimal costoEnvio = BigDecimal.ZERO;

    @Column(name = "total", nullable = false, precision = 10, scale = 2)
    private BigDecimal total;

    @Column(name = "estado_pago", nullable = false, length = 30)
    private String estadoPago = "COMPLETADO";

    // --- NUEVOS CAMPOS PARA PAGO MIXTO / DELIVERY ---
    @Column(name = "monto_adelanto", precision = 10, scale = 2)
    private BigDecimal montoAdelanto = BigDecimal.ZERO;

    @Column(name = "saldo_contraentrega", precision = 10, scale = 2)
    private BigDecimal saldoContraentrega = BigDecimal.ZERO;

    @Column(name = "estado_envio_pago", length = 30)
    private String estadoEnvioPago = "NO_APLICA";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario")
    private Usuario usuario;

    public Pedido() {
        this.fechaPedido = LocalDateTime.now();
    }

    public Integer getIdPedido() {
        return idPedido;
    }

    public void setIdPedido(Integer idPedido) {
        this.idPedido = idPedido;
    }

    public String getCodigoOrden() {
        return codigoOrden;
    }

    public void setCodigoOrden(String codigoOrden) {
        this.codigoOrden = codigoOrden;
    }

    public LocalDateTime getFechaPedido() {
        return fechaPedido;
    }

    public void setFechaPedido(LocalDateTime fechaPedido) {
        this.fechaPedido = fechaPedido;
    }

    public String getNombresCliente() {
        return nombresCliente;
    }

    public void setNombresCliente(String nombresCliente) {
        this.nombresCliente = nombresCliente;
    }

    public String getCorreoCliente() {
        return correoCliente;
    }

    public void setCorreoCliente(String correoCliente) {
        this.correoCliente = correoCliente;
    }

    public String getTelefonoCliente() {
        return telefonoCliente;
    }

    public void setTelefonoCliente(String telefonoCliente) {
        this.telefonoCliente = telefonoCliente;
    }

    public String getDireccionEnvio() {
        return direccionEnvio;
    }

    public void setDireccionEnvio(String direccionEnvio) {
        this.direccionEnvio = direccionEnvio;
    }

    public String getDepartamento() {
        return departamento;
    }

    public void setDepartamento(String departamento) {
        this.departamento = departamento;
    }

    public String getMetodoPago() {
        return metodoPago;
    }

    public void setMetodoPago(String metodoPago) {
        this.metodoPago = metodoPago;
    }

    public String getMoneda() {
        return moneda;
    }

    public void setMoneda(String moneda) {
        this.moneda = moneda;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public BigDecimal getDescuento() {
        return descuento;
    }

    public void setDescuento(BigDecimal descuento) {
        this.descuento = descuento;
    }

    public BigDecimal getCostoEnvio() {
        return costoEnvio;
    }

    public void setCostoEnvio(BigDecimal costoEnvio) {
        this.costoEnvio = costoEnvio;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public String getEstadoPago() {
        return estadoPago;
    }

    public void setEstadoPago(String estadoPago) {
        this.estadoPago = estadoPago;
    }

    public BigDecimal getMontoAdelanto() {
        return montoAdelanto;
    }

    public void setMontoAdelanto(BigDecimal montoAdelanto) {
        this.montoAdelanto = montoAdelanto;
    }

    public BigDecimal getSaldoContraentrega() {
        return saldoContraentrega;
    }

    public void setSaldoContraentrega(BigDecimal saldoContraentrega) {
        this.saldoContraentrega = saldoContraentrega;
    }

    public String getEstadoEnvioPago() {
        return estadoEnvioPago;
    }

    public void setEstadoEnvioPago(String estadoEnvioPago) {
        this.estadoEnvioPago = estadoEnvioPago;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }
}