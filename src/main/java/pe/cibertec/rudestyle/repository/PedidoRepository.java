package pe.cibertec.rudestyle.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.cibertec.rudestyle.model.Pedido;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Integer> {
    List<Pedido> findAllByOrderByFechaPedidoDesc();
    
    List<Pedido> findByUsuario_IdUsuarioOrderByFechaPedidoDesc(Integer idUsuario);
    
    Optional<Pedido> findByCodigoOrden(String codigoOrden);

    // Búsqueda por coincidencia de texto (nombre, código o correo)
    @Query("SELECT p FROM Pedido p WHERE " +
           "LOWER(p.codigoOrden) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
           "LOWER(p.nombresCliente) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
           "LOWER(p.correoCliente) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
           "p.telefonoCliente LIKE CONCAT('%', :filtro, '%') " +
           "ORDER BY p.fechaPedido DESC")
    List<Pedido> buscarPorFiltro(@Param("filtro") String filtro);

    // Búsqueda por rango de fechas (para ventas del día)
    List<Pedido> findByFechaPedidoBetweenOrderByFechaPedidoDesc(LocalDateTime inicio, LocalDateTime fin);
}