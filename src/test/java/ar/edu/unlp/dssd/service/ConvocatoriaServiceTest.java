package ar.edu.unlp.dssd.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ar.edu.unlp.dssd.dto.ConvocatoriaRequest;
import ar.edu.unlp.dssd.dto.NecesidadRequest;
import ar.edu.unlp.dssd.dto.PublicacionRequest;
import ar.edu.unlp.dssd.exception.BonitaException;
import ar.edu.unlp.dssd.exception.RecursoNoEncontradoException;
import ar.edu.unlp.dssd.exception.ReglaNegocioException;
import ar.edu.unlp.dssd.model.Convocatoria;
import ar.edu.unlp.dssd.model.Emergencia;
import ar.edu.unlp.dssd.model.EstadoConvocatoria;
import ar.edu.unlp.dssd.model.Necesidad;
import ar.edu.unlp.dssd.repository.ConvocatoriaRepository;
import ar.edu.unlp.dssd.repository.EmergenciaRepository;
import ar.edu.unlp.dssd.repository.NecesidadRepository;

@ExtendWith(MockitoExtension.class)
class ConvocatoriaServiceTest {

    @Mock
    private ConvocatoriaRepository convocatoriaRepository;

    @Mock
    private EmergenciaRepository emergenciaRepository;

    @Mock
    private NecesidadRepository necesidadRepository;

    @Mock
    private NecesidadService necesidadService;

    @Mock
    private BonitaService bonitaService;

    @InjectMocks
    private ConvocatoriaService convocatoriaService;

    private static final Long ID_CONVOCATORIA = 1L;
    private static final Long ID_EMERGENCIA = 5L;
    private static final Long CASE_ID = 100L;

    private Emergencia emergencia(Long caseId) {
        Emergencia e = new Emergencia();
        e.setId(ID_EMERGENCIA);
        e.setCaseId(caseId);
        return e;
    }

    private Convocatoria convocatoria(EstadoConvocatoria estado, Emergencia emergencia) {
        Convocatoria c = new Convocatoria();
        c.setId(ID_CONVOCATORIA);
        c.setEstado(estado);
        c.setEmergencia(emergencia);
        return c;
    }

    private LocalDateTime futuro() {
        return LocalDateTime.now().plusDays(3);
    }

    private PublicacionRequest publicacionValida() {
        return new PublicacionRequest(null, futuro());
    }

    private ConvocatoriaRequest relevamiento() {
        return new ConvocatoriaRequest(List.of(
                new NecesidadRequest(3L, 5, "Frazadas"),
                new NecesidadRequest(4L, 10, "Agua")));
    }

    private void sinConvocatoriaViva() {
        when(convocatoriaRepository.existsByEmergenciaIdAndEstadoIn(ID_EMERGENCIA,
                EnumSet.of(EstadoConvocatoria.BORRADOR, EstadoConvocatoria.PUBLICADA))).thenReturn(false);
    }

    // ------------------------------------------------------------------
    // publicar
    // ------------------------------------------------------------------

    @Test
    void publicar_siLaConvocatoriaNoExiste_lanzaRecursoNoEncontrado() {
        when(convocatoriaRepository.findById(ID_CONVOCATORIA)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> convocatoriaService.publicar(ID_CONVOCATORIA, publicacionValida()))
                .isInstanceOf(RecursoNoEncontradoException.class);
        verifyNoInteractions(bonitaService);
    }

    @ParameterizedTest
    @EnumSource(value = EstadoConvocatoria.class, names = {"PUBLICADA", "CERRADA"})
    void publicar_siNoEstaEnBorrador_lanzaReglaNegocioYNoLlamaABonita(EstadoConvocatoria estado) {
        Convocatoria c = convocatoria(estado, emergencia(CASE_ID));
        when(convocatoriaRepository.findById(ID_CONVOCATORIA)).thenReturn(Optional.of(c));

        assertThatThrownBy(() -> convocatoriaService.publicar(ID_CONVOCATORIA, publicacionValida()))
                .isInstanceOf(ReglaNegocioException.class);

        verifyNoInteractions(bonitaService);
        verify(convocatoriaRepository, never()).save(any());
        assertThat(c.getEstado()).isEqualTo(estado);
    }

    @Test
    void publicar_sinNecesidades_lanzaReglaNegocioYNoLlamaABonita() {
        Convocatoria c = convocatoria(EstadoConvocatoria.BORRADOR, emergencia(CASE_ID));
        when(convocatoriaRepository.findById(ID_CONVOCATORIA)).thenReturn(Optional.of(c));
        when(necesidadRepository.findByConvocatoriaId(ID_CONVOCATORIA)).thenReturn(List.of());

        assertThatThrownBy(() -> convocatoriaService.publicar(ID_CONVOCATORIA, publicacionValida()))
                .isInstanceOf(ReglaNegocioException.class);

        verifyNoInteractions(bonitaService);
        verify(convocatoriaRepository, never()).save(any());
        assertThat(c.getEstado()).isEqualTo(EstadoConvocatoria.BORRADOR);
    }

