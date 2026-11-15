package ar.edu.unlp.dssd.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ar.edu.unlp.dssd.dto.AdjudicacionRequest;
import ar.edu.unlp.dssd.dto.AdjudicacionResponse;
import ar.edu.unlp.dssd.exception.RecursoNoEncontradoException;
import ar.edu.unlp.dssd.exception.ReglaNegocioException;
import ar.edu.unlp.dssd.model.Convocatoria;
import ar.edu.unlp.dssd.model.Emergencia;
import ar.edu.unlp.dssd.model.EstadoOferta;
import ar.edu.unlp.dssd.model.EtapaEmergencia;
import ar.edu.unlp.dssd.model.Oferta;
import ar.edu.unlp.dssd.repository.EmergenciaRepository;
import ar.edu.unlp.dssd.repository.OfertaRepository;
import ar.edu.unlp.dssd.repository.OfertaVersionRepository;
import ar.edu.unlp.dssd.repository.UsuarioRepository;
import ar.edu.unlp.dssd.seguridad.Rol;
import ar.edu.unlp.dssd.seguridad.SesionUsuario;
import ar.edu.unlp.dssd.service.EtapaEmergenciaService.EstadoEmergencia;

/** [Entrega final] Regla 8: selección del municipio con integracion-completa en false y en true */
@ExtendWith(MockitoExtension.class)
class MunicipioServiceTest {

    @Mock private EmergenciaRepository emergenciaRepository;
    @Mock private OfertaRepository ofertaRepository;
    @Mock private OfertaVersionRepository ofertaVersionRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private EtapaEmergenciaService etapaService;
    @Mock private IntegracionService integracionService;
    @Mock private TareasFinalesBonitaService tareasFinalesBonita;
    @InjectMocks private MunicipioService municipioService;

    private final SesionUsuario sesionMunicipio = new SesionUsuario("t", 1L, "olmos", "Olga", "Olmos",
            Rol.OPERADOR_MUNICIPAL, null, null, null, Instant.now());
    private final Emergencia emergencia = new Emergencia();
    private final Oferta elegida = oferta(21L, EstadoOferta.VALIDADA);
    private final Oferta otra = oferta(22L, EstadoOferta.VALIDADA);
    private final Oferta rechazada = oferta(23L, EstadoOferta.NO_HABILITADA);

    private static Oferta oferta(Long id, EstadoOferta estado) {
        Oferta o = new Oferta();
        o.setId(id);
        o.setEstado(estado);
        o.setNumeroVersion(1);
        return o;
    }

    private EstadoEmergencia estado(EtapaEmergencia etapa) {
        List<Oferta> lotes = List.of(elegida, otra, rechazada);
        return new EstadoEmergencia(etapa, new Convocatoria(), List.of(), lotes, lotes, List.of());
    }

    private void enEtapa(EtapaEmergencia antes, EtapaEmergencia despues) {
        emergencia.setId(5L);
        emergencia.setCaseId(100L);
        when(emergenciaRepository.findByIdAndCreadoPorId(5L, 1L)).thenReturn(Optional.of(emergencia));
        when(etapaService.evaluar(emergencia)).thenReturn(estado(antes), estado(despues));
    }

    @Test
    void adjudicar_sinIntegracionCompleta_seleccionaYConfirmaEnElMomento() {
        enEtapa(EtapaEmergencia.EN_SELECCION, EtapaEmergencia.ADJUDICADA);
        when(tareasFinalesBonita.integracionCompleta()).thenReturn(false);

        AdjudicacionResponse respuesta = municipioService.adjudicar(5L, new AdjudicacionRequest(List.of(21L)),
                sesionMunicipio);

        assertThat(respuesta.seleccionadas()).isEqualTo(1);
        assertThat(respuesta.noSeleccionadas()).isEqualTo(1);
        assertThat(respuesta.etapa()).isEqualTo(EtapaEmergencia.ADJUDICADA);
        assertThat(elegida.getEstado()).isEqualTo(EstadoOferta.SELECCIONADA);
        assertThat(otra.getEstado()).isEqualTo(EstadoOferta.NO_SELECCIONADA);
        assertThat(rechazada.getEstado()).isEqualTo(EstadoOferta.NO_HABILITADA);
        verify(tareasFinalesBonita).seleccionarOfertas(100L);
        verify(integracionService).confirmarAdjudicacion(5L);
    }

