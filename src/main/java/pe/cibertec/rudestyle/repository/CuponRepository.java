package pe.cibertec.rudestyle.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.cibertec.rudestyle.model.Cupon;
import java.util.Optional;

@Repository
public interface CuponRepository extends JpaRepository<Cupon, Integer> {

    // 1. Busca por código exacto (para comprobar primero si existe)
    Optional<Cupon> findByCodigo(String codigo);

    // 2. Mantiene tu método para buscar código y estado (1 = activo)
    Optional<Cupon> findByCodigoAndEstado(String codigo, Integer estado);
}