    @Test
    void publicar_siLaEmergenciaNoTieneCaseId_lanzaReglaNegocioYNoLlamaABonita() {
        Convocatoria c = convocatoria(EstadoConvocatoria.BORRADOR, emergencia(null));
        when(convocatoriaRepository.findById(ID_CONVOCATORIA)).thenReturn(Optional.of(c));
        when(necesidadRepository.findByConvocatoriaId(ID_CONVOCATORIA)).thenReturn(List.of(new Necesidad()));

        assertThatThrownBy(() -> convocatoriaService.publicar(ID_CONVOCATORIA, publicacionValida()))
                .isInstanceOf(ReglaNegocioException.class);

        verifyNoInteractions(bonitaService);
        verify(convocatoriaRepository, never()).save(any());
    }

    @Test
    void publicar_siLaConvocatoriaNoTieneEmergencia_lanzaReglaNegocio() {
        Convocatoria c = convocatoria(EstadoConvocatoria.BORRADOR, null);
        when(convocatoriaRepository.findById(ID_CONVOCATORIA)).thenReturn(Optional.of(c));
        when(necesidadRepository.findByConvocatoriaId(ID_CONVOCATORIA)).thenReturn(List.of(new Necesidad()));

        assertThatThrownBy(() -> convocatoriaService.publicar(ID_CONVOCATORIA, publicacionValida()))
                .isInstanceOf(ReglaNegocioException.class);

        verifyNoInteractions(bonitaService);
    }

    @Test
    void publicar_sinFechaDeCierre_lanzaIllegalArgumentException() {
        Convocatoria c = convocatoria(EstadoConvocatoria.BORRADOR, emergencia(CASE_ID));
        when(convocatoriaRepository.findById(ID_CONVOCATORIA)).thenReturn(Optional.of(c));
        when(necesidadRepository.findByConvocatoriaId(ID_CONVOCATORIA)).thenReturn(List.of(new Necesidad()));

        assertThatThrownBy(() -> convocatoriaService.publicar(ID_CONVOCATORIA,
                new PublicacionRequest(null, null)))
                .isInstanceOf(IllegalArgumentException.class);

        verifyNoInteractions(bonitaService);
        verify(convocatoriaRepository, never()).save(any());
    }

    @Test
    void publicar_conFechaDeCierrePasada_lanzaIllegalArgumentException() {
        Convocatoria c = convocatoria(EstadoConvocatoria.BORRADOR, emergencia(CASE_ID));
        when(convocatoriaRepository.findById(ID_CONVOCATORIA)).thenReturn(Optional.of(c));
        when(necesidadRepository.findByConvocatoriaId(ID_CONVOCATORIA)).thenReturn(List.of(new Necesidad()));

        assertThatThrownBy(() -> convocatoriaService.publicar(ID_CONVOCATORIA,
                new PublicacionRequest(null, LocalDateTime.now().minusMinutes(1))))
                .isInstanceOf(IllegalArgumentException.class);

        verifyNoInteractions(bonitaService);
        verify(convocatoriaRepository, never()).save(any());
        assertThat(c.getEstado()).isEqualTo(EstadoConvocatoria.BORRADOR);
    }

    @Test
    void publicar_conAperturaPosteriorAlCierre_lanzaIllegalArgumentException() {
        Convocatoria c = convocatoria(EstadoConvocatoria.BORRADOR, emergencia(CASE_ID));
        when(convocatoriaRepository.findById(ID_CONVOCATORIA)).thenReturn(Optional.of(c));
        when(necesidadRepository.findByConvocatoriaId(ID_CONVOCATORIA)).thenReturn(List.of(new Necesidad()));
        LocalDateTime cierre = LocalDateTime.now().plusDays(1);

        assertThatThrownBy(() -> convocatoriaService.publicar(ID_CONVOCATORIA,
                new PublicacionRequest(cierre.plusDays(1), cierre)))
                .isInstanceOf(IllegalArgumentException.class);

        verifyNoInteractions(bonitaService);
        verify(convocatoriaRepository, never()).save(any());
    }

