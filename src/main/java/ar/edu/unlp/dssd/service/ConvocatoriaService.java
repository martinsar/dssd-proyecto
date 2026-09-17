package ar.edu.unlp.dssd.service;

import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ar.edu.unlp.dssd.model.Convocatoria;
import ar.edu.unlp.dssd.repository.ConvocatoriaRepository;

@Service
public class ConvocatoriaService {

    @Autowired
    private ConvocatoriaRepository convocatoriaRepository;

    public Convocatoria guardar(Convocatoria convocatoria) {
        return convocatoriaRepository.save(convocatoria);
    }

    public List<Convocatoria> obtenerTodos() {
        return convocatoriaRepository.findAll();
    }

    public Optional<Convocatoria> obtenerPorId(Long id) {
        return convocatoriaRepository.findById(id);
    }

    public void eliminar(Long id) {
        convocatoriaRepository.deleteById(id);
    }
}