package ar.edu.unlp.dssd.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ar.edu.unlp.dssd.dto.FinalizacionRequest;
import ar.edu.unlp.dssd.dto.OfertaEdicionRequest;
import ar.edu.unlp.dssd.dto.OfertaOngDto;
import ar.edu.unlp.dssd.dto.OfertaRequest;
import ar.edu.unlp.dssd.exception.AccesoDenegadoException;
import ar.edu.unlp.dssd.exception.RecursoNoEncontradoException;
import ar.edu.unlp.dssd.exception.ReglaNegocioException;
import ar.edu.unlp.dssd.model.Convocatoria;
import ar.edu.unlp.dssd.model.Emergencia;
import ar.edu.unlp.dssd.model.EstadoConvocatoria;
import ar.edu.unlp.dssd.model.EstadoOferta;
import ar.edu.unlp.dssd.model.Necesidad;
import ar.edu.unlp.dssd.model.Oferta;
import ar.edu.unlp.dssd.model.OfertaConjunta;
import ar.edu.unlp.dssd.model.OfertaVersion;
import ar.edu.unlp.dssd.model.Organizacion;
import ar.edu.unlp.dssd.model.Proyecto;
import ar.edu.unlp.dssd.model.TipoRecurso;
import ar.edu.unlp.dssd.repository.ConvocatoriaRepository;
import ar.edu.unlp.dssd.repository.NecesidadRepository;
import ar.edu.unlp.dssd.repository.OfertaConjuntaRepository;
import ar.edu.unlp.dssd.repository.OfertaRepository;
import ar.edu.unlp.dssd.repository.OfertaVersionRepository;
import ar.edu.unlp.dssd.repository.OrganizacionRepository;
import ar.edu.unlp.dssd.repository.ProyectoRepository;
import ar.edu.unlp.dssd.repository.TipoRecursoRepository;
import ar.edu.unlp.dssd.repository.UsuarioRepository;
import ar.edu.unlp.dssd.seguridad.Rol;
import ar.edu.unlp.dssd.seguridad.SesionUsuario;

/** [Entrega final] Reglas 4, 5 y 9 sobre los lotes de las ONGs */
@ExtendWith(MockitoExtension.class)
class OfertaServiceTest {

    @Mock private OfertaRepository ofertaRepository;
    @Mock private ConvocatoriaRepository convocatoriaRepository;
    @Mock private OrganizacionRepository organizacionRepository;
    @Mock private TipoRecursoRepository tipoRecursoRepository;
    @Mock private NecesidadRepository necesidadRepository;
    @Mock private OfertaConjuntaRepository ofertaConjuntaRepository;
    @Mock private OfertaVersionRepository ofertaVersionRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private ProyectoRepository proyectoRepository;
    @Mock private IntegracionService integracionService;
    @Mock private TareasFinalesBonitaService tareasFinalesBonita;
    @InjectMocks private OfertaService ofertaService;

    private final TipoRecurso agua = tipo(1L, "AGUA");
    private final TipoRecurso carpas = tipo(2L, "CARPAS");
    private final Organizacion caritas = organizacion(1L, "Cáritas");
    private final Organizacion cruzRoja = organizacion(2L, "Cruz Roja Argentina");
    private final SesionUsuario sesionCaritas = sesion(1L);
    private final Emergencia emergencia = new Emergencia();

    private static TipoRecurso tipo(Long id, String codigo) {
        TipoRecurso t = new TipoRecurso();
        t.setId(id);
        t.setCodigo(codigo);
        t.setNombre(codigo);
        return t;
    }

    private static Organizacion organizacion(Long id, String nombre) {
        Organizacion o = new Organizacion();
        o.setId(id);
        o.setNombre(nombre);
        return o;
    }

    private static SesionUsuario sesion(Long organizacionId) {
        return new SesionUsuario("t", 10L, "ong", "Rosa", "Representante", Rol.ONG,
                organizacionId, "ONG", null, Instant.now());
    }

    private Convocatoria convocatoria(boolean abierta) {
        emergencia.setId(5L);
        emergencia.setCaseId(100L);
        Convocatoria c = new Convocatoria();
        c.setId(7L);
        c.setEmergencia(emergencia);
        c.setEstado(EstadoConvocatoria.PUBLICADA);
        c.setFechaApertura(LocalDateTime.now().minusDays(2));
        c.setFechaCierre(abierta ? LocalDateTime.now().plusDays(1) : LocalDateTime.now().minusMinutes(1));
        return c;
    }

