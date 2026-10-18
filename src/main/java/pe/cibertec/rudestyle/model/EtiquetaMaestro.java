
package pe.cibertec.rudestyle.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "etiquetas_maestro")
public class EtiquetaMaestro {

    // ===== INICIO DE LA MODIFICACIÓN HECHA POR MARCO =====

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_etiqueta")
    private Long idEtiqueta;

    @Column(name = "nombre")
    private String nombre;

    @Column(name = "nombre_etiqueta")
    private String nombreEtiqueta;

    public EtiquetaMaestro() {
    }

    public Long getIdEtiqueta() {
        return idEtiqueta;
    }

    public void setIdEtiqueta(Long idEtiqueta) {
        this.idEtiqueta = idEtiqueta;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getNombreEtiqueta() {
        return nombreEtiqueta;
    }

    public void setNombreEtiqueta(String nombreEtiqueta) {
        this.nombreEtiqueta = nombreEtiqueta;
    }

    // ===== FIN DE LA MODIFICACIÓN HECHA POR MARCO =====
}
