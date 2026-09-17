package ar.edu.unlp.dssd.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ar.edu.unlp.dssd.model.Emergencia;
import ar.edu.unlp.dssd.repository.EmergenciaRepository;

@Service
public class EmergenciaService {

    @Autowired
    private EmergenciaRepository emergenciaRepository;

    public Emergencia guardar(Emergencia emergencia) {
        return emergenciaRepository.save(emergencia);
    }

    public List<Emergencia> obtenerTodos() {
        return emergenciaRepository.findAll();
    }

    public Optional<Emergencia> obtenerPorId(Long id) {
        return emergenciaRepository.findById(id);
    }

    public void eliminar(Long id) {
        emergenciaRepository.deleteById(id);
    }
}