    private Oferta lote(Long id, Organizacion organizacion, EstadoOferta estado, Convocatoria convocatoria) {
        Oferta o = new Oferta();
        o.setId(id);
        o.setOrganizacion(organizacion);
        o.setTipoRecurso(agua);
        o.setCantidad(100);
        o.setDescripcion("Bidones");
        o.setEstado(estado);
        o.setConvocatoria(convocatoria);
        o.setNumeroVersion(2);
        return o;
    }

    private void conLotesPedidos(TipoRecurso... tipos) {
        List<Necesidad> necesidades = java.util.Arrays.stream(tipos).map(t -> {
            Necesidad n = new Necesidad();
            n.setTipoRecurso(t);
            n.setCantidad(500);
            return n;
        }).toList();
        when(necesidadRepository.findByConvocatoriaId(7L)).thenReturn(necesidades);
    }

    private OfertaRequest alta(Long tipoRecursoId, Long ofertaConjuntaId) {
        return new OfertaRequest(100, "Bidones", tipoRecursoId, null, 7L, ofertaConjuntaId);
    }

    // ------------------------------------------------------------------
    // Alta
    // ------------------------------------------------------------------

    @Test
    void crear_guardaLaVersion1PendienteYMarcaFueraDeEspecialidad() {
        caritas.setEspecialidades(new LinkedHashSet<>(Set.of(agua)));
        when(convocatoriaRepository.findById(7L)).thenReturn(Optional.of(convocatoria(true)));
        when(tipoRecursoRepository.findById(2L)).thenReturn(Optional.of(carpas));
        conLotesPedidos(agua, carpas);
        when(organizacionRepository.findById(1L)).thenReturn(Optional.of(caritas));
        when(ofertaRepository.save(any(Oferta.class))).then(returnsFirstArg());

        OfertaOngDto dto = ofertaService.crear(alta(2L, null), sesionCaritas);

        assertThat(dto.estado()).isEqualTo(EstadoOferta.PENDIENTE);
        assertThat(dto.numeroVersion()).isEqualTo(1);
        assertThat(dto.fueraDeEspecialidad()).isTrue();
        assertThat(dto.conjuntaValida()).isTrue();
        assertThat(dto.puedeEditar()).isTrue();
        assertThat(dto.puedeFinalizar()).isFalse();
        ArgumentCaptor<OfertaVersion> version = ArgumentCaptor.forClass(OfertaVersion.class);
        verify(ofertaVersionRepository).save(version.capture());
        assertThat(version.getValue().getNumeroVersion()).isEqualTo(1);
        assertThat(version.getValue().getEstado()).isEqualTo(EstadoOferta.PENDIENTE);
    }

    @Test
    void crear_conLaVentanaCerrada_responde409SinGuardar() {
        when(convocatoriaRepository.findById(7L)).thenReturn(Optional.of(convocatoria(false)));

        assertThatThrownBy(() -> ofertaService.crear(alta(1L, null), sesionCaritas))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessage("La convocatoria no está recibiendo ofertas.");
        verify(ofertaRepository, never()).save(any());
    }

