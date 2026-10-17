package pe.cibertec.rudestyle.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pe.cibertec.rudestyle.model.Categoria;
import pe.cibertec.rudestyle.repository.CategoriaRepository;
import pe.cibertec.rudestyle.service.CategoriaService;

import java.util.List;

@Service
public class CategoriaServiceImpl implements CategoriaService {

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Override
    public List<Categoria> listarActivas() {
        return categoriaRepository.findByEstado(1);
    }
}