package ar.edu.unlp.dssd.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ar.edu.unlp.dssd.dto.ActividadUsuariosBonitaDto;
import ar.edu.unlp.dssd.dto.AuditoriaEmergenciaDto;
import ar.edu.unlp.dssd.dto.CuellosDeBotellaBonitaDto;
import ar.edu.unlp.dssd.dto.EstadosCasosBonitaDto;
import ar.edu.unlp.dssd.dto.FuenteDatos;
import ar.edu.unlp.dssd.dto.IndicadoresDto;
import ar.edu.unlp.dssd.dto.LoteDetalleDto;
import ar.edu.unlp.dssd.dto.SerieMensualDto;
import ar.edu.unlp.dssd.dto.TiempoEtapaDto;
import ar.edu.unlp.dssd.dto.TiemposEsperaEjecucionBonitaDto;
import ar.edu.unlp.dssd.dto.TiemposEtapasDto;
import ar.edu.unlp.dssd.dto.TiemposResolucionCruzadosDto;
import ar.edu.unlp.dssd.exception.RecursoNoEncontradoException;
import ar.edu.unlp.dssd.model.Convocatoria;
import ar.edu.unlp.dssd.model.Emergencia;
import ar.edu.unlp.dssd.model.EstadoConvocatoria;
import ar.edu.unlp.dssd.model.EstadoOferta;
import ar.edu.unlp.dssd.model.EtapaEmergencia;
import ar.edu.unlp.dssd.model.Oferta;
import ar.edu.unlp.dssd.model.OfertaConjunta;
import ar.edu.unlp.dssd.model.Organizacion;
import ar.edu.unlp.dssd.model.Proyecto;
import ar.edu.unlp.dssd.model.TipoRecurso;
import ar.edu.unlp.dssd.repository.EmergenciaRepository;
import ar.edu.unlp.dssd.service.BonitaMetricasService.MetricasBonitaResumen;
import ar.edu.unlp.dssd.service.EtapaEmergenciaService.EstadoEmergencia;

/** [Entrega final] Tests del servicio de indicadores, cobertura, ranking ONGs y series mensuales. */
@ExtendWith(MockitoExtension.class)
class IndicadoresServiceTest {

    @Mock
    private EmergenciaRepository emergenciaRepository;

    @Mock
    private EtapaEmergenciaService etapaService;

    @Mock
    private BonitaMetricasService bonitaMetricasService;

    @InjectMocks
    private IndicadoresService indicadoresService;

    private TipoRecurso recurso(String nombre) {
        TipoRecurso t = new TipoRecurso();
        t.setNombre(nombre);
        return t;
    }

    private Organizacion org(Long id, String nombre) {
        Organizacion o = new Organizacion();
        o.setId(id);
        o.setNombre(nombre);
        return o;
    }

    private Oferta oferta(EstadoOferta estado, int cantidad, TipoRecurso tipo, Organizacion org,
                          boolean enConjunta, LocalDateTime fechaAdjudicacion) {
        Oferta o = new Oferta();
        o.setEstado(estado);
        o.setCantidad(cantidad);
        o.setTipoRecurso(tipo);
        o.setOrganizacion(org);
        o.setFechaAdjudicacion(fechaAdjudicacion);
        if (enConjunta) {
            OfertaConjunta oc = new OfertaConjunta();
            oc.setId(99L);
            o.setOfertaConjunta(oc);
        }
        return o;
    }

    private Emergencia emergencia(Long id, Long caseId, LocalDate fechaCreacion, String gravedad, Proyecto proyecto) {
        Emergencia e = new Emergencia();
        e.setId(id);
        e.setCaseId(caseId);
        e.setFechaCreacion(fechaCreacion);
        e.setNivelGravedad(gravedad);
        e.setTipoDesastre("Inundación");
        e.setZonaAfectada("La Plata");
        e.setProyecto(proyecto);
        return e;
    }

