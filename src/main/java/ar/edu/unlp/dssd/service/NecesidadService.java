package ar.edu.unlp.dssd.service;

import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ar.edu.unlp.dssd.model.Necesidad;
import ar.edu.unlp.dssd.repository.NecesidadRepository;

@Service
public class NecesidadService {

    @Autowired
    private NecesidadRepository necesidadRepository;

    public Necesidad guardar(Necesidad necesidad) {
        return necesidadRepository.save(necesidad);
    }

    public List<Necesidad> obtenerTodos() {
        return necesidadRepository.findAll();
    }

    public Optional<Necesidad> obtenerPorId(Long id) {
        return necesidadRepository.findById(id);
    }

    public void eliminar(Long id) {
        necesidadRepository.deleteById(id);
    }
}