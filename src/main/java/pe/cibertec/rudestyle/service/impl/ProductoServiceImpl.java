package pe.cibertec.rudestyle.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pe.cibertec.rudestyle.model.Producto;
import pe.cibertec.rudestyle.repository.ProductoRepository;
import pe.cibertec.rudestyle.service.ProductoService;

import java.util.List;
import java.util.Optional;

@Service
public class ProductoServiceImpl implements ProductoService {

    @Autowired
    private ProductoRepository productoRepository;

    @Override
    public List<Producto> listarTodos() {
        return productoRepository.findAllWithCategoria();
    }

    @Override
    public List<Producto> listarDestacados() {
        return productoRepository.findByDestacadoTrue();
    }

    @Override
    public List<Producto> listarPorCategoria(Integer idCategoria) {
        return productoRepository.findByCategoria_IdCategoria(idCategoria);
    }

    @Override
    public Optional<Producto> buscarPorId(Integer id) {
        return productoRepository.findById(id);
    }

    @Override
    public List<Producto> buscarPorNombre(String filtro) {
        return productoRepository.buscarPorNombre(filtro);
    }
}