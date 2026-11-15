package ar.edu.unlp.dssd.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ar.edu.unlp.dssd.dto.ConfirmacionAdjudicacionResponse;
import ar.edu.unlp.dssd.dto.LoteCoberturaDto;
import ar.edu.unlp.dssd.dto.OfertaConsolidadaDto;
import ar.edu.unlp.dssd.dto.OfertasConsolidadasDto;
import ar.edu.unlp.dssd.dto.ResultadoValidacionRequest;
import ar.edu.unlp.dssd.dto.ValidacionesRequest;
import ar.edu.unlp.dssd.exception.RecursoNoEncontradoException;
import ar.edu.unlp.dssd.exception.ReglaNegocioException;
import ar.edu.unlp.dssd.model.Convocatoria;
import ar.edu.unlp.dssd.model.Emergencia;
import ar.edu.unlp.dssd.model.EstadoOferta;
import ar.edu.unlp.dssd.model.Necesidad;
import ar.edu.unlp.dssd.model.Oferta;
import ar.edu.unlp.dssd.model.OfertaConjunta;
import ar.edu.unlp.dssd.model.OfertaVersion;
import ar.edu.unlp.dssd.model.Organizacion;
import ar.edu.unlp.dssd.model.Proyecto;
import ar.edu.unlp.dssd.model.TipoNotificacion;
import ar.edu.unlp.dssd.model.TipoRecurso;
import ar.edu.unlp.dssd.repository.ConvocatoriaRepository;
import ar.edu.unlp.dssd.repository.EmergenciaRepository;
import ar.edu.unlp.dssd.repository.NecesidadRepository;
import ar.edu.unlp.dssd.repository.OfertaRepository;
import ar.edu.unlp.dssd.repository.OfertaVersionRepository;
import ar.edu.unlp.dssd.repository.ProyectoRepository;

/** [Entrega final] Validez de los lotes y consultas y registros que hace Bonita sobre las ofertas */
@Service
public class IntegracionService {

    // [Entrega final] Estados en los que el Sistema Nacional puede (re)registrar un resultado
    private static final Set<EstadoOferta> ESTADOS_VALIDABLES = EnumSet.of(EstadoOferta.PENDIENTE,
            EstadoOferta.VALIDADA, EstadoOferta.NO_HABILITADA);

    @Autowired
    private EmergenciaRepository emergenciaRepository;

    @Autowired
    private ConvocatoriaRepository convocatoriaRepository;

    @Autowired
    private NecesidadRepository necesidadRepository;

    @Autowired
    private OfertaRepository ofertaRepository;

    @Autowired
    private OfertaVersionRepository ofertaVersionRepository;

    @Autowired
    private ProyectoRepository proyectoRepository;

    @Autowired
    private NotificacionService notificacionService;

    // ------------------------------------------------------------------
    // Validez (regla 3)
    // ------------------------------------------------------------------

    /**
     * [Entrega final] Único punto que decide si un lote cuenta para la cobertura, el consolidado y el municipio:
     * no está RETIRADA y, si es de una oferta conjunta, esa oferta conjunta es válida.
     */
    public boolean ofertaEsValida(Oferta oferta) {
        if (oferta == null || oferta.getEstado() == EstadoOferta.RETIRADA) {
            return false;
        }
        return oferta.getOfertaConjunta() == null || conjuntaEsValida(oferta.getOfertaConjunta());
    }

    /** Una oferta conjunta es válida si todas sus ONGs asociadas tienen al menos un lote no retirado */
    public boolean conjuntaEsValida(OfertaConjunta conjunta) {
        return faltantes(conjunta, ofertaRepository.findByOfertaConjuntaId(conjunta.getId())).isEmpty();
    }

    /** Se filtran los lotes válidos calculando una sola vez la validez de cada oferta conjunta */
    public List<Oferta> filtrarValidas(Collection<Oferta> ofertas) {
        Map<Long, Boolean> validezPorConjunta = new HashMap<>();
        return ofertas.stream()
                .filter(o -> o.getEstado() != EstadoOferta.RETIRADA)
                .filter(o -> o.getOfertaConjunta() == null || validezPorConjunta.computeIfAbsent(
                        o.getOfertaConjunta().getId(), id -> conjuntaEsValida(o.getOfertaConjunta())))
                .toList();
    }

