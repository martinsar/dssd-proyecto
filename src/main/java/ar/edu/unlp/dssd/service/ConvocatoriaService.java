package ar.edu.unlp.dssd.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ar.edu.unlp.dssd.dto.ConvocatoriaRequest;
import ar.edu.unlp.dssd.dto.NecesidadRequest;
import ar.edu.unlp.dssd.dto.PublicacionRequest;
import ar.edu.unlp.dssd.exception.RecursoNoEncontradoException;
import ar.edu.unlp.dssd.exception.ReglaNegocioException;
import ar.edu.unlp.dssd.model.Convocatoria;
import ar.edu.unlp.dssd.model.Emergencia;
import ar.edu.unlp.dssd.model.EstadoConvocatoria;
import ar.edu.unlp.dssd.repository.ConvocatoriaRepository;
import ar.edu.unlp.dssd.repository.EmergenciaRepository;
import ar.edu.unlp.dssd.repository.NecesidadRepository;

@Service
public class ConvocatoriaService {

    @Autowired
    private ConvocatoriaRepository convocatoriaRepository;

    @Autowired
    private EmergenciaRepository emergenciaRepository;

    @Autowired
    private NecesidadRepository necesidadRepository;

    @Autowired
    private NecesidadService necesidadService;

    @Autowired
    private BonitaService bonitaService;

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

    public List<Convocatoria> obtenerPorEmergencia(Long emergenciaId) {
        return convocatoriaRepository.findByEmergenciaIdOrderByIdDesc(emergenciaId);
    }

    /**
     * Relevamiento ("Desglosar Lotes"): crea la convocatoria en BORRADOR con todos sus lotes y completa
     * esa tarea en Bonita. Las fechas NO se cargan aca: se definen al publicar.
     * Todo o nada: si algo falla (incluido Bonita) la transaccion hace rollback y no queda nada guardado.
     */
    @Transactional
    public Convocatoria crearRelevamiento(Long emergenciaId, ConvocatoriaRequest request) {
        Emergencia emergencia = emergenciaRepository.findById(emergenciaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Emergencia no encontrada: " + emergenciaId));

        // Una emergencia solo puede tener una convocatoria "viva" (borrador o publicada) a la vez
        if (convocatoriaRepository.existsByEmergenciaIdAndEstadoIn(emergenciaId,
                EnumSet.of(EstadoConvocatoria.BORRADOR, EstadoConvocatoria.PUBLICADA))) {
            throw new ReglaNegocioException("La emergencia ya tiene una convocatoria en borrador o publicada");
        }
        if (emergencia.getCaseId() == null) {
            throw new ReglaNegocioException("La emergencia no tiene un caso asociado en Bonita");
        }
        if (request == null || request.necesidades() == null || request.necesidades().isEmpty()) {
            throw new IllegalArgumentException("Hay que informar al menos un lote (necesidad)");
        }

        Convocatoria convocatoria = new Convocatoria();
        convocatoria.setEmergencia(emergencia);
        convocatoria.setEstado(EstadoConvocatoria.BORRADOR);
        convocatoria.setFechaCreacion(LocalDate.now());
        Convocatoria guardada = convocatoriaRepository.save(convocatoria);

        // Las reglas de cada lote viven en NecesidadService (no se duplican aca)
        for (NecesidadRequest lote : request.necesidades()) {
            necesidadService.crearLote(guardada, lote);
        }

        // Ultimo paso: si Bonita falla se lanza BonitaException y se deshace todo lo anterior
        bonitaService.completarRelevamiento(emergencia.getCaseId());
        return guardada;
    }

    /**
     * Actualiza solo las fechas de apertura/cierre de una convocatoria en BORRADOR. El estado no se
     * toca aca: cambia unicamente al publicar (que pasa por Bonita). Los valores null se ignoran.
     */
    @Transactional
    public Convocatoria actualizarFechas(Long id, LocalDateTime fechaApertura, LocalDateTime fechaCierre) {
        Convocatoria convocatoria = convocatoriaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Convocatoria no encontrada: " + id));

        if (convocatoria.getEstado() != EstadoConvocatoria.BORRADOR) {
            throw new ReglaNegocioException("Solo se pueden modificar las fechas de una convocatoria en BORRADOR");
        }

        LocalDateTime apertura = fechaApertura != null ? fechaApertura : convocatoria.getFechaApertura();
        LocalDateTime cierre = fechaCierre != null ? fechaCierre : convocatoria.getFechaCierre();
        if (apertura != null && cierre != null && apertura.isAfter(cierre)) {
            throw new IllegalArgumentException("La fecha de apertura no puede ser posterior a la de cierre");
        }

        convocatoria.setFechaApertura(apertura);
        convocatoria.setFechaCierre(cierre);
        return convocatoriaRepository.save(convocatoria);
    }

    /**
     * Publicacion formal ("Gestionar Convocatoria"): define la ventana de tiempo, avanza el caso en
     * Bonita y recien despues marca PUBLICADA. Si Bonita falla se lanza una excepcion, la transaccion
     * hace rollback (las fechas tampoco se guardan) y queda en BORRADOR; se puede reintentar porque
     * BonitaService tolera que "Gestionar Convocatoria" ya este completada.
     */
    @Transactional
    public Convocatoria publicar(Long id, PublicacionRequest request) {
        Convocatoria convocatoria = convocatoriaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Convocatoria no encontrada: " + id));

        if (convocatoria.getEstado() != EstadoConvocatoria.BORRADOR) {
            throw new ReglaNegocioException("Solo se puede publicar una convocatoria en estado BORRADOR");
        }
        if (necesidadRepository.findByConvocatoriaId(id).isEmpty()) {
            throw new ReglaNegocioException("La convocatoria no tiene necesidades (lotes) cargadas");
        }
        Emergencia emergencia = convocatoria.getEmergencia();
        if (emergencia == null || emergencia.getCaseId() == null) {
            throw new ReglaNegocioException("La emergencia no tiene un caso asociado en Bonita");
        }

        LocalDateTime ahora = LocalDateTime.now();
        LocalDateTime fechaCierre = request == null ? null : request.fechaCierre();
        if (fechaCierre == null) {
            throw new IllegalArgumentException("La fecha de cierre es obligatoria");
        }
        if (!fechaCierre.isAfter(ahora)) {
            throw new IllegalArgumentException("La fecha de cierre debe ser futura");
        }
        LocalDateTime apertura = request.fechaApertura() != null ? request.fechaApertura() : ahora;
        if (apertura.isAfter(fechaCierre)) {
            throw new IllegalArgumentException("La fecha de apertura no puede ser posterior a la de cierre");
        }

        convocatoria.setFechaApertura(apertura);
        convocatoria.setFechaCierre(fechaCierre);

        bonitaService.publicarConvocatoria(emergencia.getCaseId(), emergencia.getId(), id, fechaCierre);

        convocatoria.setEstado(EstadoConvocatoria.PUBLICADA);
        return convocatoriaRepository.save(convocatoria);
    }
}
