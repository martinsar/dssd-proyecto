package ar.edu.unlp.dssd.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ar.edu.unlp.dssd.dto.FinalizacionRequest;
import ar.edu.unlp.dssd.dto.OfertaEdicionRequest;
import ar.edu.unlp.dssd.dto.OfertaOngDto;
import ar.edu.unlp.dssd.dto.OfertaRequest;
import ar.edu.unlp.dssd.dto.OfertaVersionDto;
import ar.edu.unlp.dssd.exception.AccesoDenegadoException;
import ar.edu.unlp.dssd.exception.RecursoNoEncontradoException;
import ar.edu.unlp.dssd.exception.ReglaNegocioException;
import ar.edu.unlp.dssd.model.Convocatoria;
import ar.edu.unlp.dssd.model.Emergencia;
import ar.edu.unlp.dssd.model.EstadoOferta;
import ar.edu.unlp.dssd.model.Oferta;
import ar.edu.unlp.dssd.model.OfertaConjunta;
import ar.edu.unlp.dssd.model.OfertaVersion;
import ar.edu.unlp.dssd.model.Organizacion;
import ar.edu.unlp.dssd.model.Proyecto;
import ar.edu.unlp.dssd.model.TipoRecurso;
import ar.edu.unlp.dssd.model.Usuario;
import ar.edu.unlp.dssd.repository.ConvocatoriaRepository;
import ar.edu.unlp.dssd.repository.NecesidadRepository;
import ar.edu.unlp.dssd.repository.OfertaConjuntaRepository;
import ar.edu.unlp.dssd.repository.OfertaRepository;
import ar.edu.unlp.dssd.repository.OfertaVersionRepository;
import ar.edu.unlp.dssd.repository.OrganizacionRepository;
import ar.edu.unlp.dssd.repository.ProyectoRepository;
import ar.edu.unlp.dssd.repository.TipoRecursoRepository;
import ar.edu.unlp.dssd.repository.UsuarioRepository;
import ar.edu.unlp.dssd.seguridad.SesionUsuario;

/** [Entrega final] Lotes de las ONGs: alta, edición con versiones, retiro y finalización */
@Service
public class OfertaService {

    private static final String SIN_ORGANIZACION = "Tu usuario no está asociado a ninguna organización.";
    private static final String NO_ENCONTRADA = "Oferta no encontrada.";
    private static final String VENTANA_CERRADA = "La convocatoria no está recibiendo ofertas.";

    @Autowired
    private OfertaRepository ofertaRepository;

    @Autowired
    private ConvocatoriaRepository convocatoriaRepository;

    @Autowired
    private OrganizacionRepository organizacionRepository;

    @Autowired
    private TipoRecursoRepository tipoRecursoRepository;

    @Autowired
    private NecesidadRepository necesidadRepository;

    @Autowired
    private OfertaConjuntaRepository ofertaConjuntaRepository;

    @Autowired
    private OfertaVersionRepository ofertaVersionRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ProyectoRepository proyectoRepository;

    @Autowired
    private IntegracionService integracionService;

    @Autowired
    private TareasFinalesBonitaService tareasFinalesBonita;

