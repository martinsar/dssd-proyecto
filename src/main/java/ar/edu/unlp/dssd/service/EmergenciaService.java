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

    @Autowired
    private BonitaService bonitaService; // Inyectamos el servicio de Bonita

    public Emergencia guardar(Emergencia emergencia) {
        // 1. Guardar en la base de datos local (PostgreSQL)
        Emergencia emergenciaGuardada = emergenciaRepository.save(emergencia);

        // 2. Comunicarse con Bonita para iniciar la instancia
        try {
            bonitaService.iniciarInstanciaEmergencia(
                emergenciaGuardada.getId(),
                emergenciaGuardada.getTipoDesastre()
            );
        } catch (Exception e) {
            // Capturamos la excepción para que, si Bonita está apagado o falla,
            // la emergencia se guarde igual en PostgreSQL y el frontend no arroje error.
            System.err.println("Advertencia: No se pudo iniciar la instancia en Bonita BPM. Detalle: " + e.getMessage());
        }

        // 3. Retornar la emergencia ya guardada
        return emergenciaGuardada;
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