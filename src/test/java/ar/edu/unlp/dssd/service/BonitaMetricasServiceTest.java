package ar.edu.unlp.dssd.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ar.edu.unlp.dssd.dto.FuenteDatos;
import ar.edu.unlp.dssd.dto.TiemposEmergenciaDto;
import ar.edu.unlp.dssd.exception.BonitaException;
import ar.edu.unlp.dssd.model.Emergencia;
import ar.edu.unlp.dssd.service.BonitaMetricasService.MetricasBonitaResumen;
import ar.edu.unlp.dssd.service.BonitaService.DatosCasoBonita;
import ar.edu.unlp.dssd.service.BonitaService.DatosMetricasBonita;

/** [Entrega final] Tests de los 6 bloques de métricas de Bonita. */
@ExtendWith(MockitoExtension.class)
class BonitaMetricasServiceTest {

    @Mock
    private BonitaService bonitaService;

    @InjectMocks
    private BonitaMetricasService metricasService;

    private Emergencia emergencia(Long id, Long caseId, String tipo, String gravedad) {
        Emergencia e = new Emergencia();
        e.setId(id);
        e.setCaseId(caseId);
        e.setTipoDesastre(tipo);
        e.setNivelGravedad(gravedad);
        return e;
    }

    @Test
    void metricasProceso_calculaLos6BloquesDeBonita() {
        List<Map<String, Object>> procesos = List.of(
                Map.of("id", "8001", "name", "GestionEmergencias", "version", "1.0", "activationState", "ENABLED"));
        List<Map<String, Object>> flujos = List.of(
                // Caso 101 - Desglosar Lotes: ready 10:00 -> assigned 11:00 (1h espera) -> completed 12:00 (1h ejec)
                Map.of("caseId", "101", "sourceObjectId", "10", "name", "Desglosar Lotes",
                        "state", "ready", "reached_state_date", "2026-11-10 10:00:00.000"),
                Map.of("caseId", "101", "sourceObjectId", "10", "name", "Desglosar Lotes",
                        "state", "completed", "assigned_date", "2026-11-10 11:00:00.000",
                        "reached_state_date", "2026-11-10 12:00:00.000", "executedBy", "302"),
                // Caso 101 - Registrar Emergencia.: 0.5h (sin espera)
                Map.of("caseId", "101", "sourceObjectId", "9", "name", "Registrar Emergencia.",
                        "state", "ready", "reached_state_date", "2026-11-10 09:30:00.000"),
                Map.of("caseId", "101", "sourceObjectId", "9", "name", "Registrar Emergencia.",
                        "state", "completed", "reached_state_date", "2026-11-10 10:00:00.000",
                        "executedBy", "301"),
                // Caso 102 - Desglosar Lotes: ready 10:00 -> assigned 13:00 (3h espera) -> completed 14:00 (1h ejec)
                Map.of("caseId", "102", "sourceObjectId", "20", "name", "Desglosar Lotes",
                        "state", "ready", "reached_state_date", "2026-11-11 10:00:00.000"),
                Map.of("caseId", "102", "sourceObjectId", "20", "name", "Desglosar Lotes",
                        "state", "completed", "assigned_date", "2026-11-11 13:00:00.000",
                        "reached_state_date", "2026-11-11 14:00:00.000", "executedBy", "302"));

        List<Map<String, Object>> casosArchivados = List.of(
                Map.of("sourceObjectId", "101", "start", "2026-11-10 09:30:00.000",
                        "end_date", "2026-11-11 09:30:00.000"), // 24h
                Map.of("sourceObjectId", "102", "start", "2026-11-11 10:00:00.000",
                        "end_date", "2026-11-11 22:00:00.000")); // 12h

        String haceDosHoras = LocalDateTime.now().minusHours(2).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        List<Map<String, Object>> casosActivos = List.of(Map.of("id", "103"));
        List<Map<String, Object>> pendientes = List.of(
                Map.of("caseId", "103", "name", "Gestionar Convocatoria", "reached_state_date", haceDosHoras));
        List<Map<String, Object>> fallidas = List.of(Map.of("caseId", "103", "state", "failed"));
        Map<String, String> usuarios = Map.of("301", "olmos_municipio", "302", "agente_coordinador_1");

        when(bonitaService.obtenerDatosMetricasProceso(null))
                .thenReturn(new DatosMetricasBonita(procesos, flujos, casosArchivados, casosActivos,
                        pendientes, fallidas, usuarios));

        Map<Long, Emergencia> porCaseId = Map.of(
                101L, emergencia(1L, 101L, "Inundación", "Alta"),
                102L, emergencia(2L, 102L, "Incendio", "Media"),
                103L, emergencia(3L, 103L, "Inundación", "Alta"));

        MetricasBonitaResumen res = metricasService.metricasProceso(porCaseId, null);

        // Bloques 1..4
        assertThat(res.tiemposPorEtapa().promedioResolucionTotalHoras()).isEqualTo(18.0);
        assertThat(res.cuellosDeBotellaBonita().totalTareasPendientes()).isEqualTo(1);
        assertThat(res.tiemposResolucionCruzados().porGravedad().get(0).etiqueta()).isEqualTo("Alta");
        assertThat(res.actividadUsuariosBonita().totalTareasCompletadas()).isEqualTo(3);

        // Bloque 5: estadosCasosBonita
        assertThat(res.estadosCasosBonita().disponible()).isTrue();
        assertThat(res.estadosCasosBonita().fuente()).isEqualTo(FuenteDatos.BONITA_API);
        assertThat(res.estadosCasosBonita().versionesProcesoDesplegadas()).isEqualTo(1);
        assertThat(res.estadosCasosBonita().casosActivos()).isEqualTo(1);
        assertThat(res.estadosCasosBonita().casosFinalizados()).isEqualTo(2);
        assertThat(res.estadosCasosBonita().tareasFallidas()).isEqualTo(1);
        assertThat(res.estadosCasosBonita().tasaFinalizacionPorcentaje()).isEqualTo(66.7);

        // Bloque 6: tiemposEsperaVsEjecucionBonita
        assertThat(res.tiemposEsperaVsEjecucionBonita().disponible()).isTrue();
        assertThat(res.tiemposEsperaVsEjecucionBonita().fuente()).isEqualTo(FuenteDatos.BONITA_API);
        var desglosar = res.tiemposEsperaVsEjecucionBonita().etapas().get(1);
        assertThat(desglosar.etapa()).isEqualTo("Desglosar Lotes");
        assertThat(desglosar.esperaPromedioHoras()).isEqualTo(2.0);
        assertThat(desglosar.ejecucionPromedioHoras()).isEqualTo(1.0);
        assertThat(desglosar.porcentajeEspera()).isEqualTo(66.7);
    }

