package pe.cibertec.rudestyle.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.cibertec.rudestyle.model.DetallePedido;

import java.util.List;

@Repository
public interface DetallePedidoRepository extends JpaRepository<DetallePedido, Integer> {
    List<DetallePedido> findByPedido_IdPedido(Integer idPedido);
}