    /** Dueña y participantes de una oferta conjunta, con la dueña primero */
    public static List<Organizacion> asociadas(OfertaConjunta conjunta) {
        Set<Organizacion> todas = new LinkedHashSet<>();
        if (conjunta.getOrganizacionDuena() != null) {
            todas.add(conjunta.getOrganizacionDuena());
        }
        if (conjunta.getParticipantes() != null) {
            todas.addAll(conjunta.getParticipantes());
        }
        return new ArrayList<>(todas);
    }

    /** ONGs asociadas que todavía no tienen ningún lote no retirado en la oferta conjunta */
    public static List<Organizacion> faltantes(OfertaConjunta conjunta, List<Oferta> lotes) {
        Set<Long> conLotes = lotes.stream()
                .filter(o -> o.getEstado() != EstadoOferta.RETIRADA && o.getOrganizacion() != null)
                .map(o -> o.getOrganizacion().getId())
                .collect(Collectors.toSet());
        return asociadas(conjunta).stream().filter(org -> !conLotes.contains(org.getId())).toList();
    }

    // ------------------------------------------------------------------
    // Consolidado y validaciones (Bonita)
    // ------------------------------------------------------------------

    /** Se arma el consolidado de la convocatoria más reciente con los lotes válidos; el filtro de estado solo aplica al listado. */
    @Transactional(readOnly = true)
    public OfertasConsolidadasDto obtenerConsolidado(Long emergenciaId, EstadoOferta estado) {
        if (!emergenciaRepository.existsById(emergenciaId)) {
            throw new RecursoNoEncontradoException("Emergencia no encontrada: " + emergenciaId);
        }
        Convocatoria convocatoria = convocatoriaActual(emergenciaId);

        List<Necesidad> necesidades = necesidadRepository.findByConvocatoriaId(convocatoria.getId());
        List<Oferta> ofertas = filtrarValidas(ofertaRepository.findByConvocatoriaId(convocatoria.getId()));

        Map<Long, Long> requeridoPorTipo = necesidades.stream()
                .collect(Collectors.groupingBy(n -> n.getTipoRecurso().getId(),
                        Collectors.summingLong(n -> cantidad(n.getCantidad()))));
        Map<Long, Long> ofertadoPorTipo = ofertas.stream()
                .collect(Collectors.groupingBy(o -> o.getTipoRecurso().getId(),
                        Collectors.summingLong(o -> cantidad(o.getCantidad()))));
        boolean coberturaCompleta = requeridoPorTipo.entrySet().stream()
                .allMatch(e -> ofertadoPorTipo.getOrDefault(e.getKey(), 0L) >= e.getValue());

        List<LoteCoberturaDto> lotes = new ArrayList<>();
        for (Necesidad necesidad : necesidades) {
            TipoRecurso tipo = necesidad.getTipoRecurso();
            lotes.add(new LoteCoberturaDto(necesidad.getId(), tipo.getId(), tipo.getCodigo(), tipo.getNombre(),
                    cantidad(necesidad.getCantidad()), ofertadoPorTipo.getOrDefault(tipo.getId(), 0L)));
        }

        List<OfertaConsolidadaDto> listado = ofertas.stream()
                .filter(o -> estado == null || o.getEstado() == estado)
                .map(this::aConsolidada)
                .toList();

        return new OfertasConsolidadasDto(emergenciaId, convocatoria.getId(), convocatoria.getFechaCierre(),
                coberturaCompleta, lotes, listado);
    }

    /** Se marca cada lote VALIDADA o NO_HABILITADA; si alguno no corresponde no se registra nada. */
    @Transactional
    public int registrarValidaciones(Long emergenciaId, ValidacionesRequest request) {
        if (request == null || request.resultados() == null || request.resultados().isEmpty()) {
            throw new IllegalArgumentException("Hay que informar al menos un resultado");
        }
        if (!emergenciaRepository.existsById(emergenciaId)) {
            throw new RecursoNoEncontradoException("Emergencia no encontrada: " + emergenciaId);
        }

        List<Oferta> ofertas = new ArrayList<>();
        for (ResultadoValidacionRequest resultado : request.resultados()) {
            if (resultado == null || resultado.ofertaId() == null || resultado.habilitada() == null) {
                throw new IllegalArgumentException("Cada resultado debe indicar ofertaId y habilitada");
            }
            Oferta oferta = ofertaRepository.findById(resultado.ofertaId())
                    .filter(o -> perteneceAEmergencia(o, emergenciaId))
                    .orElseThrow(() -> new RecursoNoEncontradoException("La oferta " + resultado.ofertaId()
                            + " no pertenece a la emergencia " + emergenciaId));
            // [Entrega final] Solo se validan lotes válidos que no avanzaron más allá de la validación
            if (!ESTADOS_VALIDABLES.contains(oferta.getEstado()) || !ofertaEsValida(oferta)) {
                throw new ReglaNegocioException("La oferta " + resultado.ofertaId()
                        + " no está en condiciones de ser validada");
            }
            oferta.setEstado(resultado.habilitada() ? EstadoOferta.VALIDADA : EstadoOferta.NO_HABILITADA);
            oferta.setFechaValidacion(LocalDate.now());
            ofertas.add(oferta);
        }
        ofertaRepository.saveAll(ofertas);
        return ofertas.size();
    }

