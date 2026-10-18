
package pe.cibertec.rudestyle.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import pe.cibertec.rudestyle.model.EtiquetaMaestro;

// ===== INICIO DE LA MODIFICACIÓN HECHA POR MARCO =====

@Repository
public interface EtiquetaMaestroRepository
        extends JpaRepository<EtiquetaMaestro, Long> {

}

// ===== FIN DE LA MODIFICACIÓN HECHA POR MARCO =====