    /** La organización sale de la sesión; el lote queda PENDIENTE en su versión 1 */
    @Transactional
    public OfertaOngDto crear(OfertaRequest request, SesionUsuario sesion) {
        Long organizacionId = exigirOrganizacion(sesion);
        Convocatoria convocatoria = convocatoriaRepository.findById(request.convocatoriaId())
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Convocatoria no encontrada: " + request.convocatoriaId()));
        exigirVentanaAbierta(convocatoria);
        TipoRecurso tipoRecurso = buscarTipoPedido(convocatoria, request.tipoRecursoId());

        OfertaConjunta conjunta = null;
        if (request.ofertaConjuntaId() != null) {
            conjunta = ofertaConjuntaRepository.findById(request.ofertaConjuntaId())
                    .orElseThrow(() -> new RecursoNoEncontradoException("Oferta conjunta no encontrada."));
            if (!esAsociada(conjunta, organizacionId)) {
                throw new AccesoDenegadoException("Tu organización no participa de esa oferta conjunta.");
            }
            if (conjunta.getConvocatoria() == null || !convocatoria.getId().equals(conjunta.getConvocatoria().getId())) {
                throw new IllegalArgumentException("La oferta conjunta pertenece a otra convocatoria.");
            }
        }
        Organizacion organizacion = organizacionRepository.findById(organizacionId)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Organizacion no encontrada: " + organizacionId));

        Oferta oferta = new Oferta();
        oferta.setCantidad(request.cantidad());
        oferta.setDescripcion(request.descripcion());
        oferta.setConvocatoria(convocatoria);
        oferta.setOrganizacion(organizacion);
        oferta.setTipoRecurso(tipoRecurso);
        oferta.setOfertaConjunta(conjunta);
        oferta.setFechaCreacion(LocalDate.now());
        oferta.setFechaModificacion(LocalDateTime.now());
        oferta.setNumeroVersion(1);
        oferta.setEstado(EstadoOferta.PENDIENTE);
        Oferta guardada = ofertaRepository.save(oferta);
        registrarVersion(guardada, sesion, "Alta del lote");
        return aDto(guardada, organizacionId, new HashMap<>());
    }

    /** Lotes cargados por la organización de la sesión (incluye los de sus ofertas conjuntas) */
    @Transactional(readOnly = true)
    public List<OfertaOngDto> listarPropias(SesionUsuario sesion) {
        Long organizacionId = exigirOrganizacion(sesion);
        Map<Long, Boolean> validez = new HashMap<>();
        return ofertaRepository.findByOrganizacionIdOrderByIdDesc(organizacionId).stream()
                .map(o -> aDto(o, organizacionId, validez))
                .toList();
    }

    /** Lote propio o de una oferta conjunta de la que participa la organización; si no, 404 */
    @Transactional(readOnly = true)
    public OfertaOngDto obtener(Long id, SesionUsuario sesion) {
        Long organizacionId = exigirOrganizacion(sesion);
        Oferta oferta = ofertaRepository.findById(id)
                .filter(o -> esVisible(o, organizacionId))
                .orElseThrow(() -> new RecursoNoEncontradoException(NO_ENCONTRADA));
        return aDto(oferta, organizacionId, new HashMap<>());
    }

    @Transactional(readOnly = true)
    public List<OfertaVersionDto> versiones(Long id, SesionUsuario sesion) {
        Long organizacionId = exigirOrganizacion(sesion);
        ofertaRepository.findById(id)
                .filter(o -> esVisible(o, organizacionId))
                .orElseThrow(() -> new RecursoNoEncontradoException(NO_ENCONTRADA));
        return ofertaVersionRepository.findByOfertaIdOrderByNumeroVersionDesc(id).stream()
                .map(OfertaVersionDto::desde)
                .toList();
    }

    /** Solo con la ventana abierta, el lote PENDIENTE y la versión esperada igual a la vigente */
    @Transactional
    public OfertaOngDto editar(Long id, OfertaEdicionRequest request, SesionUsuario sesion) {
        Long organizacionId = exigirOrganizacion(sesion);
        Oferta oferta = buscarPropiaParaActualizar(id, organizacionId);
        exigirEditable(oferta);
        int vigente = oferta.getNumeroVersion() == null ? 1 : oferta.getNumeroVersion();
        if (request.versionEsperada() == null || request.versionEsperada() != vigente) {
            throw new ReglaNegocioException(
                    "El lote fue modificado por otra persona. Recargá para ver la última versión.");
        }
        oferta.setTipoRecurso(buscarTipoPedido(oferta.getConvocatoria(), request.tipoRecursoId()));
        oferta.setCantidad(request.cantidad());
        oferta.setDescripcion(request.descripcion());
        oferta.avanzarVersion();
        Oferta guardada = ofertaRepository.save(oferta);
        registrarVersion(guardada, sesion, "Modificación del lote");
        return aDto(guardada, organizacionId, new HashMap<>());
    }

