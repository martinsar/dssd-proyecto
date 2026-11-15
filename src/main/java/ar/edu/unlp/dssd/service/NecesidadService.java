package ar.edu.unlp.dssd.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ar.edu.unlp.dssd.dto.NecesidadRequest;
import ar.edu.unlp.dssd.exception.RecursoNoEncontradoException;
import ar.edu.unlp.dssd.exception.ReglaNegocioException;
import ar.edu.unlp.dssd.model.Convocatoria;
import ar.edu.unlp.dssd.model.EstadoConvocatoria;
import ar.edu.unlp.dssd.model.Necesidad;
import ar.edu.unlp.dssd.model.TipoRecurso;
import ar.edu.unlp.dssd.repository.ConvocatoriaRepository;
import ar.edu.unlp.dssd.repository.NecesidadRepository;
import ar.edu.unlp.dssd.repository.TipoRecursoRepository;

@Service
public class NecesidadService {

    @Autowired
    private NecesidadRepository necesidadRepository;

    @Autowired
    private ConvocatoriaRepository convocatoriaRepository;

    @Autowired
    private TipoRecursoRepository tipoRecursoRepository;

    /**
     * Alta generica (POST /api/necesidades): la necesidad tiene que traer una convocatoria existente
     * y en BORRADOR, igual que el alta por convocatoria.
     */
    @Transactional
    public Necesidad guardar(Necesidad necesidad) {
        if (necesidad.getConvocatoria() == null || necesidad.getConvocatoria().getId() == null) {
            throw new IllegalArgumentException("La convocatoria de la necesidad es obligatoria");
        }
        Convocatoria convocatoria = buscarConvocatoria(necesidad.getConvocatoria().getId());
        exigirBorrador(convocatoria);
        // Usamos la convocatoria persistida, no la que vino en el body
        necesidad.setConvocatoria(convocatoria);
        return necesidadRepository.save(necesidad);
    }

    /**
     * Modificacion generica (PUT /api/necesidades/{id}): tanto la convocatoria actual como la destino
     * (si el body trae otra) deben estar en BORRADOR. Si el body no trae convocatoria se mantiene la actual.
     */
    @Transactional
    public Necesidad actualizar(Long id, Necesidad detalles) {
        Necesidad existente = necesidadRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Necesidad no encontrada: " + id));
        exigirConvocatoriaEnBorrador(existente);

        Convocatoria destino = existente.getConvocatoria();
        if (detalles.getConvocatoria() != null && detalles.getConvocatoria().getId() != null) {
            destino = buscarConvocatoria(detalles.getConvocatoria().getId());
            exigirBorrador(destino);
        }

        existente.setFechaCreacion(detalles.getFechaCreacion());
        existente.setDescripcion(detalles.getDescripcion());
        existente.setCantidad(detalles.getCantidad());
        existente.setConvocatoria(destino);
        existente.setTipoRecurso(detalles.getTipoRecurso());
        return necesidadRepository.save(existente);
    }

    public List<Necesidad> obtenerTodos() {
        return necesidadRepository.findAll();
    }

    public Optional<Necesidad> obtenerPorId(Long id) {
        return necesidadRepository.findById(id);
    }

    /** Baja generica (DELETE /api/necesidades/{id}): solo si su convocatoria esta en BORRADOR. */
    @Transactional
    public void eliminar(Long id) {
        Necesidad necesidad = necesidadRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Necesidad no encontrada: " + id));
        exigirConvocatoriaEnBorrador(necesidad);
        necesidadRepository.delete(necesidad);
    }

    public List<Necesidad> obtenerPorConvocatoria(Long convocatoriaId) {
        if (!convocatoriaRepository.existsById(convocatoriaId)) {
            throw new RecursoNoEncontradoException("Convocatoria no encontrada: " + convocatoriaId);
        }
        return necesidadRepository.findByConvocatoriaId(convocatoriaId);
    }

    /** Agrega un lote a una convocatoria en BORRADOR (una vez publicada ya no se puede modificar). */
    @Transactional
    public Necesidad agregar(Long convocatoriaId, NecesidadRequest request) {
        Convocatoria convocatoria = buscarConvocatoria(convocatoriaId);
        exigirBorrador(convocatoria);
        return crearLote(convocatoria, request);
    }

    /**
     * Valida y guarda un lote de una convocatoria dada. Es la unica implementacion de las reglas de
     * un lote: la usan tanto agregar() como el relevamiento (ConvocatoriaService.crearRelevamiento).
     */
    @Transactional
    public Necesidad crearLote(Convocatoria convocatoria, NecesidadRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("El lote no puede ser nulo");
        }
        if (request.cantidad() == null || request.cantidad() < 1) {
            throw new IllegalArgumentException("La cantidad debe ser un entero mayor o igual a 1");
        }
        if (request.tipoRecursoId() == null) {
            throw new IllegalArgumentException("El tipo de recurso es obligatorio");
        }
        TipoRecurso tipoRecurso = tipoRecursoRepository.findById(request.tipoRecursoId())
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Tipo de recurso no encontrado: " + request.tipoRecursoId()));

        Necesidad necesidad = new Necesidad();
        necesidad.setConvocatoria(convocatoria);
        necesidad.setTipoRecurso(tipoRecurso);
        necesidad.setCantidad(request.cantidad());
        necesidad.setDescripcion(request.descripcion());
        necesidad.setFechaCreacion(LocalDate.now());
        return necesidadRepository.save(necesidad);
    }

    /** Quita un lote de una convocatoria en BORRADOR. */
    @Transactional
    public void quitar(Long convocatoriaId, Long necesidadId) {
        Convocatoria convocatoria = buscarConvocatoria(convocatoriaId);

        // Si la necesidad existe pero es de otra convocatoria, tambien respondemos 404
        Necesidad necesidad = necesidadRepository.findById(necesidadId)
                .filter(n -> n.getConvocatoria() != null
                        && convocatoriaId.equals(n.getConvocatoria().getId()))
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "La necesidad " + necesidadId + " no pertenece a la convocatoria " + convocatoriaId));

        exigirBorrador(convocatoria);
        necesidadRepository.delete(necesidad);
    }

    private Convocatoria buscarConvocatoria(Long convocatoriaId) {
        return convocatoriaRepository.findById(convocatoriaId)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Convocatoria no encontrada: " + convocatoriaId));
    }

    /** Valida que la convocatoria de una necesidad ya guardada exista y este en BORRADOR. */
    private void exigirConvocatoriaEnBorrador(Necesidad necesidad) {
        if (necesidad.getConvocatoria() == null || necesidad.getConvocatoria().getId() == null) {
            throw new RecursoNoEncontradoException("La necesidad " + necesidad.getId() + " no tiene convocatoria");
        }
        exigirBorrador(buscarConvocatoria(necesidad.getConvocatoria().getId()));
    }

    private void exigirBorrador(Convocatoria convocatoria) {
        if (convocatoria.getEstado() != EstadoConvocatoria.BORRADOR) {
            throw new ReglaNegocioException("La convocatoria no está en BORRADOR, no se pueden modificar sus lotes");
        }
    }
}
