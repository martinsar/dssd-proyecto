package ar.edu.unlp.dssd.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ar.edu.unlp.dssd.model.TipoRecurso;
import ar.edu.unlp.dssd.repository.TipoRecursoRepository;

@Service
public class TipoRecursoService {

    @Autowired
    private TipoRecursoRepository tipoRecursoRepository;

    // Guardar o Actualizar
    public TipoRecurso guardar(TipoRecurso tipoRecurso) {
        return tipoRecursoRepository.save(tipoRecurso);
    }

    // Obtener todos
    public List<TipoRecurso> obtenerTodos() {
        return tipoRecursoRepository.findAll();
    }

    // Obtener por ID
    public Optional<TipoRecurso> obtenerPorId(Long id) {
        return tipoRecursoRepository.findById(id);
    }

    // Eliminar
    public void eliminar(Long id) {
        tipoRecursoRepository.deleteById(id);
    }
}