    /** El lote queda RETIRADA (no se borra) y deja de contar para la oferta conjunta */
    @Transactional
    public OfertaOngDto retirar(Long id, SesionUsuario sesion) {
        Long organizacionId = exigirOrganizacion(sesion);
        Oferta oferta = buscarPropiaParaActualizar(id, organizacionId);
        exigirEditable(oferta);
        oferta.setEstado(EstadoOferta.RETIRADA);
        oferta.avanzarVersion();
        Oferta guardada = ofertaRepository.save(oferta);
        registrarVersion(guardada, sesion, "Retiro del lote");
        return aDto(guardada, organizacionId, new HashMap<>());
    }

    /**
     * La ONG marca su lote ADJUDICADA como FINALIZADA (regla 9). Si ya no quedan lotes adjudicados sin
     * finalizar, el proyecto pasa a TERMINADO y se ejecuta "Finalizar actividades" en Bonita.
     */
    @Transactional
    public OfertaOngDto finalizar(Long id, FinalizacionRequest request, SesionUsuario sesion) {
        Long organizacionId = exigirOrganizacion(sesion);
        Oferta oferta = buscarPropiaParaActualizar(id, organizacionId);
        if (oferta.getEstado() != EstadoOferta.ADJUDICADA) {
            throw new ReglaNegocioException("Solo se puede finalizar un lote adjudicado.");
        }
        String informe = request == null ? null : request.informe();
        if (informe != null && informe.length() > 500) {
            throw new IllegalArgumentException("El informe puede tener hasta 500 caracteres");
        }
        oferta.setEstado(EstadoOferta.FINALIZADA);
        oferta.setFechaFinalizacion(LocalDateTime.now());
        oferta.setInformeFinalizacion(informe == null || informe.isBlank() ? null : informe.trim());
        oferta.avanzarVersion();
        Oferta guardada = ofertaRepository.save(oferta);
        registrarVersion(guardada, sesion, "Actividad finalizada");

        cerrarProyectoSiCorresponde(guardada.getConvocatoria());
        return aDto(guardada, organizacionId, new HashMap<>());
    }

    /** Se cierra el proyecto cuando todos los lotes adjudicados de la convocatoria están finalizados */
    private void cerrarProyectoSiCorresponde(Convocatoria convocatoria) {
        boolean quedanPendientes = ofertaRepository.findByConvocatoriaId(convocatoria.getId()).stream()
                .anyMatch(o -> o.getEstado() == EstadoOferta.ADJUDICADA || o.getEstado() == EstadoOferta.SELECCIONADA);
        if (quedanPendientes) {
            return;
        }
        Emergencia emergencia = convocatoria.getEmergencia();
        if (emergencia == null) {
            return;
        }
        Proyecto proyecto = emergencia.getProyecto();
        if (proyecto != null && !Proyecto.TERMINADO.equals(proyecto.getEstado())) {
            proyecto.setEstado(Proyecto.TERMINADO);
            proyecto.setFechaFin(LocalDateTime.now());
            proyectoRepository.save(proyecto);
        }
        // Al final, para que con integracion-completa=true un error de Bonita deshaga todo
        tareasFinalesBonita.finalizarActividades(emergencia.getCaseId());
    }

    // ------------------------------------------------------------------
    // Conversión y reglas compartidas
    // ------------------------------------------------------------------

    /** validez: cache de validez por oferta conjunta para no recalcularla en cada lote */
    public OfertaOngDto aDto(Oferta oferta, Long organizacionId, Map<Long, Boolean> validez) {
        OfertaConjunta conjunta = oferta.getOfertaConjunta();
        boolean conjuntaValida = conjunta == null
                || validez.computeIfAbsent(conjunta.getId(), clave -> integracionService.conjuntaEsValida(conjunta));
        boolean propia = oferta.getOrganizacion() != null && organizacionId.equals(oferta.getOrganizacion().getId());
        return OfertaOngDto.desde(oferta,
                EtapaEmergenciaService.ventanaAbierta(oferta.getConvocatoria(), LocalDateTime.now()),
                conjuntaValida, fueraDeEspecialidad(oferta.getOrganizacion(), oferta.getTipoRecurso()), propia);
    }