    @Test
    void crear_conTipoNoPedido_responde400() {
        when(convocatoriaRepository.findById(7L)).thenReturn(Optional.of(convocatoria(true)));
        when(tipoRecursoRepository.findById(2L)).thenReturn(Optional.of(carpas));
        conLotesPedidos(agua);

        assertThatThrownBy(() -> ofertaService.crear(alta(2L, null), sesionCaritas))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void crear_enUnaConjuntaAjena_responde403() {
        Convocatoria c = convocatoria(true);
        OfertaConjunta ajena = new OfertaConjunta();
        ajena.setId(4L);
        ajena.setConvocatoria(c);
        ajena.setOrganizacionDuena(cruzRoja);
        ajena.setParticipantes(new LinkedHashSet<>(Set.of(organizacion(3L, "Bomberos"))));
        when(convocatoriaRepository.findById(7L)).thenReturn(Optional.of(c));
        when(tipoRecursoRepository.findById(1L)).thenReturn(Optional.of(agua));
        conLotesPedidos(agua);
        when(ofertaConjuntaRepository.findById(4L)).thenReturn(Optional.of(ajena));

        assertThatThrownBy(() -> ofertaService.crear(alta(1L, 4L), sesionCaritas))
                .isInstanceOf(AccesoDenegadoException.class);
    }

    @Test
    void crear_sinOrganizacionEnLaSesion_responde403() {
        assertThatThrownBy(() -> ofertaService.crear(alta(1L, null), sesion(null)))
                .isInstanceOf(AccesoDenegadoException.class);
    }

    // ------------------------------------------------------------------
    // Edición y retiro
    // ------------------------------------------------------------------

    @Test
    void editar_conLaVersionEsperada_actualizaYPasaALaSiguiente() {
        Oferta oferta = lote(21L, caritas, EstadoOferta.PENDIENTE, convocatoria(true));
        when(ofertaRepository.buscarParaActualizar(21L)).thenReturn(Optional.of(oferta));
        when(tipoRecursoRepository.findById(1L)).thenReturn(Optional.of(agua));
        conLotesPedidos(agua);
        when(ofertaRepository.save(any(Oferta.class))).then(returnsFirstArg());

        OfertaOngDto dto = ofertaService.editar(21L, new OfertaEdicionRequest(1L, 250, "Bidones de 5 L", 2),
                sesionCaritas);

        assertThat(dto.cantidad()).isEqualTo(250);
        assertThat(dto.descripcion()).isEqualTo("Bidones de 5 L");
        assertThat(dto.numeroVersion()).isEqualTo(3);
        verify(ofertaVersionRepository).save(any(OfertaVersion.class));
    }

    @Test
    void editar_conOtraVersionEsperada_responde409SinGuardar() {
        when(ofertaRepository.buscarParaActualizar(21L))
                .thenReturn(Optional.of(lote(21L, caritas, EstadoOferta.PENDIENTE, convocatoria(true))));

        assertThatThrownBy(() -> ofertaService.editar(21L, new OfertaEdicionRequest(1L, 250, "x", 1), sesionCaritas))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("Recargá");
        verify(ofertaRepository, never()).save(any());
    }

    @Test
    void editar_conLaVentanaCerrada_responde409() {
        when(ofertaRepository.buscarParaActualizar(21L))
                .thenReturn(Optional.of(lote(21L, caritas, EstadoOferta.PENDIENTE, convocatoria(false))));

        assertThatThrownBy(() -> ofertaService.editar(21L, new OfertaEdicionRequest(1L, 250, "x", 2), sesionCaritas))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessage("La convocatoria no está recibiendo ofertas.");
    }

    @Test
    void editar_unLoteYaValidado_responde409() {
        when(ofertaRepository.buscarParaActualizar(21L))
                .thenReturn(Optional.of(lote(21L, caritas, EstadoOferta.VALIDADA, convocatoria(true))));

        assertThatThrownBy(() -> ofertaService.editar(21L, new OfertaEdicionRequest(1L, 250, "x", 2), sesionCaritas))
                .isInstanceOf(ReglaNegocioException.class);
    }

    @Test
    void editar_unLoteDeOtraOrganizacion_responde404() {
        when(ofertaRepository.buscarParaActualizar(21L))
                .thenReturn(Optional.of(lote(21L, cruzRoja, EstadoOferta.PENDIENTE, convocatoria(true))));

        assertThatThrownBy(() -> ofertaService.editar(21L, new OfertaEdicionRequest(1L, 250, "x", 2), sesionCaritas))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    void retirar_unLoteDeOtraOngDeLaMismaConjunta_responde403() {
        Convocatoria c = convocatoria(true);
        OfertaConjunta conjunta = new OfertaConjunta();
        conjunta.setId(4L);
        conjunta.setOrganizacionDuena(caritas);
        conjunta.setParticipantes(new LinkedHashSet<>(Set.of(cruzRoja)));
        Oferta deCruzRoja = lote(22L, cruzRoja, EstadoOferta.PENDIENTE, c);
        deCruzRoja.setOfertaConjunta(conjunta);
        when(ofertaRepository.buscarParaActualizar(22L)).thenReturn(Optional.of(deCruzRoja));

        assertThatThrownBy(() -> ofertaService.retirar(22L, sesionCaritas))
                .isInstanceOf(AccesoDenegadoException.class);
    }

    @Test
    void retirar_dejaElLoteRetiradoSinBorrarloYGuardaLaVersion() {
        Oferta oferta = lote(21L, caritas, EstadoOferta.PENDIENTE, convocatoria(true));
        when(ofertaRepository.buscarParaActualizar(21L)).thenReturn(Optional.of(oferta));
        when(ofertaRepository.save(any(Oferta.class))).then(returnsFirstArg());

        OfertaOngDto dto = ofertaService.retirar(21L, sesionCaritas);

        assertThat(dto.estado()).isEqualTo(EstadoOferta.RETIRADA);
        assertThat(dto.numeroVersion()).isEqualTo(3);
        assertThat(dto.puedeEditar()).isFalse();
        verify(ofertaRepository, never()).delete(any());
        verify(ofertaVersionRepository).save(any(OfertaVersion.class));
    }

    @Test
    void retirar_conLaVentanaCerrada_responde409() {
        when(ofertaRepository.buscarParaActualizar(21L))
                .thenReturn(Optional.of(lote(21L, caritas, EstadoOferta.PENDIENTE, convocatoria(false))));

        assertThatThrownBy(() -> ofertaService.retirar(21L, sesionCaritas)).isInstanceOf(ReglaNegocioException.class);
    }

    // ------------------------------------------------------------------
    // Finalización y cierre del proyecto
    // ------------------------------------------------------------------

    @Test
    void finalizar_unLoteNoAdjudicado_responde409() {
        when(ofertaRepository.buscarParaActualizar(21L))
                .thenReturn(Optional.of(lote(21L, caritas, EstadoOferta.VALIDADA, convocatoria(false))));

        assertThatThrownBy(() -> ofertaService.finalizar(21L, new FinalizacionRequest(null), sesionCaritas))
                .isInstanceOf(ReglaNegocioException.class);
    }

    @Test
    void finalizar_conOtrosAdjudicadosPendientes_noCierraElProyecto() {
        Convocatoria c = convocatoria(false);
        Proyecto proyecto = new Proyecto();
        proyecto.setEstado(Proyecto.EN_CURSO);
        emergencia.setProyecto(proyecto);
        Oferta propia = lote(21L, caritas, EstadoOferta.ADJUDICADA, c);
        Oferta otra = lote(22L, cruzRoja, EstadoOferta.ADJUDICADA, c);
        when(ofertaRepository.buscarParaActualizar(21L)).thenReturn(Optional.of(propia));
        when(ofertaRepository.save(any(Oferta.class))).then(returnsFirstArg());
        when(ofertaRepository.findByConvocatoriaId(7L)).thenReturn(List.of(propia, otra));

        OfertaOngDto dto = ofertaService.finalizar(21L, new FinalizacionRequest("Se entregaron 100 bidones."),
                sesionCaritas);

        assertThat(dto.estado()).isEqualTo(EstadoOferta.FINALIZADA);
        assertThat(dto.informeFinalizacion()).isEqualTo("Se entregaron 100 bidones.");
        assertThat(dto.fechaFinalizacion()).isNotNull();
        assertThat(proyecto.getEstado()).isEqualTo(Proyecto.EN_CURSO);
        verify(tareasFinalesBonita, never()).finalizarActividades(any());
    }

    @Test
    void finalizar_elUltimoLoteAdjudicado_terminaElProyectoYCompletaLaTareaDeBonita() {
        Convocatoria c = convocatoria(false);
        Proyecto proyecto = new Proyecto();
        proyecto.setEstado(Proyecto.EN_CURSO);
        emergencia.setProyecto(proyecto);
        Oferta propia = lote(21L, caritas, EstadoOferta.ADJUDICADA, c);
        Oferta yaFinalizada = lote(22L, cruzRoja, EstadoOferta.FINALIZADA, c);
        Oferta noSeleccionada = lote(23L, cruzRoja, EstadoOferta.NO_SELECCIONADA, c);
        when(ofertaRepository.buscarParaActualizar(21L)).thenReturn(Optional.of(propia));
        when(ofertaRepository.save(any(Oferta.class))).then(returnsFirstArg());
        when(ofertaRepository.findByConvocatoriaId(7L)).thenReturn(List.of(propia, yaFinalizada, noSeleccionada));

        ofertaService.finalizar(21L, null, sesionCaritas);

        assertThat(proyecto.getEstado()).isEqualTo(Proyecto.TERMINADO);
        assertThat(proyecto.getFechaFin()).isNotNull();
        verify(proyectoRepository).save(proyecto);
        verify(tareasFinalesBonita).finalizarActividades(100L);
    }

    @Test
    void obtener_unLoteDeOtraOrganizacionSinConjunta_responde404() {
        when(ofertaRepository.findById(22L))
                .thenReturn(Optional.of(lote(22L, cruzRoja, EstadoOferta.PENDIENTE, convocatoria(true))));

        assertThatThrownBy(() -> ofertaService.obtener(22L, sesionCaritas))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }
}
