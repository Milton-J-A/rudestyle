package pe.cibertec.rudestyle.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "solicitud_postventa")
public class SolicitudPostventa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idSolicitud;

    @Column(nullable = false, unique = true, length = 20)
    private String codigoTicket;

    @Column(nullable = false)
    private LocalDateTime fechaRegistro;

    @Column(nullable = false, length = 150)
    private String nombreCliente;

    @Column(nullable = false, length = 100)
    private String correoCliente;

    @Column(nullable = false, length = 20)
    private String telefonoCliente;

    @Column(nullable = false, length = 50)
    private String motivo; // CONSULTA, CAMBIO_TALLA, DEVOLUCION, MAYORISTA

    @Column(length = 50)
    private String codigoOrden;

    @Column(length = 255)
    private String detalleGestion; // Nueva talla o cuenta bancaria/Yape

    @Column(length = 100)
    private String motivoDevolucion;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String mensaje;

    @Column(nullable = false, length = 30)
    private String estado = "PENDIENTE";
    
    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String fotoEvidencia;

    public String getFotoEvidencia() { return fotoEvidencia; }
    public void setFotoEvidencia(String fotoEvidencia) { this.fotoEvidencia = fotoEvidencia; }

    // Getters y Setters
    public Integer getIdSolicitud() { return idSolicitud; }
    public void setIdSolicitud(Integer idSolicitud) { this.idSolicitud = idSolicitud; }

    public String getCodigoTicket() { return codigoTicket; }
    public void setCodigoTicket(String codigoTicket) { this.codigoTicket = codigoTicket; }

    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDateTime fechaRegistro) { this.fechaRegistro = fechaRegistro; }

    public String getNombreCliente() { return nombreCliente; }
    public void setNombreCliente(String nombreCliente) { this.nombreCliente = nombreCliente; }

    public String getCorreoCliente() { return correoCliente; }
    public void setCorreoCliente(String correoCliente) { this.correoCliente = correoCliente; }

    public String getTelefonoCliente() { return telefonoCliente; }
    public void setTelefonoCliente(String telefonoCliente) { this.telefonoCliente = telefonoCliente; }

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }

    public String getCodigoOrden() { return codigoOrden; }
    public void setCodigoOrden(String codigoOrden) { this.codigoOrden = codigoOrden; }

    public String getDetalleGestion() { return detalleGestion; }
    public void setDetalleGestion(String detalleGestion) { this.detalleGestion = detalleGestion; }

    public String getMotivoDevolucion() { return motivoDevolucion; }
    public void setMotivoDevolucion(String motivoDevolucion) { this.motivoDevolucion = motivoDevolucion; }

    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
}