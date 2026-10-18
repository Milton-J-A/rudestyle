
package pe.cibertec.rudestyle.model;

import jakarta.persistence.*;

@Entity
@Table(name = "especificaciones_producto")
public class EspecificacionProducto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_especificacion")
    private Long idEspecificacion;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_producto", nullable = false, unique = true)
    private Producto producto;

    @Column(name = "material", length = 150)
    private String material;

    @Column(name = "composicion", length = 255)
    private String composicion;

    @Column(name = "descripcion_comercial", columnDefinition = "TEXT")
    private String descripcionComercial;

    @Column(name = "tipo_corte", length = 100)
    private String tipoCorte;

    @Column(name = "caracteristicas_tela", columnDefinition = "TEXT")
    private String caracteristicasTela;

    @Column(name = "acabado", length = 255)
    private String acabado;

    @Column(name = "lavado", length = 255)
    private String lavado;

    @Column(name = "secado", length = 255)
    private String secado;

    @Column(name = "planchado", length = 255)
    private String planchado;

    @Column(name = "conservacion", length = 255)
    private String conservacion;

    // ===== INICIO DE LA MODIFICACIÓN HECHA POR MARCO =====
    // Constructor vacío requerido por JPA
    public EspecificacionProducto() {
    }
    // ===== FIN DE LA MODIFICACIÓN HECHA POR MARCO =====

    public Long getIdEspecificacion() {
        return idEspecificacion;
    }

    public void setIdEspecificacion(Long idEspecificacion) {
        this.idEspecificacion = idEspecificacion;
    }

    public Producto getProducto() {
        return producto;
    }

    public void setProducto(Producto producto) {
        this.producto = producto;
    }

    public String getMaterial() {
        return material;
    }

    public void setMaterial(String material) {
        this.material = material;
    }

    public String getComposicion() {
        return composicion;
    }

    public void setComposicion(String composicion) {
        this.composicion = composicion;
    }

    public String getDescripcionComercial() {
        return descripcionComercial;
    }

    public void setDescripcionComercial(String descripcionComercial) {
        this.descripcionComercial = descripcionComercial;
    }

    public String getTipoCorte() {
        return tipoCorte;
    }

    public void setTipoCorte(String tipoCorte) {
        this.tipoCorte = tipoCorte;
    }

    public String getCaracteristicasTela() {
        return caracteristicasTela;
    }

    public void setCaracteristicasTela(String caracteristicasTela) {
        this.caracteristicasTela = caracteristicasTela;
    }

    public String getAcabado() {
        return acabado;
    }

    public void setAcabado(String acabado) {
        this.acabado = acabado;
    }

    public String getLavado() {
        return lavado;
    }

    public void setLavado(String lavado) {
        this.lavado = lavado;
    }

    public String getSecado() {
        return secado;
    }

    public void setSecado(String secado) {
        this.secado = secado;
    }

    public String getPlanchado() {
        return planchado;
    }

    public void setPlanchado(String planchado) {
        this.planchado = planchado;
    }

    public String getConservacion() {
        return conservacion;
    }

    public void setConservacion(String conservacion) {
        this.conservacion = conservacion;
    }
}
