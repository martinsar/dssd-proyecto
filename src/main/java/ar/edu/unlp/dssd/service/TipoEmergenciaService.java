package ar.edu.unlp.dssd.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ar.edu.unlp.dssd.model.TipoEmergencia;
import ar.edu.unlp.dssd.repository.TipoEmergenciaRepository;

@Service
public class TipoEmergenciaService {

    @Autowired
    private TipoEmergenciaRepository tipoEmergenciaRepository;

    public TipoEmergencia guardar(TipoEmergencia tipoEmergencia) {
        return tipoEmergenciaRepository.save(tipoEmergencia);
    }

    public List<TipoEmergencia> obtenerTodos() {
        return tipoEmergenciaRepository.findAll();
    }

    public Optional<TipoEmergencia> obtenerPorId(Long id) {
        return tipoEmergenciaRepository.findById(id);
    }

    public void eliminar(Long id) {
        tipoEmergenciaRepository.deleteById(id);
    }
}