    @Test
    void publicar_casoFeliz_sinApertura_usaAhoraGuardaFechasLlamaABonitaYQuedaPublicada() {
        LocalDateTime cierre = futuro();
        Convocatoria c = convocatoria(EstadoConvocatoria.BORRADOR, emergencia(CASE_ID));
        when(convocatoriaRepository.findById(ID_CONVOCATORIA)).thenReturn(Optional.of(c));
        when(necesidadRepository.findByConvocatoriaId(ID_CONVOCATORIA)).thenReturn(List.of(new Necesidad()));
        when(convocatoriaRepository.save(any(Convocatoria.class))).then(returnsFirstArg());

        LocalDateTime antes = LocalDateTime.now();
        Convocatoria resultado = convocatoriaService.publicar(ID_CONVOCATORIA,
                new PublicacionRequest(null, cierre));
        LocalDateTime despues = LocalDateTime.now();

        // (caseId, idEmergencia, idConvocatoria, fechaCierre)
        verify(bonitaService).publicarConvocatoria(CASE_ID, ID_EMERGENCIA, ID_CONVOCATORIA, cierre);
        assertThat(resultado.getEstado()).isEqualTo(EstadoConvocatoria.PUBLICADA);
        assertThat(resultado.getFechaCierre()).isEqualTo(cierre);
        assertThat(resultado.getFechaApertura()).isBetween(antes, despues);
        verify(convocatoriaRepository).save(c);
    }

    @Test
    void publicar_conAperturaExplicita_laRespeta() {
        Convocatoria c = convocatoria(EstadoConvocatoria.BORRADOR, emergencia(CASE_ID));
        when(convocatoriaRepository.findById(ID_CONVOCATORIA)).thenReturn(Optional.of(c));
        when(necesidadRepository.findByConvocatoriaId(ID_CONVOCATORIA)).thenReturn(List.of(new Necesidad()));
        when(convocatoriaRepository.save(any(Convocatoria.class))).then(returnsFirstArg());
        LocalDateTime apertura = LocalDateTime.now().plusHours(1);
        LocalDateTime cierre = LocalDateTime.now().plusDays(2);

        Convocatoria resultado = convocatoriaService.publicar(ID_CONVOCATORIA,
                new PublicacionRequest(apertura, cierre));

        assertThat(resultado.getFechaApertura()).isEqualTo(apertura);
        assertThat(resultado.getFechaCierre()).isEqualTo(cierre);
    }

    @Test
    void publicar_siBonitaFalla_propagaLaExcepcion_yLaConvocatoriaNoSeGuardaComoPublicada() {
        Convocatoria c = convocatoria(EstadoConvocatoria.BORRADOR, emergencia(CASE_ID));
        when(convocatoriaRepository.findById(ID_CONVOCATORIA)).thenReturn(Optional.of(c));
        when(necesidadRepository.findByConvocatoriaId(ID_CONVOCATORIA)).thenReturn(List.of(new Necesidad()));
        org.mockito.Mockito.doThrow(new BonitaException("Bonita caido"))
                .when(bonitaService).publicarConvocatoria(any(), any(), any(), any());

        assertThatThrownBy(() -> convocatoriaService.publicar(ID_CONVOCATORIA, publicacionValida()))
                .isInstanceOf(BonitaException.class);

        assertThat(c.getEstado()).isEqualTo(EstadoConvocatoria.BORRADOR);
        verify(convocatoriaRepository, never()).save(any());
    }

    // ------------------------------------------------------------------
    // crearRelevamiento
    // ------------------------------------------------------------------

    @Test
    void crearRelevamiento_siLaEmergenciaNoExiste_lanzaRecursoNoEncontrado() {
        when(emergenciaRepository.findById(ID_EMERGENCIA)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> convocatoriaService.crearRelevamiento(ID_EMERGENCIA, relevamiento()))
                .isInstanceOf(RecursoNoEncontradoException.class);
        verify(convocatoriaRepository, never()).save(any());
        verifyNoInteractions(bonitaService);
    }

    @Test
    void crearRelevamiento_siYaHayUnaBorradorOPublicada_lanzaReglaNegocio() {
        when(emergenciaRepository.findById(ID_EMERGENCIA)).thenReturn(Optional.of(emergencia(CASE_ID)));
        when(convocatoriaRepository.existsByEmergenciaIdAndEstadoIn(ID_EMERGENCIA,
                EnumSet.of(EstadoConvocatoria.BORRADOR, EstadoConvocatoria.PUBLICADA))).thenReturn(true);

        assertThatThrownBy(() -> convocatoriaService.crearRelevamiento(ID_EMERGENCIA, relevamiento()))
                .isInstanceOf(ReglaNegocioException.class);
        verify(convocatoriaRepository, never()).save(any());
        verifyNoInteractions(bonitaService);
    }

    @Test
    void crearRelevamiento_siLaEmergenciaNoTieneCaseId_lanzaReglaNegocio() {
        when(emergenciaRepository.findById(ID_EMERGENCIA)).thenReturn(Optional.of(emergencia(null)));
        sinConvocatoriaViva();

        assertThatThrownBy(() -> convocatoriaService.crearRelevamiento(ID_EMERGENCIA, relevamiento()))
                .isInstanceOf(ReglaNegocioException.class);
        verify(convocatoriaRepository, never()).save(any());
        verifyNoInteractions(bonitaService);
    }