    /**
     * [Entrega final] El tipo no está entre las especialidades de la ONG. Si la ONG no tiene especialidades
     * cargadas no se marca nada.
     */
    public static boolean fueraDeEspecialidad(Organizacion organizacion, TipoRecurso tipo) {
        if (organizacion == null || tipo == null || organizacion.getEspecialidades() == null
                || organizacion.getEspecialidades().isEmpty()) {
            return false;
        }
        return organizacion.getEspecialidades().stream().noneMatch(e -> e.getId().equals(tipo.getId()));
    }

    static boolean esAsociada(OfertaConjunta conjunta, Long organizacionId) {
        return IntegracionService.asociadas(conjunta).stream().anyMatch(o -> organizacionId.equals(o.getId()));
    }

    private boolean esVisible(Oferta oferta, Long organizacionId) {
        if (oferta.getOrganizacion() != null && organizacionId.equals(oferta.getOrganizacion().getId())) {
            return true;
        }
        return oferta.getOfertaConjunta() != null && esAsociada(oferta.getOfertaConjunta(), organizacionId);
    }

    /** 404 si la organización no ve el lote; 403 si lo ve (oferta conjunta) pero lo cargó otra ONG */
    private Oferta buscarPropiaParaActualizar(Long id, Long organizacionId) {
        Oferta oferta = ofertaRepository.buscarParaActualizar(id)
                .filter(o -> esVisible(o, organizacionId))
                .orElseThrow(() -> new RecursoNoEncontradoException(NO_ENCONTRADA));
        if (oferta.getOrganizacion() == null || !organizacionId.equals(oferta.getOrganizacion().getId())) {
            throw new AccesoDenegadoException("Solo la organización que cargó el lote puede modificarlo.");
        }
        return oferta;
    }

    private void exigirEditable(Oferta oferta) {
        exigirVentanaAbierta(oferta.getConvocatoria());
        if (oferta.getEstado() != EstadoOferta.PENDIENTE) {
            throw new ReglaNegocioException("Solo se puede modificar o retirar un lote PENDIENTE.");
        }
    }

    static void exigirVentanaAbierta(Convocatoria convocatoria) {
        if (!EtapaEmergenciaService.ventanaAbierta(convocatoria, LocalDateTime.now())) {
            throw new ReglaNegocioException(VENTANA_CERRADA);
        }
    }

    static Long exigirOrganizacion(SesionUsuario sesion) {
        if (sesion == null || sesion.organizacionId() == null) {
            throw new AccesoDenegadoException(SIN_ORGANIZACION);
        }
        return sesion.organizacionId();
    }

    private TipoRecurso buscarTipoPedido(Convocatoria convocatoria, Long tipoRecursoId) {
        TipoRecurso tipoRecurso = tipoRecursoRepository.findById(tipoRecursoId)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Tipo de recurso no encontrado: " + tipoRecursoId));
        boolean pedido = necesidadRepository.findByConvocatoriaId(convocatoria.getId()).stream()
                .anyMatch(n -> n.getTipoRecurso() != null && tipoRecursoId.equals(n.getTipoRecurso().getId()));
        if (!pedido) {
            throw new IllegalArgumentException("La convocatoria no pide ese tipo de recurso.");
        }
        return tipoRecurso;
    }

    private void registrarVersion(Oferta oferta, SesionUsuario sesion, String motivo) {
        Usuario usuario = sesion == null || sesion.usuarioId() == null
                ? null : usuarioRepository.getReferenceById(sesion.usuarioId());
        ofertaVersionRepository.save(OfertaVersion.desde(oferta, usuario, motivo));
    }
}