    @Test
    void metricasProceso_sinCasosEnElPeriodo_noLlamaABonita() {
        MetricasBonitaResumen res = metricasService.metricasProceso(Map.of(), null);

        assertThat(res.estadosCasosBonita().disponible()).isTrue();
        assertThat(res.tiemposEsperaVsEjecucionBonita().etapas()).isEmpty();
        verifyNoInteractions(bonitaService);
    }

    @Test
    void metricasProceso_siBonitaFalla_devuelveNoDisponibleEnLos6Bloques() {
        when(bonitaService.obtenerDatosMetricasProceso(null))
                .thenThrow(new BonitaException("Bonita no responde"));

        MetricasBonitaResumen res = metricasService.metricasProceso(
                Map.of(101L, emergencia(1L, 101L, "Inundación", "Alta")), null);

        assertThat(res.tiemposPorEtapa().disponible()).isFalse();
        assertThat(res.cuellosDeBotellaBonita().disponible()).isFalse();
        assertThat(res.tiemposResolucionCruzados().disponible()).isFalse();
        assertThat(res.actividadUsuariosBonita().disponible()).isFalse();
        assertThat(res.estadosCasosBonita().disponible()).isFalse();
        assertThat(res.tiemposEsperaVsEjecucionBonita().disponible()).isFalse();
    }

    @Test
    void tiemposDeEmergencia_combinaEtapasCompletadasYPendientes() {
        Emergencia e = emergencia(5L, 200L, "Inundación", "Alta");

        List<Map<String, Object>> flujos = List.of(
                Map.of("caseId", "200", "sourceObjectId", "1", "name", "Registrar Emergencia.",
                        "state", "ready", "reached_state_date", "2026-11-15 08:00:00.000"),
                Map.of("caseId", "200", "sourceObjectId", "1", "name", "Registrar Emergencia.",
                        "state", "completed", "reached_state_date", "2026-11-15 09:30:00.000"));
        List<Map<String, Object>> pendientes = List.of(
                Map.of("name", "Desglosar Lotes", "reached_state_date", "2026-11-15 09:31:00.000"));

        when(bonitaService.obtenerDatosMetricasCaso(200L, null))
                .thenReturn(new DatosCasoBonita(flujos, pendientes, Optional.empty()));

        TiemposEmergenciaDto resultado = metricasService.tiemposDeEmergencia(e, null);

        assertThat(resultado.disponible()).isTrue();
        assertThat(resultado.etapas()).hasSize(2);
    }

    @Test
    void tiemposDeEmergencia_sinCaseId_devuelveNoDisponibleSinLlamarABonita() {
        Emergencia e = emergencia(8L, null, "Inundación", "Alta");

        TiemposEmergenciaDto resultado = metricasService.tiemposDeEmergencia(e, null);

        assertThat(resultado.disponible()).isFalse();
        verifyNoInteractions(bonitaService);
    }
}