    @Test
    void crearRelevamiento_conNecesidadesNulas_lanzaIllegalArgumentException() {
        when(emergenciaRepository.findById(ID_EMERGENCIA)).thenReturn(Optional.of(emergencia(CASE_ID)));
        sinConvocatoriaViva();

        assertThatThrownBy(() -> convocatoriaService.crearRelevamiento(ID_EMERGENCIA,
                new ConvocatoriaRequest(null)))
                .isInstanceOf(IllegalArgumentException.class);
        verify(convocatoriaRepository, never()).save(any());
        verifyNoInteractions(bonitaService);
    }

    @Test
    void crearRelevamiento_conNecesidadesVacias_lanzaIllegalArgumentException() {
        when(emergenciaRepository.findById(ID_EMERGENCIA)).thenReturn(Optional.of(emergencia(CASE_ID)));
        sinConvocatoriaViva();

        assertThatThrownBy(() -> convocatoriaService.crearRelevamiento(ID_EMERGENCIA,
                new ConvocatoriaRequest(List.of())))
                .isInstanceOf(IllegalArgumentException.class);
        verify(convocatoriaRepository, never()).save(any());
        verifyNoInteractions(bonitaService);
    }

    @Test
    void crearRelevamiento_siUnLoteEsInvalido_propagaLaExcepcionYNoLlamaABonita() {
        when(emergenciaRepository.findById(ID_EMERGENCIA)).thenReturn(Optional.of(emergencia(CASE_ID)));
        sinConvocatoriaViva();
        when(convocatoriaRepository.save(any(Convocatoria.class))).then(returnsFirstArg());
        when(necesidadService.crearLote(any(Convocatoria.class), any(NecesidadRequest.class)))
                .thenThrow(new IllegalArgumentException("La cantidad debe ser un entero mayor o igual a 1"));

        assertThatThrownBy(() -> convocatoriaService.crearRelevamiento(ID_EMERGENCIA, relevamiento()))
                .isInstanceOf(IllegalArgumentException.class);
        // El rollback de la transaccion deshace el save de la convocatoria; Bonita no se toca
        verifyNoInteractions(bonitaService);
    }

    @Test
    void crearRelevamiento_casoFeliz_guardaBorradorSinFechasConSusLotesYCompletaElRelevamiento() {
        Emergencia emergencia = emergencia(CASE_ID);
        when(emergenciaRepository.findById(ID_EMERGENCIA)).thenReturn(Optional.of(emergencia));
        sinConvocatoriaViva();
        when(convocatoriaRepository.save(any(Convocatoria.class))).then(returnsFirstArg());
        ConvocatoriaRequest request = relevamiento();

        Convocatoria resultado = convocatoriaService.crearRelevamiento(ID_EMERGENCIA, request);

        ArgumentCaptor<Convocatoria> captor = ArgumentCaptor.forClass(Convocatoria.class);
        verify(convocatoriaRepository).save(captor.capture());
        Convocatoria guardada = captor.getValue();
        assertThat(guardada.getEstado()).isEqualTo(EstadoConvocatoria.BORRADOR);
        assertThat(guardada.getEmergencia()).isSameAs(emergencia);
        assertThat(guardada.getFechaCreacion()).isEqualTo(LocalDate.now());
        assertThat(guardada.getFechaApertura()).isNull();
        assertThat(guardada.getFechaCierre()).isNull();
        assertThat(resultado).isSameAs(guardada);

        verify(necesidadService, times(2)).crearLote(any(Convocatoria.class), any(NecesidadRequest.class));
        verify(necesidadService).crearLote(guardada, request.necesidades().get(0));
        verify(necesidadService).crearLote(guardada, request.necesidades().get(1));
        verify(bonitaService).completarRelevamiento(CASE_ID);
    }

    @Test
    void crearRelevamiento_siBonitaFalla_propagaLaExcepcion() {
        when(emergenciaRepository.findById(ID_EMERGENCIA)).thenReturn(Optional.of(emergencia(CASE_ID)));
        sinConvocatoriaViva();
        when(convocatoriaRepository.save(any(Convocatoria.class))).then(returnsFirstArg());
        org.mockito.Mockito.doThrow(new BonitaException("Bonita caido"))
                .when(bonitaService).completarRelevamiento(CASE_ID);

        // La excepcion hace que @Transactional deshaga la convocatoria y los lotes
        assertThatThrownBy(() -> convocatoriaService.crearRelevamiento(ID_EMERGENCIA, relevamiento()))
                .isInstanceOf(BonitaException.class);
    }
}
