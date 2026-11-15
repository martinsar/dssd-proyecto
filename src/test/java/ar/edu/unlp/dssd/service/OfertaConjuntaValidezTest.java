package ar.edu.unlp.dssd.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ar.edu.unlp.dssd.dto.ConfirmacionAdjudicacionResponse;
import ar.edu.unlp.dssd.dto.OfertasConsolidadasDto;
import ar.edu.unlp.dssd.dto.ResultadoValidacionRequest;
import ar.edu.unlp.dssd.dto.ValidacionesRequest;
import ar.edu.unlp.dssd.exception.ReglaNegocioException;
import ar.edu.unlp.dssd.model.Convocatoria;
import ar.edu.unlp.dssd.model.Emergencia;
import ar.edu.unlp.dssd.model.EstadoOferta;
import ar.edu.unlp.dssd.model.Necesidad;
import ar.edu.unlp.dssd.model.Oferta;
import ar.edu.unlp.dssd.model.OfertaConjunta;
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

/** [Entrega final] Regla 3 (validez de ofertas conjuntas) y confirmación de la adjudicación */
@ExtendWith(MockitoExtension.class)
class OfertaConjuntaValidezTest {

    @Mock private EmergenciaRepository emergenciaRepository;
    @Mock private ConvocatoriaRepository convocatoriaRepository;
    @Mock private NecesidadRepository necesidadRepository;
    @Mock private OfertaRepository ofertaRepository;
    @Mock private OfertaVersionRepository ofertaVersionRepository;
    @Mock private ProyectoRepository proyectoRepository;
    @Mock private NotificacionService notificacionService;
    @InjectMocks private IntegracionService integracionService;

    private final TipoRecurso agua = tipo();
    private final Organizacion caritas = organizacion(1L, "Cáritas");
    private final Organizacion cruzRoja = organizacion(2L, "Cruz Roja Argentina");
    private final Organizacion bomberos = organizacion(3L, "Bomberos Voluntarios La Plata");
    private final Emergencia emergencia = emergencia();
    private final Convocatoria convocatoria = convocatoria();

    private static TipoRecurso tipo() {
        TipoRecurso t = new TipoRecurso();
        t.setId(1L);
        t.setCodigo("AGUA");
        t.setNombre("Agua potable");
        return t;
    }

    private static Organizacion organizacion(Long id, String nombre) {
        Organizacion o = new Organizacion();
        o.setId(id);
        o.setNombre(nombre);
        return o;
    }

    private static Emergencia emergencia() {
        Emergencia e = new Emergencia();
        e.setId(5L);
        e.setTipoDesastre("Inundación");
        e.setZonaAfectada("Barrio San Carlos");
        return e;
    }

    private Convocatoria convocatoria() {
        Convocatoria c = new Convocatoria();
        c.setId(9L);
        c.setEmergencia(emergencia);
        c.setFechaCierre(LocalDateTime.now().minusHours(1));
        return c;
    }

    private OfertaConjunta conjunta(Long id, Organizacion duena, Organizacion... participantes) {
        OfertaConjunta c = new OfertaConjunta();
        c.setId(id);
        c.setConvocatoria(convocatoria);
        c.setOrganizacionDuena(duena);
        c.setParticipantes(new LinkedHashSet<>(Set.of(participantes)));
        return c;
    }

    private Oferta lote(Long id, Organizacion organizacion, int cantidad, EstadoOferta estado, OfertaConjunta conjunta) {
        Oferta o = new Oferta();
        o.setId(id);
        o.setConvocatoria(convocatoria);
        o.setOrganizacion(organizacion);
        o.setTipoRecurso(agua);
        o.setCantidad(cantidad);
        o.setEstado(estado);
        o.setOfertaConjunta(conjunta);
        o.setNumeroVersion(1);
        return o;
    }

