package pe.cibertec.rudestyle.model;

import jakarta.persistence.*;

@Entity
@Table(name = "Categoria")
public class Categoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_categoria")
    private Integer idCategoria;

    @Column(name = "nom_categoria", nullable = false, length = 100)
    private String nomCategoria;

    @Column(name = "descripcion")
    private String descripcion;

    @Column(name = "estado")
    private Integer estado;

    public Categoria() {
    }

    public Categoria(Integer idCategoria, String nomCategoria, String descripcion, Integer estado) {
        this.idCategoria = idCategoria;
        this.nomCategoria = nomCategoria;
        this.descripcion = descripcion;
        this.estado = estado;
    }

    public Integer getIdCategoria() {
        return idCategoria;
    }

    public void setIdCategoria(Integer idCategoria) {
        this.idCategoria = idCategoria;
    }

    public String getNomCategoria() {
        return nomCategoria;
    }

    public void setNomCategoria(String nomCategoria) {
        this.nomCategoria = nomCategoria;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Integer getEstado() {
        return estado;
    }

    public void setEstado(Integer estado) {
        this.estado = estado;
    }
}