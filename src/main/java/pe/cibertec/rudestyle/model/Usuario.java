package pe.cibertec.rudestyle.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "usuario")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Integer idUsuario;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "correo", length = 100)
    private String correo;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "clave", length = 255)
    private String clave;

    @Column(name = "contrasena", length = 255)
    private String contrasena;

    @Column(name = "telefono", length = 20)
    private String telefono;

    @Column(name = "direccion", length = 255)
    private String direccion;

    @Column(name = "rol", length = 20)
    private String rol;

    @Column(name = "fecha_registro")
    private LocalDateTime fechaRegistro;

    public Usuario() {
    }

    public Integer getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Integer idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCorreo() {
        return correo != null ? correo : email;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
        this.email = correo; // Mantiene ambos sincronizados
    }

    public String getEmail() {
        return email != null ? email : correo;
    }

    public void setEmail(String email) {
        this.email = email;
        this.correo = email; // Mantiene ambos sincronizados
    }

    public String getClave() {
        return clave != null ? clave : contrasena;
    }

    public void setClave(String clave) {
        this.clave = clave;
        this.contrasena = clave; // Mantiene ambos sincronizados
    }

    public String getContrasena() {
        return contrasena != null ? contrasena : clave;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
        this.clave = contrasena; // Mantiene ambos sincronizados
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDateTime fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }
}