    @Test
    void adjudicar_conIntegracionCompleta_dejaSeleccionadasYEsperaABonita() {
        enEtapa(EtapaEmergencia.EN_SELECCION, EtapaEmergencia.ADJUDICADA);
        when(tareasFinalesBonita.integracionCompleta()).thenReturn(true);

        municipioService.adjudicar(5L, new AdjudicacionRequest(List.of(21L, 22L)), sesionMunicipio);

        assertThat(elegida.getEstado()).isEqualTo(EstadoOferta.SELECCIONADA);
        assertThat(otra.getEstado()).isEqualTo(EstadoOferta.SELECCIONADA);
        verify(tareasFinalesBonita).seleccionarOfertas(100L);
        verify(integracionService, never()).confirmarAdjudicacion(any());
    }

    @Test
    void adjudicar_conIntegracionCompletaYSinLaTarea_propagaEl409() {
        enEtapaSoloAntes();
        org.mockito.Mockito.doThrow(new ReglaNegocioException("El proceso todavía no habilitó la selección"))
                .when(tareasFinalesBonita).seleccionarOfertas(100L);

        assertThatThrownBy(() -> municipioService.adjudicar(5L, new AdjudicacionRequest(List.of(21L)), sesionMunicipio))
                .isInstanceOf(ReglaNegocioException.class);
        verify(integracionService, never()).confirmarAdjudicacion(any());
    }

    @Test
    void adjudicar_fueraDeEnSeleccion_responde409() {
        emergencia.setId(5L);
        when(emergenciaRepository.findByIdAndCreadoPorId(5L, 1L)).thenReturn(Optional.of(emergencia));
        when(etapaService.evaluar(emergencia)).thenReturn(estado(EtapaEmergencia.EN_VALIDACION));

        assertThatThrownBy(() -> municipioService.adjudicar(5L, new AdjudicacionRequest(List.of(21L)), sesionMunicipio))
                .isInstanceOf(ReglaNegocioException.class);
        verify(ofertaRepository, never()).save(any());
    }

    @Test
    void adjudicar_unLoteNoValidado_responde400() {
        enEtapaSoloAntes();

        assertThatThrownBy(() -> municipioService.adjudicar(5L, new AdjudicacionRequest(List.of(23L)), sesionMunicipio))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> municipioService.adjudicar(5L, new AdjudicacionRequest(List.of()), sesionMunicipio))
                .isInstanceOf(IllegalArgumentException.class);
        verify(ofertaRepository, never()).save(any());
    }

    @Test
    void detalle_deUnaEmergenciaAjena_responde404() {
        when(emergenciaRepository.findByIdAndCreadoPorId(9L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> municipioService.detalle(9L, sesionMunicipio))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    void ofertasValidadas_noIncluyeLotesNoHabilitados() {
        emergencia.setId(5L);
        when(emergenciaRepository.findByIdAndCreadoPorId(5L, 1L)).thenReturn(Optional.of(emergencia));
        when(etapaService.evaluar(emergencia)).thenReturn(estado(EtapaEmergencia.EN_SELECCION));

        assertThat(municipioService.ofertasValidadas(5L, sesionMunicipio))
                .extracting(o -> o.id()).containsExactly(21L, 22L);
    }

    private void enEtapaSoloAntes() {
        emergencia.setId(5L);
        emergencia.setCaseId(100L);
        when(emergenciaRepository.findByIdAndCreadoPorId(5L, 1L)).thenReturn(Optional.of(emergencia));
        when(etapaService.evaluar(emergencia)).thenReturn(estado(EtapaEmergencia.EN_SELECCION));
    }
}