    @Test
    void indicadores_combinaDatosLocalesNuevosBloquesY6BloquesDeBonita() {
        Proyecto terminado = new Proyecto();
        terminado.setEstado(Proyecto.TERMINADO);
        terminado.setFechaInicio(LocalDateTime.of(2026, 11, 1, 10, 0));
        terminado.setFechaFin(LocalDateTime.of(2026, 11, 3, 10, 0)); // 48h

        Proyecto enCurso = new Proyecto();
        enCurso.setEstado(Proyecto.EN_CURSO);
        enCurso.setFechaInicio(LocalDateTime.of(2026, 11, 10, 8, 0));

        Emergencia e1 = emergencia(1L, 101L, LocalDate.of(2026, 11, 1), "ALTA", terminado);
        Emergencia e2 = emergencia(2L, 102L, LocalDate.of(2026, 11, 10), "MEDIA", enCurso);

        Convocatoria c1 = new Convocatoria();
        c1.setEstado(EstadoConvocatoria.PUBLICADA);
        c1.setRonda(2);
        c1.setCoberturaParcialAceptada(true);
        Convocatoria c2 = new Convocatoria();
        c2.setEstado(EstadoConvocatoria.PUBLICADA);
        c2.setRonda(1);

        TipoRecurso agua = recurso("Agua potable");
        TipoRecurso alimentos = recurso("Raciones de alimento");
        Organizacion caritas = org(1L, "Cáritas");
        Organizacion cruzRoja = org(2L, "Cruz Roja");

        Oferta o1 = oferta(EstadoOferta.FINALIZADA, 200, agua, caritas, true, LocalDateTime.of(2026, 11, 2, 12, 0));
        Oferta o2 = oferta(EstadoOferta.ADJUDICADA, 100, alimentos, cruzRoja, false, LocalDateTime.of(2026, 11, 12, 9, 0));

        LoteDetalleDto loteAgua = new LoteDetalleDto(1L, 1L, "AGUA", "Agua potable", "Bidones", 250, 200L, 200, 200);
        LoteDetalleDto loteAlimentos = new LoteDetalleDto(2L, 2L, "ALIMENTOS", "Raciones de alimento", "Cajas", 100, 100L, 100, 100);

        when(emergenciaRepository.findAllByOrderByIdDesc()).thenReturn(List.of(e2, e1));
        when(etapaService.evaluar(e1)).thenReturn(
                new EstadoEmergencia(EtapaEmergencia.FINALIZADA, c1, List.of(), List.of(o1), List.of(o1), List.of(loteAgua)));
        when(etapaService.evaluar(e2)).thenReturn(
                new EstadoEmergencia(EtapaEmergencia.ADJUDICADA, c2, List.of(), List.of(o2), List.of(o2), List.of(loteAlimentos)));

        MetricasBonitaResumen resumenBonita = new MetricasBonitaResumen(
                new TiemposEtapasDto(true, FuenteDatos.BONITA_API, null, 2, 1, 24.0,
                        List.of(new TiempoEtapaDto("Desglosar Lotes", 2.5, 2.0, 3.0, 2, FuenteDatos.BONITA_API))),
                new CuellosDeBotellaBonitaDto(true, FuenteDatos.BONITA_API, null, 1, 1, List.of()),
                new TiemposResolucionCruzadosDto(true, FuenteDatos.COMBINADA, null, List.of(), List.of()),
                new ActividadUsuariosBonitaDto(true, FuenteDatos.BONITA_API, null, 3, List.of()),
                new EstadosCasosBonitaDto(true, FuenteDatos.BONITA_API, null, 1, 1, 1, 0, 50.0, List.of()),
                new TiemposEsperaEjecucionBonitaDto(true, FuenteDatos.BONITA_API, null, 1.0, 1.5, 40.0, List.of()));
        when(bonitaMetricasService.metricasProceso(anyMap(), isNull())).thenReturn(resumenBonita);

        IndicadoresDto dto = indicadoresService.indicadores(null, null);

        assertThat(dto.fuente()).isEqualTo(FuenteDatos.COMBINADA);
        assertThat(dto.fuentesPorSeccion()).containsEntry("coberturaNecesidades", FuenteDatos.DSSD_DB);
        assertThat(dto.fuentesPorSeccion()).containsEntry("rankingOngs", FuenteDatos.DSSD_DB);
        assertThat(dto.fuentesPorSeccion()).containsEntry("estadosCasosBonita", FuenteDatos.BONITA_API);
        assertThat(dto.fuentesPorSeccion()).containsEntry("tiemposEsperaVsEjecucionBonita", FuenteDatos.BONITA_API);

        // Nuevo bloque local 1: coberturaNecesidades
        assertThat(dto.coberturaNecesidades().fuente()).isEqualTo(FuenteDatos.DSSD_DB);
        assertThat(dto.coberturaNecesidades().unidadesRequeridas()).isEqualTo(350);
        assertThat(dto.coberturaNecesidades().unidadesAdjudicadas()).isEqualTo(300);
        assertThat(dto.coberturaNecesidades().porcentajeCoberturaAdjudicada()).isEqualTo(85.7);
        assertThat(dto.coberturaNecesidades().convocatoriasConReapertura()).isEqualTo(1);
        assertThat(dto.coberturaNecesidades().convocatoriasConCoberturaParcial()).isEqualTo(1);

        // Nuevo bloque local 2: rankingOngs
        assertThat(dto.rankingOngs().fuente()).isEqualTo(FuenteDatos.DSSD_DB);
        assertThat(dto.rankingOngs().totalOrganizacionesParticipantes()).isEqualTo(2);
        assertThat(dto.rankingOngs().organizaciones().get(0).organizacionNombre()).isEqualTo("Cáritas");
        assertThat(dto.rankingOngs().organizaciones().get(0).unidadesMovilizadas()).isEqualTo(200);
        assertThat(dto.rankingOngs().organizaciones().get(0).participacionesEnConsorcio()).isEqualTo(1);

        // Nuevos bloques Bonita 5 y 6
        assertThat(dto.estadosCasosBonita().fuente()).isEqualTo(FuenteDatos.BONITA_API);
        assertThat(dto.tiemposEsperaVsEjecucionBonita().fuente()).isEqualTo(FuenteDatos.BONITA_API);
    }