    /**
     * [Entrega final] Confirmación formal (idempotente): los lotes SELECCIONADA pasan a ADJUDICADA, se notifica
     * a cada ONG su resultado (sin duplicar) y se crea el proyecto EN_CURSO si todavía no existe.
     */
    @Transactional
    public ConfirmacionAdjudicacionResponse confirmarAdjudicacion(Long emergenciaId) {
        Emergencia emergencia = emergenciaRepository.findById(emergenciaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Emergencia no encontrada."));
        Convocatoria convocatoria = convocatoriaActual(emergenciaId);
        List<Oferta> lotes = ofertaRepository.findByConvocatoriaId(convocatoria.getId());

        int adjudicadas = 0;
        int notificaciones = 0;
        LocalDateTime ahora = LocalDateTime.now();
        for (Oferta oferta : lotes) {
            if (oferta.getEstado() == EstadoOferta.SELECCIONADA) {
                oferta.setEstado(EstadoOferta.ADJUDICADA);
                oferta.setFechaAdjudicacion(ahora);
                oferta.avanzarVersion();
                ofertaRepository.save(oferta);
                ofertaVersionRepository.save(OfertaVersion.desde(oferta, null, "Adjudicación confirmada"));
                adjudicadas++;
            }
            if (oferta.getEstado() == EstadoOferta.ADJUDICADA
                    && notificacionService.notificarResultado(oferta, TipoNotificacion.OFERTA_ADJUDICADA)) {
                notificaciones++;
            } else if (oferta.getEstado() == EstadoOferta.NO_SELECCIONADA
                    && notificacionService.notificarResultado(oferta, TipoNotificacion.OFERTA_NO_SELECCIONADA)) {
                notificaciones++;
            }
        }

        boolean hayAdjudicadas = lotes.stream().anyMatch(o -> o.getEstado() == EstadoOferta.ADJUDICADA
                || o.getEstado() == EstadoOferta.FINALIZADA);
        if (hayAdjudicadas && emergencia.getProyecto() == null) {
            Proyecto proyecto = new Proyecto();
            proyecto.setNombre("Emergencia N° " + emergencia.getId() + " · " + emergencia.getTipoDesastre()
                    + " · " + emergencia.getZonaAfectada());
            proyecto.setEstado(Proyecto.EN_CURSO);
            proyecto.setFechaInicio(ahora);
            emergencia.setProyecto(proyectoRepository.save(proyecto));
            emergenciaRepository.save(emergencia);
        }
        return new ConfirmacionAdjudicacionResponse(adjudicadas, notificaciones);
    }

    private Convocatoria convocatoriaActual(Long emergenciaId) {
        return convocatoriaRepository.findByEmergenciaIdOrderByIdDesc(emergenciaId)
                .stream().findFirst()
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "La emergencia " + emergenciaId + " no tiene convocatorias"));
    }

    private boolean perteneceAEmergencia(Oferta oferta, Long emergenciaId) {
        Convocatoria convocatoria = oferta.getConvocatoria();
        return convocatoria != null && convocatoria.getEmergencia() != null
                && emergenciaId.equals(convocatoria.getEmergencia().getId());
    }

    private OfertaConsolidadaDto aConsolidada(Oferta oferta) {
        Organizacion organizacion = oferta.getOrganizacion();
        TipoRecurso tipo = oferta.getTipoRecurso();
        return new OfertaConsolidadaDto(oferta.getId(), oferta.getEstado(),
                organizacion == null ? null : organizacion.getId(),
                organizacion == null ? null : organizacion.getCodigoOng(),
                organizacion == null ? null : organizacion.getNombre(),
                tipo.getId(), tipo.getCodigo(), tipo.getNombre(),
                oferta.getCantidad(), oferta.getDescripcion());
    }

    private static long cantidad(Integer valor) {
        return valor == null ? 0L : valor;
    }
}
