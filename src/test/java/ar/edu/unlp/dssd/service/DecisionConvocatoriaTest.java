package ar.edu.unlp.dssd.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ar.edu.unlp.dssd.dto.ConvocatoriaResumenDto;
import ar.edu.unlp.dssd.dto.DecisionConvocatoriaRequest;
import ar.edu.unlp.dssd.dto.NecesidadRequest;
import ar.edu.unlp.dssd.exception.ReglaNegocioException;
import ar.edu.unlp.dssd.model.AccionConvocatoria;
import ar.edu.unlp.dssd.model.Convocatoria;
import ar.edu.unlp.dssd.model.Emergencia;
import ar.edu.unlp.dssd.model.EstadoConvocatoria;
import ar.edu.unlp.dssd.model.EstadoOferta;
import ar.edu.unlp.dssd.model.EtapaEmergencia;
import ar.edu.unlp.dssd.model.Necesidad;
import ar.edu.unlp.dssd.model.Oferta;
import ar.edu.unlp.dssd.model.TipoNotificacion;
import ar.edu.unlp.dssd.repository.ConvocatoriaRepository;
import ar.edu.unlp.dssd.repository.EmergenciaRepository;
import ar.edu.unlp.dssd.repository.NecesidadRepository;
import ar.edu.unlp.dssd.repository.OfertaRepository;
import ar.edu.unlp.dssd.repository.OfertaVersionRepository;
import ar.edu.unlp.dssd.service.EtapaEmergenciaService.EstadoEmergencia;

/** [Entrega final] Decisión del coordinador: reabrir, reformular o continuar con cobertura parcial */
@ExtendWith(MockitoExtension.class)
class DecisionConvocatoriaTest {

    @Mock private ConvocatoriaRepository convocatoriaRepository;
    @Mock private EmergenciaRepository emergenciaRepository;
    @Mock private NecesidadRepository necesidadRepository;
    @Mock private OfertaRepository ofertaRepository;
    @Mock private OfertaVersionRepository ofertaVersionRepository;
    @Mock private NecesidadService necesidadService;
    @Mock private BonitaService bonitaService;
    @Mock private IntegracionService integracionService;
    @Mock private NotificacionService notificacionService;
    @Mock private EtapaEmergenciaService etapaEmergenciaService;
    @InjectMocks private ConvocatoriaService convocatoriaService;

    private final Emergencia emergencia = new Emergencia();
    private final Convocatoria convocatoria = new Convocatoria();
    private final Oferta validada = oferta(EstadoOferta.VALIDADA);
    private final Oferta noHabilitada = oferta(EstadoOferta.NO_HABILITADA);
    private final Oferta retirada = oferta(EstadoOferta.RETIRADA);

    private static Oferta oferta(EstadoOferta estado) {
        Oferta o = new Oferta();
        o.setEstado(estado);
        o.setNumeroVersion(1);
        o.setNivelAprobacion("ALTO");
        return o;
    }

    private void enEtapa(EtapaEmergencia etapa) {
        emergencia.setId(5L);
        convocatoria.setId(7L);
        convocatoria.setEstado(EstadoConvocatoria.PUBLICADA);
        convocatoria.setRonda(1);
        convocatoria.setEmergencia(emergencia);
        when(convocatoriaRepository.findById(7L)).thenReturn(Optional.of(convocatoria));
        List<Oferta> lotes = List.of(validada, noHabilitada, retirada);
        when(etapaEmergenciaService.evaluar(emergencia)).thenReturn(new EstadoEmergencia(etapa, convocatoria,
                List.of(), lotes, List.of(validada, noHabilitada), List.of()));
    }

    @Test
    void decidir_fueraDeRequiereDecision_responde409() {
        enEtapa(EtapaEmergencia.EN_SELECCION);

        assertThatThrownBy(() -> convocatoriaService.decidir(7L,
                new DecisionConvocatoriaRequest(AccionConvocatoria.CONTINUAR_PARCIAL, null, null)))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessage("La convocatoria no está esperando una decisión.");
    }

    @Test
    void reabrir_vuelveLosLotesEvaluadosAPendienteSubeLaRondaYNotifica() {
        enEtapa(EtapaEmergencia.REQUIERE_DECISION);
        when(convocatoriaRepository.save(any(Convocatoria.class))).then(returnsFirstArg());
        LocalDateTime cierre = LocalDateTime.now().plusDays(2);

        ConvocatoriaResumenDto dto = convocatoriaService.decidir(7L,
                new DecisionConvocatoriaRequest(AccionConvocatoria.REABRIR, cierre, null));

        assertThat(dto.ronda()).isEqualTo(2);
        assertThat(dto.fechaCierre()).isEqualTo(cierre);
        assertThat(validada.getEstado()).isEqualTo(EstadoOferta.PENDIENTE);
        assertThat(validada.getNivelAprobacion()).isNull();
        assertThat(noHabilitada.getEstado()).isEqualTo(EstadoOferta.PENDIENTE);
        assertThat(retirada.getEstado()).isEqualTo(EstadoOferta.RETIRADA);
        verify(notificacionService).notificarConvocatoria(convocatoria, TipoNotificacion.CONVOCATORIA_REABIERTA);
    }

    @Test
    void reabrir_sinFechaFutura_responde400() {
        enEtapa(EtapaEmergencia.REQUIERE_DECISION);

        assertThatThrownBy(() -> convocatoriaService.decidir(7L, new DecisionConvocatoriaRequest(
                AccionConvocatoria.REABRIR, LocalDateTime.now().minusMinutes(1), null)))
                .isInstanceOf(IllegalArgumentException.class);
        verify(notificacionService, never()).notificarConvocatoria(any(), any());
    }

    @Test
    void reformular_reemplazaLosLotesPedidos() {
        enEtapa(EtapaEmergencia.REQUIERE_DECISION);
        List<Necesidad> anteriores = List.of(new Necesidad());
        when(necesidadRepository.findByConvocatoriaId(7L)).thenReturn(anteriores);
        when(convocatoriaRepository.save(any(Convocatoria.class))).then(returnsFirstArg());
        NecesidadRequest nuevo = new NecesidadRequest(1L, 300, "Bidones");

        convocatoriaService.decidir(7L, new DecisionConvocatoriaRequest(AccionConvocatoria.REFORMULAR,
                LocalDateTime.now().plusDays(1), List.of(nuevo)));

        verify(necesidadRepository).deleteAll(anteriores);
        verify(necesidadService).crearLote(convocatoria, nuevo);
    }

    @Test
    void reformular_sinLotes_responde400() {
        enEtapa(EtapaEmergencia.REQUIERE_DECISION);

        assertThatThrownBy(() -> convocatoriaService.decidir(7L, new DecisionConvocatoriaRequest(
                AccionConvocatoria.REFORMULAR, LocalDateTime.now().plusDays(1), List.of())))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void continuarParcial_aceptaLaCoberturaParcialSinTocarLosLotes() {
        enEtapa(EtapaEmergencia.REQUIERE_DECISION);
        when(convocatoriaRepository.save(any(Convocatoria.class))).then(returnsFirstArg());

        ConvocatoriaResumenDto dto = convocatoriaService.decidir(7L,
                new DecisionConvocatoriaRequest(AccionConvocatoria.CONTINUAR_PARCIAL, null, null));

        assertThat(dto.coberturaParcialAceptada()).isTrue();
        assertThat(validada.getEstado()).isEqualTo(EstadoOferta.VALIDADA);
        verify(notificacionService, never()).notificarConvocatoria(any(), any());
    }
}
