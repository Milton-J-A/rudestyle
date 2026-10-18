
package pe.cibertec.rudestyle.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import pe.cibertec.rudestyle.model.EspecificacionProducto;

@Repository
public interface EspecificacionProductoRepository
        extends JpaRepository<EspecificacionProducto, Long> {

    // ===== INICIO DE LA MODIFICACIÓN HECHA POR MARCO =====
    // Busca las especificaciones correspondientes a un producto.
    Optional<EspecificacionProducto> findByProducto_IdProducto(Integer idProducto);
    // ===== FIN DE LA MODIFICACIÓN HECHA POR MARCO =====

}
