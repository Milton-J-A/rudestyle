package pe.cibertec.rudestyle.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.cibertec.rudestyle.model.Producto;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Integer> {

    @Query("SELECT p FROM Producto p JOIN FETCH p.categoria WHERE p.destacado = true")
    List<Producto> findByDestacadoTrue();

    @Query("SELECT p FROM Producto p JOIN FETCH p.categoria WHERE p.categoria.idCategoria = :idCat")
    List<Producto> findByCategoria_IdCategoria(@Param("idCat") Integer idCategoria);

    @Query("SELECT p FROM Producto p JOIN FETCH p.categoria")
    List<Producto> findAllWithCategoria();

    @Query("SELECT p FROM Producto p JOIN FETCH p.categoria WHERE LOWER(p.nomProducto) LIKE LOWER(CONCAT('%', :filtro, '%'))")
    List<Producto> buscarPorNombre(@Param("filtro") String filtro);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Producto p SET p.stock = p.stock - :cantidad " +
           "WHERE p.idProducto = :id AND p.stock >= :cantidad")
    int descontarStock(@Param("id") Integer id, @Param("cantidad") Integer cantidad);

    // =========================================================================
    // NUEVO: FILTRO COMBINADO MULTICRITERIO (Categoría, Texto y Rango de Precios)
    // =========================================================================
    @Query("SELECT p FROM Producto p JOIN FETCH p.categoria " +
           "WHERE (:idCategoria IS NULL OR p.categoria.idCategoria = :idCategoria) " +
           "AND (:busqueda IS NULL OR LOWER(p.nomProducto) LIKE LOWER(CONCAT('%', :busqueda, '%'))) " +
           "AND (:precioMin IS NULL OR p.precio >= :precioMin) " +
           "AND (:precioMax IS NULL OR p.precio <= :precioMax)")
    List<Producto> filtrarProductos(
            @Param("idCategoria") Integer idCategoria,
            @Param("busqueda") String busqueda,
            @Param("precioMin") BigDecimal precioMin,
            @Param("precioMax") BigDecimal precioMax
    );
}