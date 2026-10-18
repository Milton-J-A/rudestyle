package pe.cibertec.rudestyle.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.cibertec.rudestyle.model.SolicitudPostventa;

import java.util.List;
import java.util.Optional;

@Repository
public interface SolicitudPostventaRepository extends JpaRepository<SolicitudPostventa, Integer> {
    
    // Método que ya tenías para el panel administrativo
    List<SolicitudPostventa> findAllByOrderByFechaRegistroDesc();

    // Buscar una solicitud específica por el código de orden
    Optional<SolicitudPostventa> findByCodigoOrden(String codigoOrden);

    // Listar todos los reclamos/garantías de un cliente filtrando por su correo
    List<SolicitudPostventa> findByCorreoClienteOrderByFechaRegistroDesc(String correoCliente);
}