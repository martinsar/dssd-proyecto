package ar.edu.unlp.dssd.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ar.edu.unlp.dssd.model.Emergencia;
import ar.edu.unlp.dssd.repository.EmergenciaRepository;

@Service
public class EmergenciaService {

    @Autowired
    private EmergenciaRepository emergenciaRepository;

    @Autowired
    private BonitaService bonitaService;

    /**
     * Registra una emergencia nueva y le crea su caso en Bonita (un caso por emergencia).
     *
     * Es transaccional: si Bonita falla, la excepcion (BonitaException) hace rollback y la
     * emergencia NO queda guardada sin caso. Necesitamos guardar primero para tener el id,
     * porque Bonita lo recibe en el contrato de inicio.
     *
     * Limitacion: Bonita no participa de la transaccion. Si el caso se creo y despues falla un
     * paso posterior (ej: completarPasosIniciales), la base hace rollback pero el caso queda
     * huerfano en Bonita.
     */
    @Transactional
    public Emergencia crear(Emergencia emergencia) {
        // Ignoramos lo que venga del cliente en estos campos: los define el servidor
        emergencia.setId(null);
        emergencia.setCaseId(null);
        emergencia.setFechaCreacion(LocalDate.now());

        Emergencia guardada = emergenciaRepository.save(emergencia);

        Long caseId = bonitaService.iniciarInstanciaEmergencia(guardada.getId(), guardada.getTipoDesastre());
        bonitaService.completarPasosIniciales(caseId, guardada.getId());

        guardada.setCaseId(caseId);
        return emergenciaRepository.save(guardada);
    }

    /** Solo persiste (sin tocar Bonita). Se usa para las actualizaciones. */
    public Emergencia guardar(Emergencia emergencia) {
        return emergenciaRepository.save(emergencia);
    }

    public List<Emergencia> obtenerTodos() {
        return emergenciaRepository.findAll();
    }

    /** Emergencias sin convocatoria publicada (las que el coordinador todavia tiene que gestionar). */
    public List<Emergencia> obtenerPendientes() {
        return emergenciaRepository.findPendientes();
    }

    public Optional<Emergencia> obtenerPorId(Long id) {
        return emergenciaRepository.findById(id);
    }

    public void eliminar(Long id) {
        emergenciaRepository.deleteById(id);
    }
}
