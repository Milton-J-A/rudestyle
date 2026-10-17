package pe.cibertec.rudestyle.service;

import pe.cibertec.rudestyle.model.Producto;
import java.util.List;
import java.util.Optional;

public interface ProductoService {
    List<Producto> listarTodos();
    List<Producto> listarDestacados();
    List<Producto> listarPorCategoria(Integer idCategoria);
    Optional<Producto> buscarPorId(Integer id);
    List<Producto> buscarPorNombre(String filtro);
}