package ar.edu.unlp.dssd.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ar.edu.unlp.dssd.model.Emergencia;
import ar.edu.unlp.dssd.repository.EmergenciaRepository;
import ar.edu.unlp.dssd.repository.UsuarioRepository;
import ar.edu.unlp.dssd.seguridad.Rol;
import ar.edu.unlp.dssd.seguridad.SesionUsuario;

@Service
public class EmergenciaService {

    @Autowired
    private EmergenciaRepository emergenciaRepository;

    @Autowired
    private BonitaService bonitaService;

    @Autowired
    private UsuarioRepository usuarioRepository;

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
    public Emergencia crear(Emergencia emergencia, SesionUsuario sesion) {
        // Ignoramos lo que venga del cliente en estos campos: los define el servidor
        emergencia.setId(null);
        emergencia.setCaseId(null);
        emergencia.setFechaCreacion(LocalDate.now());
        // [Entrega final] Se registra como autor al usuario de la sesión
        emergencia.setCreadoPor(sesion == null ? null : usuarioRepository.getReferenceById(sesion.usuarioId()));

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

    /** [Entrega final] El operador municipal solo ve las emergencias que creó; el resto ve todas */
    public List<Emergencia> obtenerVisibles(SesionUsuario sesion) {
        if (sesion.rol() == Rol.OPERADOR_MUNICIPAL) {
            return emergenciaRepository.findByCreadoPorIdOrderByIdDesc(sesion.usuarioId());
        }
        return emergenciaRepository.findAll();
    }

    /** [Entrega final] Se busca la emergencia aplicando la misma regla de visibilidad */
    public Optional<Emergencia> obtenerVisible(Long id, SesionUsuario sesion) {
        return emergenciaRepository.findById(id).filter(emergencia -> esVisible(emergencia, sesion));
    }

    private boolean esVisible(Emergencia emergencia, SesionUsuario sesion) {
        if (sesion.rol() != Rol.OPERADOR_MUNICIPAL) {
            return true;
        }
        return emergencia.getCreadoPor() != null
                && sesion.usuarioId().equals(emergencia.getCreadoPor().getId());
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
