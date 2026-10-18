package pe.cibertec.rudestyle.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "Producto")
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_producto")
    private Integer idProducto;

    @Column(name = "nom_producto", nullable = false, length = 150)
    private String nomProducto;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "precio", nullable = false, precision = 10, scale = 2)
    private BigDecimal precio;

    @Column(name = "precio_original", precision = 10, scale = 2)
    private BigDecimal precioOriginal;

    @Column(name = "descuento_porcentaje")
    private Integer descuentoPorcentaje;

    @Column(name = "stock", nullable = false)
    private Integer stock;

    @Column(name = "imagen", length = 255)
    private String imagen;

    @Column(name = "tallas_disponibles", length = 50)
    private String tallasDisponibles;

    @Column(name = "colores_disponibles", length = 100)
    private String coloresDisponibles;

    @Column(name = "destacado")
    private Boolean destacado;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_categoria")
    private Categoria categoria;

    public Producto() {
    }

    public Producto(Integer idProducto, String nomProducto, String descripcion, BigDecimal precio,
                    BigDecimal precioOriginal, Integer descuentoPorcentaje, Integer stock, String imagen,
                    String tallasDisponibles, String coloresDisponibles, Boolean destacado, Categoria categoria) {
        this.idProducto = idProducto;
        this.nomProducto = nomProducto;
        this.descripcion = descripcion;
        this.precio = precio;
        this.precioOriginal = precioOriginal;
        this.descuentoPorcentaje = descuentoPorcentaje;
        this.stock = stock;
        this.imagen = imagen;
        this.tallasDisponibles = tallasDisponibles;
        this.coloresDisponibles = coloresDisponibles;
        this.destacado = destacado;
        this.categoria = categoria;
    }

    public Integer getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(Integer idProducto) {
        this.idProducto = idProducto;
    }

    public String getNomProducto() {
        return nomProducto;
    }

    public void setNomProducto(String nomProducto) {
        this.nomProducto = nomProducto;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }

    public BigDecimal getPrecioOriginal() {
        return precioOriginal;
    }

    public void setPrecioOriginal(BigDecimal precioOriginal) {
        this.precioOriginal = precioOriginal;
    }

    public Integer getDescuentoPorcentaje() {
        return descuentoPorcentaje;
    }

    public void setDescuentoPorcentaje(Integer descuentoPorcentaje) {
        this.descuentoPorcentaje = descuentoPorcentaje;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public String getImagen() {
        return imagen;
    }

    public void setImagen(String imagen) {
        this.imagen = imagen;
    }

    public String getTallasDisponibles() {
        return tallasDisponibles;
    }

    public void setTallasDisponibles(String tallasDisponibles) {
        this.tallasDisponibles = tallasDisponibles;
    }

    public String getColoresDisponibles() {
        return coloresDisponibles;
    }

    public void setColoresDisponibles(String coloresDisponibles) {
        this.coloresDisponibles = coloresDisponibles;
    }

    public Boolean getDestacado() {
        return destacado;
    }

    public void setDestacado(Boolean destacado) {
        this.destacado = destacado;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }
    @Override
    public String toString() {
        return this.nomProducto;
    }
}