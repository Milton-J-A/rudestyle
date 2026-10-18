
package pe.cibertec.rudestyle.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import pe.cibertec.rudestyle.model.CalificacionProducto;

public interface CalificacionProductoRepository
        extends JpaRepository<CalificacionProducto, Long> {

    // ===== INICIO DE LA MODIFICACIÓN HECHA POR MARCO =====

    // Obtener las calificaciones de un producto.
    List<CalificacionProducto> findByProducto_IdProducto(Integer idProducto);

    // Calcular el promedio real de puntuaciones del producto.
    @Query("""
            SELECT AVG(c.puntuacion)
            FROM CalificacionProducto c
            WHERE c.producto.idProducto = :idProducto
            """)
    Double calcularPromedioPorProducto(
            @Param("idProducto") Integer idProducto);

    // Contar las calificaciones reales del producto.
    long countByProducto_IdProducto(Integer idProducto);

    // Buscar si un detalle de pedido ya tiene una calificación.
    Optional<CalificacionProducto> findByIdDetalle(Integer idDetalle);

    // Obtener las calificaciones asociadas a varios detalles.
    List<CalificacionProducto> findByIdDetalleIn(
            List<Integer> idsDetalle);

    // Guardar la relación entre una calificación y una etiqueta existente.
    @Modifying
    @Query(value = """
            INSERT INTO calificacion_etiquetas
                (id_calificacion, id_etiqueta)
            VALUES
                (:idCalificacion, :idEtiqueta)
            """, nativeQuery = true)
    int guardarEtiqueta(
            @Param("idCalificacion") Long idCalificacion,
            @Param("idEtiqueta") Long idEtiqueta);

    // Obtener cada etiqueta y el número de calificaciones que la seleccionaron.
    @Query(value = """
            SELECT
                COALESCE(em.nombre_etiqueta, em.nombre) AS nombreEtiqueta,
                COUNT(*) AS cantidad
            FROM calificacion_producto cp
            INNER JOIN calificacion_etiquetas ce
                ON ce.id_calificacion = cp.id_calificacion
            INNER JOIN etiquetas_maestro em
                ON em.id_etiqueta = ce.id_etiqueta
            WHERE cp.id_producto = :idProducto
              AND COALESCE(em.nombre_etiqueta, em.nombre) IS NOT NULL
            GROUP BY COALESCE(em.nombre_etiqueta, em.nombre)
            ORDER BY cantidad DESC
            """, nativeQuery = true)
    List<Object[]> obtenerEtiquetasPorProducto(
            @Param("idProducto") Integer idProducto);

    // ===== FIN DE LA MODIFICACIÓN HECHA POR MARCO =====
}