    @Test
    void seriesMensuales_agrupaSuministrosYConsorciosCronologicamenteConFuenteLocal() {
        Proyecto terminado = new Proyecto();
        terminado.setEstado(Proyecto.TERMINADO);
        terminado.setFechaFin(LocalDateTime.of(2026, 10, 20, 15, 0));

        Emergencia octubre = emergencia(1L, 101L, LocalDate.of(2026, 10, 5), "ALTA", terminado);
        Emergencia noviembre = emergencia(2L, 102L, LocalDate.of(2026, 11, 2), "BAJA", null);

        TipoRecurso agua = recurso("Agua potable");
        Oferta oOct = oferta(EstadoOferta.FINALIZADA, 150, agua, null, true, LocalDateTime.of(2026, 10, 10, 10, 0));
        Oferta oNov = oferta(EstadoOferta.ADJUDICADA, 300, agua, null, false, LocalDateTime.of(2026, 11, 5, 10, 0));

        when(emergenciaRepository.findAllByOrderByIdDesc()).thenReturn(List.of(noviembre, octubre));
        when(etapaService.evaluar(octubre)).thenReturn(
                new EstadoEmergencia(EtapaEmergencia.FINALIZADA, null, List.of(), List.of(oOct), List.of(oOct), List.of()));
        when(etapaService.evaluar(noviembre)).thenReturn(
                new EstadoEmergencia(EtapaEmergencia.ADJUDICADA, null, List.of(), List.of(oNov), List.of(oNov), List.of()));

        List<SerieMensualDto> series = indicadoresService.seriesMensuales(null, null);

        assertThat(series).extracting(SerieMensualDto::periodo).containsExactly("2026-10", "2026-11");
        assertThat(series).allMatch(s -> FuenteDatos.DSSD_DB.equals(s.fuente()));
        assertThat(series.get(0).suministrosMovilizados()).isEqualTo(150);
    }

    @Test
    void emergencias_filtraPorEtapaYEstadoProyectoConFuenteLocal() {
        Proyecto terminado = new Proyecto();
        terminado.setEstado(Proyecto.TERMINADO);
        Emergencia e1 = emergencia(1L, 101L, LocalDate.of(2026, 11, 1), "ALTA", terminado);
        Emergencia e2 = emergencia(2L, 102L, LocalDate.of(2026, 11, 2), "ALTA", null);

        when(emergenciaRepository.findAllByOrderByIdDesc()).thenReturn(List.of(e2, e1));
        when(etapaService.evaluar(e1)).thenReturn(
                new EstadoEmergencia(EtapaEmergencia.FINALIZADA, null, List.of(), List.of(), List.of(), List.of()));
        when(etapaService.evaluar(e2)).thenReturn(
                new EstadoEmergencia(EtapaEmergencia.EN_RELEVAMIENTO, null, List.of(), List.of(), List.of(), List.of()));

        List<AuditoriaEmergenciaDto> finalizadas = indicadoresService.emergencias(
                null, null, EtapaEmergencia.FINALIZADA, Proyecto.TERMINADO);
        List<AuditoriaEmergenciaDto> sinProyecto = indicadoresService.emergencias(
                null, null, null, "SIN_PROYECTO");

        assertThat(finalizadas).extracting(AuditoriaEmergenciaDto::id).containsExactly(1L);
        assertThat(finalizadas.get(0).fuente()).isEqualTo(FuenteDatos.DSSD_DB);
        assertThat(sinProyecto).extracting(AuditoriaEmergenciaDto::id).containsExactly(2L);
    }

    @Test
    void tiemposEmergencia_inexistente_lanza404() {
        when(emergenciaRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> indicadoresService.tiemposEmergencia(999L, null))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    void indicadores_conDesdePosteriorAHasta_lanzaIllegalArgument() {
        assertThatThrownBy(() -> indicadoresService.indicadores(
                LocalDate.of(2026, 12, 1), LocalDate.of(2026, 11, 1)))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