    @Test
    void conjunta_esValidaSoloSiTodasLasAsociadasTienenUnLoteNoRetirado() {
        OfertaConjunta conjunta = conjunta(4L, caritas, cruzRoja);
        Oferta deCaritas = lote(21L, caritas, 100, EstadoOferta.PENDIENTE, conjunta);
        Oferta deCruzRoja = lote(22L, cruzRoja, 100, EstadoOferta.PENDIENTE, conjunta);

        when(ofertaRepository.findByOfertaConjuntaId(4L)).thenReturn(List.of(deCaritas));
        assertThat(integracionService.conjuntaEsValida(conjunta)).isFalse();
        assertThat(IntegracionService.faltantes(conjunta, List.of(deCaritas)))
                .extracting(Organizacion::getNombre).containsExactly("Cruz Roja Argentina");

        when(ofertaRepository.findByOfertaConjuntaId(4L)).thenReturn(List.of(deCaritas, deCruzRoja));
        assertThat(integracionService.conjuntaEsValida(conjunta)).isTrue();
        assertThat(integracionService.ofertaEsValida(deCaritas)).isTrue();
    }

    @Test
    void conjunta_dejaDeSerValidaSiUnaAsociadaRetiraSuUnicoLote() {
        OfertaConjunta conjunta = conjunta(4L, caritas, cruzRoja);
        Oferta deCaritas = lote(21L, caritas, 100, EstadoOferta.PENDIENTE, conjunta);
        Oferta retirada = lote(22L, cruzRoja, 100, EstadoOferta.RETIRADA, conjunta);
        when(ofertaRepository.findByOfertaConjuntaId(4L)).thenReturn(List.of(deCaritas, retirada));

        assertThat(integracionService.ofertaEsValida(deCaritas)).isFalse();
        assertThat(integracionService.ofertaEsValida(retirada)).isFalse();
    }

    @Test
    void ofertaIndividual_siempreEsValidaSalvoRetirada() {
        assertThat(integracionService.ofertaEsValida(lote(30L, bomberos, 10, EstadoOferta.PENDIENTE, null))).isTrue();
        assertThat(integracionService.ofertaEsValida(lote(31L, bomberos, 10, EstadoOferta.RETIRADA, null))).isFalse();
    }

    @Test
    void consolidado_excluyeLotesDeConjuntasNoValidasYRetirados() {
        OfertaConjunta incompleta = conjunta(4L, caritas, cruzRoja);
        Oferta deIncompleta = lote(21L, caritas, 400, EstadoOferta.PENDIENTE, incompleta);
        Oferta individual = lote(22L, bomberos, 200, EstadoOferta.PENDIENTE, null);
        Oferta retirada = lote(23L, bomberos, 900, EstadoOferta.RETIRADA, null);
        Necesidad necesidad = new Necesidad();
        necesidad.setId(3L);
        necesidad.setTipoRecurso(agua);
        necesidad.setCantidad(500);
        when(emergenciaRepository.existsById(5L)).thenReturn(true);
        when(convocatoriaRepository.findByEmergenciaIdOrderByIdDesc(5L)).thenReturn(List.of(convocatoria));
        when(necesidadRepository.findByConvocatoriaId(9L)).thenReturn(List.of(necesidad));
        when(ofertaRepository.findByConvocatoriaId(9L)).thenReturn(List.of(deIncompleta, individual, retirada));
        when(ofertaRepository.findByOfertaConjuntaId(4L)).thenReturn(List.of(deIncompleta));

        OfertasConsolidadasDto dto = integracionService.obtenerConsolidado(5L, null);

        assertThat(dto.ofertas()).extracting(o -> o.ofertaId()).containsExactly(22L);
        assertThat(dto.lotes().get(0).cantidadOfertada()).isEqualTo(200L);
        assertThat(dto.coberturaCompleta()).isFalse();
    }

