package pe.cibertec.rudestyle.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.cibertec.rudestyle.model.SolicitudPostventa;
import java.util.List;

@Repository
public interface SolicitudPostventaRepository extends JpaRepository<SolicitudPostventa, Integer> {
    List<SolicitudPostventa> findAllByOrderByFechaRegistroDesc();
}