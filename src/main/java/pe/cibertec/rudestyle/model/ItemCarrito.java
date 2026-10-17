package pe.cibertec.rudestyle.model;

import java.math.BigDecimal;

public class ItemCarrito {

    private Producto producto;
    private Integer cantidad;
    private String talla; // <-- Atributo para la talla elegida

    public ItemCarrito() {
    }

    // Constructor previo para mantener compatibilidad
    public ItemCarrito(Producto producto, Integer cantidad) {
        this.producto = producto;
        this.cantidad = cantidad;
        this.talla = "Estándar";
    }

    // Nuevo constructor con talla
    public ItemCarrito(Producto producto, Integer cantidad, String talla) {
        this.producto = producto;
        this.cantidad = cantidad;
        this.talla = talla;
    }

    public Producto getProducto() {
        return producto;
    }

    public void setProducto(Producto producto) {
        this.producto = producto;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }

    public String getTalla() {
        return talla;
    }

    public void setTalla(String talla) {
        this.talla = talla;
    }

    // Calcula el subtotal multiplicando el precio unitario por la cantidad
    public BigDecimal getSubtotal() {
        if (producto != null && producto.getPrecio() != null && cantidad != null) {
            return producto.getPrecio().multiply(BigDecimal.valueOf(cantidad));
        }
        return BigDecimal.ZERO;
    }
}