    @Test
    void validaciones_rechazanLotesDeConjuntasNoValidas() {
        OfertaConjunta incompleta = conjunta(4L, caritas, cruzRoja);
        Oferta deIncompleta = lote(21L, caritas, 400, EstadoOferta.PENDIENTE, incompleta);
        when(emergenciaRepository.existsById(5L)).thenReturn(true);
        when(ofertaRepository.findById(21L)).thenReturn(Optional.of(deIncompleta));
        when(ofertaRepository.findByOfertaConjuntaId(4L)).thenReturn(List.of(deIncompleta));

        assertThatThrownBy(() -> integracionService.registrarValidaciones(5L,
                new ValidacionesRequest(List.of(new ResultadoValidacionRequest(21L, true)))))
                .isInstanceOf(ReglaNegocioException.class);
        verify(ofertaRepository, never()).saveAll(any());
    }

    @Test
    void confirmarAdjudicacion_adjudicaNotificaYCreaElProyecto() {
        Oferta seleccionada = lote(21L, caritas, 300, EstadoOferta.SELECCIONADA, null);
        Oferta noSeleccionada = lote(22L, cruzRoja, 300, EstadoOferta.NO_SELECCIONADA, null);
        when(emergenciaRepository.findById(5L)).thenReturn(Optional.of(emergencia));
        when(convocatoriaRepository.findByEmergenciaIdOrderByIdDesc(5L)).thenReturn(List.of(convocatoria));
        when(ofertaRepository.findByConvocatoriaId(9L)).thenReturn(List.of(seleccionada, noSeleccionada));
        when(notificacionService.notificarResultado(any(), any())).thenReturn(true);
        when(proyectoRepository.save(any(Proyecto.class))).then(returnsFirstArg());

        ConfirmacionAdjudicacionResponse respuesta = integracionService.confirmarAdjudicacion(5L);

        assertThat(respuesta.adjudicadas()).isEqualTo(1);
        assertThat(respuesta.notificaciones()).isEqualTo(2);
        assertThat(seleccionada.getEstado()).isEqualTo(EstadoOferta.ADJUDICADA);
        assertThat(seleccionada.getFechaAdjudicacion()).isNotNull();
        assertThat(seleccionada.getNumeroVersion()).isEqualTo(2);
        verify(notificacionService).notificarResultado(seleccionada, TipoNotificacion.OFERTA_ADJUDICADA);
        verify(notificacionService).notificarResultado(noSeleccionada, TipoNotificacion.OFERTA_NO_SELECCIONADA);
        assertThat(emergencia.getProyecto()).isNotNull();
        assertThat(emergencia.getProyecto().getEstado()).isEqualTo(Proyecto.EN_CURSO);
        assertThat(emergencia.getProyecto().getNombre()).isEqualTo("Emergencia N° 5 · Inundación · Barrio San Carlos");
    }

    @Test
    void confirmarAdjudicacion_esIdempotente() {
        Proyecto existente = new Proyecto();
        existente.setEstado(Proyecto.EN_CURSO);
        emergencia.setProyecto(existente);
        Oferta adjudicada = lote(21L, caritas, 300, EstadoOferta.ADJUDICADA, null);
        when(emergenciaRepository.findById(5L)).thenReturn(Optional.of(emergencia));
        when(convocatoriaRepository.findByEmergenciaIdOrderByIdDesc(5L)).thenReturn(List.of(convocatoria));
        when(ofertaRepository.findByConvocatoriaId(9L)).thenReturn(List.of(adjudicada));
        when(notificacionService.notificarResultado(adjudicada, TipoNotificacion.OFERTA_ADJUDICADA)).thenReturn(false);

        ConfirmacionAdjudicacionResponse respuesta = integracionService.confirmarAdjudicacion(5L);

        assertThat(respuesta.adjudicadas()).isZero();
        assertThat(respuesta.notificaciones()).isZero();
        assertThat(adjudicada.getNumeroVersion()).isEqualTo(1);
        verify(proyectoRepository, never()).save(any());
    }
}
