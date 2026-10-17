package pe.cibertec.rudestyle.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "cupon")
public class Cupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cupon")
    private Integer idCupon;

    @Column(name = "codigo", nullable = false, unique = true, length = 30)
    private String codigo;

    @Column(name = "tipo", nullable = false, length = 20)
    private String tipo; // "PORCENTAJE" o "MONTO_FIJO"

    @Column(name = "valor_descuento", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorDescuento;

    @Column(name = "estado", nullable = false)
    private Integer estado = 1; // 1 = activo, 0 = inactivo

    public Cupon() {}

    public Integer getIdCupon() {
        return idCupon;
    }

    public void setIdCupon(Integer idCupon) {
        this.idCupon = idCupon;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public BigDecimal getValorDescuento() {
        return valorDescuento;
    }

    public void setValorDescuento(BigDecimal valorDescuento) {
        this.valorDescuento = valorDescuento;
    }

    public Integer getEstado() {
        return estado;
    }

    public void setEstado(Integer estado) {
        this.estado = estado;
    }
}