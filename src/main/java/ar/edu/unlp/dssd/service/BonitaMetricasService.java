package ar.edu.unlp.dssd.service;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.DoubleSummaryStatistics;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ar.edu.unlp.dssd.dto.ActividadUsuarioDto;
import ar.edu.unlp.dssd.dto.ActividadUsuariosBonitaDto;
import ar.edu.unlp.dssd.dto.CuellosDeBotellaBonitaDto;
import ar.edu.unlp.dssd.dto.EsperaEjecucionEtapaDto;
import ar.edu.unlp.dssd.dto.EstadosCasosBonitaDto;
import ar.edu.unlp.dssd.dto.EtapaEjecucionDto;
import ar.edu.unlp.dssd.dto.FuenteDatos;
import ar.edu.unlp.dssd.dto.ProcesoBonitaResumenDto;
import ar.edu.unlp.dssd.dto.TareaPendienteEtapaDto;
import ar.edu.unlp.dssd.dto.TiempoEtapaDto;
import ar.edu.unlp.dssd.dto.TiempoGrupoDto;
import ar.edu.unlp.dssd.dto.TiemposEmergenciaDto;
import ar.edu.unlp.dssd.dto.TiemposEsperaEjecucionBonitaDto;
import ar.edu.unlp.dssd.dto.TiemposEtapasDto;
import ar.edu.unlp.dssd.dto.TiemposResolucionCruzadosDto;
import ar.edu.unlp.dssd.exception.BonitaException;
import ar.edu.unlp.dssd.model.Emergencia;
import ar.edu.unlp.dssd.service.BonitaService.DatosCasoBonita;
import ar.edu.unlp.dssd.service.BonitaService.DatosMetricasBonita;

/**
 * [Entrega final] Calcula los 6 bloques de métricas provenientes de la API REST de Bonita:
 * 1) Tiempos promedio por etapa, 2) Cuellos de botella actuales, 3) Tiempos cruzados por gravedad/tipo,
 * 4) Actividad operativa por usuario/actor, 5) Salud y estado de procesos/casos/fallos y
 * 6) Desglose de tiempo de espera en cola vs. tiempo de ejecución efectiva.
 */
@Service
public class BonitaMetricasService {

    private static final Logger log = LoggerFactory.getLogger(BonitaMetricasService.class);
    private static final double MS_POR_HORA = 3_600_000.0;

    private static final List<String> ORDEN_ETAPAS = List.of(
            BonitaService.TAREA_REGISTRAR_EMERGENCIA,
            BonitaService.TAREA_DESGLOSAR_LOTES,
            BonitaService.TAREA_GESTIONAR_CONVOCATORIA,
            BonitaService.TAREA_SELECCIONAR_OFERTAS,
            BonitaService.TAREA_FINALIZAR_ACTIVIDADES);

    private static final DateTimeFormatter FORMATO_BONITA = new DateTimeFormatterBuilder()
            .appendPattern("yyyy-MM-dd HH:mm:ss")
            .optionalStart()
            .appendFraction(ChronoField.NANO_OF_SECOND, 0, 9, true)
            .optionalEnd()
            .toFormatter();

    /** Resultado agrupado de los 6 bloques de métricas consultados en una sola llamada a Bonita. */
    public record MetricasBonitaResumen(TiemposEtapasDto tiemposPorEtapa,
                                        CuellosDeBotellaBonitaDto cuellosDeBotellaBonita,
                                        TiemposResolucionCruzadosDto tiemposResolucionCruzados,
                                        ActividadUsuariosBonitaDto actividadUsuariosBonita,
                                        EstadosCasosBonitaDto estadosCasosBonita,
                                        TiemposEsperaEjecucionBonitaDto tiemposEsperaVsEjecucionBonita) {
    }

    @Autowired
    private BonitaService bonitaService;

    /** Calcula los 6 bloques de métricas de Bonita para las emergencias del período con caseId asociado. */
    public MetricasBonitaResumen metricasProceso(Map<Long, Emergencia> emergenciasPorCaseId,
                                                 SesionBonita sesionUsuario) {
        if (emergenciasPorCaseId == null || emergenciasPorCaseId.isEmpty()) {
            return vacio(true, null, 0);
        }

        Set<Long> caseIdsPeriodo = emergenciasPorCaseId.keySet();
        DatosMetricasBonita datos;
        try {
            datos = bonitaService.obtenerDatosMetricasProceso(sesionUsuario);
        } catch (BonitaException e) {
            log.warn("No se pudieron obtener métricas de Bonita: {}", e.getMessage());
            return vacio(false, "No se pudieron consultar los tiempos en Bonita: " + e.getMessage(),
                    caseIdsPeriodo.size());
        }

        // 1. Tiempos por etapa desde /API/bpm/archivedFlowNode
        List<EjecucionTarea> completadas = reconstruirTareasCompletadas(datos.flujosArchivados(), caseIdsPeriodo);

        Map<String, List<EjecucionTarea>> porEtapa = new HashMap<>();
        for (EjecucionTarea tarea : completadas) {
            porEtapa.computeIfAbsent(tarea.nombre(), k -> new ArrayList<>()).add(tarea);
        }

        List<TiempoEtapaDto> etapas = porEtapa.entrySet().stream()
                .sorted(Comparator.comparingInt((Map.Entry<String, ?> e) -> ordenEtapa(e.getKey()))
                        .thenComparing(Map.Entry::getKey))
                .map(entrada -> resumirEtapa(entrada.getKey(), entrada.getValue()))
                .toList();

        // 2. Cuellos de botella actuales desde /API/bpm/case y /API/bpm/humanTask
        Set<Long> casosActivos = new HashSet<>();
        for (Map<String, Object> caso : datos.casosActivos()) {
            Long caseId = extraerCaseIdCasoActivo(caso);
            if (caseId != null && caseIdsPeriodo.contains(caseId)) {
                casosActivos.add(caseId);
            }
        }
        List<TareaPendienteEtapaDto> pendientesPorEtapa = calcularCuellosDeBotella(
                datos.tareasPendientes(), caseIdsPeriodo, LocalDateTime.now().withNano(0));
        int totalPendientes = pendientesPorEtapa.stream()
                .mapToInt(TareaPendienteEtapaDto::cantidadPendientes).sum();

        // 3. Tiempo total y tiempos cruzados por gravedad y tipo de desastre (DSSD_DB + BONITA_API)
        DoubleSummaryStatistics estadisticasCasos = new DoubleSummaryStatistics();
        Set<Long> casosFinalizados = new HashSet<>();
        Map<String, List<Double>> duracionesPorGravedad = new HashMap<>();
        Map<String, List<Double>> duracionesPorTipo = new HashMap<>();

        for (Map<String, Object> caso : datos.casosArchivados()) {
            Long caseId = extraerCaseIdCaso(caso);
            if (caseId == null || !caseIdsPeriodo.contains(caseId) || !casosFinalizados.add(caseId)) {
                continue;
            }
            LocalDateTime inicio = parsearFecha(caso.get("start"));
            LocalDateTime fin = primeroNoNulo(parsearFecha(caso.get("end_date")), parsearFecha(caso.get("archivedDate")));
            if (inicio != null && fin != null) {
                double duracion = horasEntre(inicio, fin);
                estadisticasCasos.accept(duracion);
                Emergencia emergencia = emergenciasPorCaseId.get(caseId);
                if (emergencia != null) {
                    duracionesPorGravedad.computeIfAbsent(etiqueta(emergencia.getNivelGravedad()), k -> new ArrayList<>())
                            .add(duracion);
                    duracionesPorTipo.computeIfAbsent(etiqueta(emergencia.getTipoDesastre()), k -> new ArrayList<>())
                            .add(duracion);
                }
            }
        }

        Double promedioTotal = estadisticasCasos.getCount() == 0
                ? null
                : redondear(estadisticasCasos.getAverage());

        List<TiempoGrupoDto> tiemposPorGravedad = resumirGrupos(duracionesPorGravedad);
        List<TiempoGrupoDto> tiemposPorTipoDesastre = resumirGrupos(duracionesPorTipo);

        // 4. Carga operativa por usuario/actor en Bonita (executedBy + /API/identity/user)
        List<ActividadUsuarioDto> usuarios = calcularActividadPorUsuario(completadas, datos.usuariosPorId());
        int totalTareasCompletadas = usuarios.stream().mapToInt(ActividadUsuarioDto::tareasCompletadas).sum();

        // 5. Salud y estado de procesos, casos y tareas fallidas en Bonita
        List<ProcesoBonitaResumenDto> procesos = datos.procesos().stream()
                .map(p -> new ProcesoBonitaResumenDto(
                        texto(p.get("id")), texto(p.get("name")), texto(p.get("version")),
                        texto(p.get("activationState")), FuenteDatos.BONITA_API))
                .toList();
        int fallidas = (int) datos.tareasFallidas().stream()
                .map(BonitaMetricasService::extraerCaseIdFlujo)
                .filter(caseId -> caseId != null && caseIdsPeriodo.contains(caseId))
                .count();
        double tasaFinalizacionBonita = porcentaje(casosFinalizados.size(),
                casosActivos.size() + casosFinalizados.size());
        EstadosCasosBonitaDto estadosCasos = new EstadosCasosBonitaDto(true, FuenteDatos.BONITA_API, null,
                procesos.size(), casosActivos.size(), casosFinalizados.size(), fallidas,
                tasaFinalizacionBonita, procesos);

        // 6. Espera en cola (ready -> assigned) vs. ejecución efectiva (assigned -> completed)
        TiemposEsperaEjecucionBonitaDto esperaVsEjecucion = calcularEsperaVsEjecucion(porEtapa);

        return new MetricasBonitaResumen(
                new TiemposEtapasDto(true, FuenteDatos.BONITA_API, null, caseIdsPeriodo.size(),
                        casosFinalizados.size(), promedioTotal, etapas),
                new CuellosDeBotellaBonitaDto(true, FuenteDatos.BONITA_API, null,
                        casosActivos.size(), totalPendientes, pendientesPorEtapa),
                new TiemposResolucionCruzadosDto(true, FuenteDatos.COMBINADA, null,
                        tiemposPorGravedad, tiemposPorTipoDesastre),
                new ActividadUsuariosBonitaDto(true, FuenteDatos.BONITA_API, null,
                        totalTareasCompletadas, usuarios),
                estadosCasos,
                esperaVsEjecucion);
    }

    /** Devuelve la línea de tiempo de etapas (completadas y pendientes) de una emergencia puntual en Bonita. */
    public TiemposEmergenciaDto tiemposDeEmergencia(Emergencia emergencia, SesionBonita sesionUsuario) {
        Long caseId = emergencia.getCaseId();
        if (caseId == null) {
            return new TiemposEmergenciaDto(emergencia.getId(), null, false, FuenteDatos.BONITA_API,
                    "La emergencia no tiene un caso asociado en Bonita.", null, null, null, List.of());
        }

        DatosCasoBonita datos;
        try {
            datos = bonitaService.obtenerDatosMetricasCaso(caseId, sesionUsuario);
        } catch (BonitaException e) {
            log.warn("No se pudieron obtener los tiempos del caso {} en Bonita: {}", caseId, e.getMessage());
            return new TiemposEmergenciaDto(emergencia.getId(), caseId, false, FuenteDatos.BONITA_API,
                    "No se pudieron consultar los tiempos en Bonita: " + e.getMessage(),
                    null, null, null, List.of());
        }

        List<EtapaEjecucionDto> etapas = new ArrayList<>();
        for (EjecucionTarea completada : reconstruirTareasCompletadas(datos.flujosArchivados(), Set.of(caseId))) {
            etapas.add(new EtapaEjecucionDto(FuenteDatos.BONITA_API, completada.nombre(), "COMPLETADA",
                    completada.inicio(), completada.fin(), redondear(completada.duracionHoras())));
        }

        LocalDateTime ahora = LocalDateTime.now().withNano(0);
        for (Map<String, Object> pendiente : datos.tareasPendientes()) {
            String nombre = extraerNombre(pendiente);
            if (nombre == null) {
                continue;
            }
            LocalDateTime inicio = primeroNoNulo(parsearFecha(pendiente.get("reached_state_date")),
                    parsearFecha(pendiente.get("assigned_date")));
            Double transcurridas = inicio == null ? null : redondear(horasEntre(inicio, ahora));
            etapas.add(new EtapaEjecucionDto(FuenteDatos.BONITA_API, nombre, "PENDIENTE", inicio, null, transcurridas));
        }

        etapas.sort(Comparator.comparing(EtapaEjecucionDto::inicio, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparingInt(e -> ordenEtapa(e.etapa())));

        LocalDateTime inicioCaso = null;
        LocalDateTime finCaso = null;
        Double duracionTotal = null;
        Optional<Map<String, Object>> casoArchivado = datos.casoArchivado();
        if (casoArchivado.isPresent()) {
            inicioCaso = parsearFecha(casoArchivado.get().get("start"));
            finCaso = primeroNoNulo(parsearFecha(casoArchivado.get().get("end_date")),
                    parsearFecha(casoArchivado.get().get("archivedDate")));
            if (inicioCaso != null && finCaso != null) {
                duracionTotal = redondear(horasEntre(inicioCaso, finCaso));
            }
        } else if (!etapas.isEmpty()) {
            inicioCaso = etapas.get(0).inicio();
        }

        return new TiemposEmergenciaDto(emergencia.getId(), caseId, true, FuenteDatos.BONITA_API, null,
                inicioCaso, finCaso, duracionTotal, etapas);
    }

    // ------------------------------------------------------------------
    // Reconstrucción de ejecuciones y agregaciones
    // ------------------------------------------------------------------

    private static MetricasBonitaResumen vacio(boolean disponible, String mensaje, int casosAnalizados) {
        return new MetricasBonitaResumen(
                new TiemposEtapasDto(disponible, FuenteDatos.BONITA_API, mensaje, casosAnalizados, 0, null, List.of()),
                new CuellosDeBotellaBonitaDto(disponible, FuenteDatos.BONITA_API, mensaje, 0, 0, List.of()),
                new TiemposResolucionCruzadosDto(disponible, FuenteDatos.COMBINADA, mensaje, List.of(), List.of()),
                new ActividadUsuariosBonitaDto(disponible, FuenteDatos.BONITA_API, mensaje, 0, List.of()),
                new EstadosCasosBonitaDto(disponible, FuenteDatos.BONITA_API, mensaje, 0, 0, 0, 0, 0.0, List.of()),
                new TiemposEsperaEjecucionBonitaDto(disponible, FuenteDatos.BONITA_API, mensaje, null, null, null,
                        List.of()));
    }

    private record ClaveTarea(Long caseId, String instanciaId) {
    }

    private record EjecucionTarea(Long caseId, String nombre, String ejecutadoPorId, LocalDateTime inicio,
                                  LocalDateTime asignada, LocalDateTime fin, double duracionHoras,
                                  double esperaHoras, double ejecucionHoras) {
    }

    private static final class AcumuladorTarea {
        final Long caseId;
        String nombre;
        String ejecutadoPorId;
        String asignadoId;
        LocalDateTime inicioReady;
        LocalDateTime asignada;
        LocalDateTime finCompletada;

        AcumuladorTarea(Long caseId, String nombre) {
            this.caseId = caseId;
            this.nombre = nombre;
        }
    }

    private List<EjecucionTarea> reconstruirTareasCompletadas(List<Map<String, Object>> flujos,
                                                              Set<Long> caseIdsPermitidos) {
        Map<ClaveTarea, AcumuladorTarea> acumuladores = new LinkedHashMap<>();
        for (Map<String, Object> nodo : flujos) {
            String tipoNodo = texto(nodo.get("type"));
            if (tipoNodo != null && !"USER_TASK".equalsIgnoreCase(tipoNodo) && !"MANUAL_TASK".equalsIgnoreCase(tipoNodo)) {
                continue;
            }
            Long caseId = extraerCaseIdFlujo(nodo);
            if (caseId == null || !caseIdsPermitidos.contains(caseId)) {
                continue;
            }
            String nombre = extraerNombre(nodo);
            if (nombre == null) {
                continue;
            }
            String sourceObjectId = texto(nodo.get("sourceObjectId"));
            String instanciaId = sourceObjectId != null ? sourceObjectId : "nombre:" + nombre;
            AcumuladorTarea acc = acumuladores.computeIfAbsent(new ClaveTarea(caseId, instanciaId),
                    k -> new AcumuladorTarea(caseId, nombre));

            String estado = texto(nodo.get("state"));
            LocalDateTime fechaEstado = parsearFecha(nodo.get("reached_state_date"));
            LocalDateTime fechaArchivo = parsearFecha(nodo.get("archivedDate"));
            LocalDateTime fechaActualizacion = parsearFecha(nodo.get("last_update_date"));
            LocalDateTime fechaAsignacion = parsearFecha(nodo.get("assigned_date"));

            if (fechaAsignacion != null && (acc.asignada == null || fechaAsignacion.isBefore(acc.asignada))) {
                acc.asignada = fechaAsignacion;
            }
            String assignedId = idUsuarioValido(nodo.get("assigned_id"));
            if (assignedId != null) {
                acc.asignadoId = assignedId;
            }
            String executedBy = idUsuarioValido(nodo.get("executedBy"));
            if (executedBy != null) {
                acc.ejecutadoPorId = executedBy;
            }

            if (estado == null) {
                continue;
            }
            String estadoLower = estado.toLowerCase();
            if ("ready".equals(estadoLower) || "initializing".equals(estadoLower)) {
                LocalDateTime inicioCandidato = primeroNoNulo(fechaEstado, fechaArchivo);
                if (inicioCandidato != null && (acc.inicioReady == null || inicioCandidato.isBefore(acc.inicioReady))) {
                    acc.inicioReady = inicioCandidato;
                }
            } else if ("completed".equals(estadoLower)) {
                LocalDateTime finCandidato = primeroNoNulo(fechaEstado, fechaArchivo, fechaActualizacion);
                if (finCandidato != null && (acc.finCompletada == null || finCandidato.isAfter(acc.finCompletada))) {
                    acc.finCompletada = finCandidato;
                }
            }
        }

        List<EjecucionTarea> resultado = new ArrayList<>();
        for (AcumuladorTarea acc : acumuladores.values()) {
            if (acc.finCompletada == null) {
                continue;
            }
            LocalDateTime inicio = primeroNoNulo(acc.inicioReady, acc.asignada, acc.finCompletada);
            double total = horasEntre(inicio, acc.finCompletada);
            double espera;
            double ejecucion;
            if (acc.asignada != null && !acc.asignada.isBefore(inicio) && !acc.asignada.isAfter(acc.finCompletada)) {
                espera = horasEntre(inicio, acc.asignada);
                ejecucion = horasEntre(acc.asignada, acc.finCompletada);
            } else {
                espera = 0.0;
                ejecucion = total;
            }
            String actor = primeroNoNulo(acc.ejecutadoPorId, acc.asignadoId);
            resultado.add(new EjecucionTarea(acc.caseId, acc.nombre, actor, inicio, acc.asignada,
                    acc.finCompletada, total, espera, ejecucion));
        }
        return resultado;
    }

    private TiempoEtapaDto resumirEtapa(String etapa, List<EjecucionTarea> ejecuciones) {
        DoubleSummaryStatistics stats = new DoubleSummaryStatistics();
        Set<Long> casosUnicos = new HashSet<>();
        for (EjecucionTarea ej : ejecuciones) {
            stats.accept(ej.duracionHoras());
            casosUnicos.add(ej.caseId());
        }
        return new TiempoEtapaDto(etapa, redondear(stats.getAverage()),
                redondear(stats.getMin()), redondear(stats.getMax()), casosUnicos.size(), FuenteDatos.BONITA_API);
    }

    private TiemposEsperaEjecucionBonitaDto calcularEsperaVsEjecucion(Map<String, List<EjecucionTarea>> porEtapa) {
        DoubleSummaryStatistics esperaGlobal = new DoubleSummaryStatistics();
        DoubleSummaryStatistics ejecucionGlobal = new DoubleSummaryStatistics();

        List<EsperaEjecucionEtapaDto> etapas = porEtapa.entrySet().stream()
                .sorted(Comparator.comparingInt((Map.Entry<String, ?> e) -> ordenEtapa(e.getKey()))
                        .thenComparing(Map.Entry::getKey))
                .map(entrada -> {
                    DoubleSummaryStatistics espera = new DoubleSummaryStatistics();
                    DoubleSummaryStatistics ejecucion = new DoubleSummaryStatistics();
                    Set<Long> casosUnicos = new HashSet<>();
                    for (EjecucionTarea ej : entrada.getValue()) {
                        espera.accept(ej.esperaHoras());
                        ejecucion.accept(ej.ejecucionHoras());
                        esperaGlobal.accept(ej.esperaHoras());
                        ejecucionGlobal.accept(ej.ejecucionHoras());
                        casosUnicos.add(ej.caseId());
                    }
                    double espProm = espera.getAverage();
                    double ejecProm = ejecucion.getAverage();
                    double pctEspera = porcentajeDouble(espProm, espProm + ejecProm);
                    return new EsperaEjecucionEtapaDto(entrada.getKey(), redondear(espProm), redondear(ejecProm),
                            pctEspera, casosUnicos.size(), FuenteDatos.BONITA_API);
                })
                .toList();

        if (esperaGlobal.getCount() == 0) {
            return new TiemposEsperaEjecucionBonitaDto(true, FuenteDatos.BONITA_API, null,
                    null, null, null, List.of());
        }
        double espGlob = esperaGlobal.getAverage();
        double ejecGlob = ejecucionGlobal.getAverage();
        return new TiemposEsperaEjecucionBonitaDto(true, FuenteDatos.BONITA_API, null,
                redondear(espGlob), redondear(ejecGlob), porcentajeDouble(espGlob, espGlob + ejecGlob), etapas);
    }

    private List<TareaPendienteEtapaDto> calcularCuellosDeBotella(List<Map<String, Object>> tareasPendientes,
                                                                  Set<Long> caseIdsPermitidos, LocalDateTime ahora) {
        Map<String, DoubleSummaryStatistics> porEtapa = new HashMap<>();
        for (Map<String, Object> tarea : tareasPendientes) {
            Long caseId = extraerCaseIdFlujo(tarea);
            if (caseId == null || !caseIdsPermitidos.contains(caseId)) {
                continue;
            }
            String nombre = extraerNombre(tarea);
            if (nombre == null) {
                continue;
            }
            LocalDateTime inicio = primeroNoNulo(parsearFecha(tarea.get("reached_state_date")),
                    parsearFecha(tarea.get("assigned_date")), parsearFecha(tarea.get("last_update_date")));
            double esperaHoras = inicio == null ? 0.0 : horasEntre(inicio, ahora);
            porEtapa.computeIfAbsent(nombre, k -> new DoubleSummaryStatistics()).accept(esperaHoras);
        }

        return porEtapa.entrySet().stream()
                .sorted(Comparator.comparingInt((Map.Entry<String, DoubleSummaryStatistics> e) -> ordenEtapa(e.getKey()))
                        .thenComparing(Map.Entry::getKey))
                .map(entrada -> new TareaPendienteEtapaDto(
                        entrada.getKey(),
                        (int) entrada.getValue().getCount(),
                        redondear(entrada.getValue().getAverage()),
                        redondear(entrada.getValue().getMax()),
                        FuenteDatos.BONITA_API))
                .toList();
    }

    private List<TiempoGrupoDto> resumirGrupos(Map<String, List<Double>> duracionesPorGrupo) {
        return duracionesPorGrupo.entrySet().stream()
                .map(entrada -> {
                    DoubleSummaryStatistics stats = new DoubleSummaryStatistics();
                    entrada.getValue().forEach(stats::accept);
                    return new TiempoGrupoDto(entrada.getKey(), redondear(stats.getAverage()),
                            redondear(stats.getMin()), redondear(stats.getMax()), (int) stats.getCount(),
                            FuenteDatos.COMBINADA);
                })
                .sorted(Comparator.comparingDouble(TiempoGrupoDto::promedioHoras).reversed()
                        .thenComparing(TiempoGrupoDto::etiqueta))
                .toList();
    }

    private List<ActividadUsuarioDto> calcularActividadPorUsuario(List<EjecucionTarea> completadas,
                                                                  Map<String, String> usuariosPorId) {
        Map<String, DoubleSummaryStatistics> porUsuario = new HashMap<>();
        for (EjecucionTarea ej : completadas) {
            if (ej.ejecutadoPorId() == null) {
                continue;
            }
            String nombreUsuario = usuariosPorId != null
                    ? usuariosPorId.getOrDefault(ej.ejecutadoPorId(), "usuario#" + ej.ejecutadoPorId())
                    : "usuario#" + ej.ejecutadoPorId();
            porUsuario.computeIfAbsent(nombreUsuario, k -> new DoubleSummaryStatistics()).accept(ej.duracionHoras());
        }

        return porUsuario.entrySet().stream()
                .map(entrada -> new ActividadUsuarioDto(
                        entrada.getKey(),
                        (int) entrada.getValue().getCount(),
                        redondear(entrada.getValue().getAverage()),
                        FuenteDatos.BONITA_API))
                .sorted(Comparator.comparingInt(ActividadUsuarioDto::tareasCompletadas).reversed()
                        .thenComparing(ActividadUsuarioDto::usuario))
                .toList();
    }

    // ------------------------------------------------------------------
    // Utilidades de parseo y formato
    // ------------------------------------------------------------------

    static LocalDateTime parsearFecha(Object valor) {
        if (valor == null) {
            return null;
        }
        if (valor instanceof Number numero) {
            return LocalDateTime.ofInstant(Instant.ofEpochMilli(numero.longValue()), ZoneId.systemDefault());
        }
        String texto = valor.toString().trim();
        if (texto.isEmpty()) {
            return null;
        }
        if (texto.chars().allMatch(Character::isDigit)) {
            try {
                return LocalDateTime.ofInstant(Instant.ofEpochMilli(Long.parseLong(texto)), ZoneId.systemDefault());
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        try {
            return LocalDateTime.parse(texto, FORMATO_BONITA);
        } catch (DateTimeParseException ignored) {
            // Se prueban formatos ISO
        }
        try {
            return OffsetDateTime.parse(texto).atZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();
        } catch (DateTimeParseException ignored) {
            // Se prueba LocalDateTime ISO
        }
        try {
            return LocalDateTime.parse(texto);
        } catch (DateTimeParseException ignored) {
            return null;
        }
    }

    private static Long extraerCaseIdFlujo(Map<String, Object> nodo) {
        return primeroNoNulo(parsearLong(nodo.get("caseId")),
                parsearLong(nodo.get("rootCaseId")),
                parsearLong(nodo.get("parentCaseId")));
    }

    private static Long extraerCaseIdCaso(Map<String, Object> caso) {
        return primeroNoNulo(parsearLong(caso.get("sourceObjectId")),
                parsearLong(caso.get("rootCaseId")),
                parsearLong(caso.get("id")));
    }

    private static Long extraerCaseIdCasoActivo(Map<String, Object> caso) {
        return primeroNoNulo(parsearLong(caso.get("id")),
                parsearLong(caso.get("rootCaseId")));
    }

    private static String extraerNombre(Map<String, Object> nodo) {
        return primeroNoNulo(texto(nodo.get("name")), texto(nodo.get("displayName")));
    }

    private static String idUsuarioValido(Object valor) {
        String id = texto(valor);
        return (id == null || "0".equals(id) || "-1".equals(id)) ? null : id;
    }

    private static String etiqueta(String valor) {
        return valor == null || valor.isBlank() ? "Sin dato" : valor.trim();
    }

    private static Long parsearLong(Object valor) {
        if (valor == null) {
            return null;
        }
        if (valor instanceof Number n) {
            return n.longValue();
        }
        String s = valor.toString().trim();
        if (s.isEmpty()) {
            return null;
        }
        try {
            return Long.valueOf(s);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static String texto(Object valor) {
        if (valor == null) {
            return null;
        }
        String s = valor.toString().trim();
        return s.isEmpty() ? null : s;
    }

    @SafeVarargs
    private static <T> T primeroNoNulo(T... valores) {
        for (T v : valores) {
            if (v != null) {
                return v;
            }
        }
        return null;
    }

    private static int ordenEtapa(String nombre) {
        int idx = ORDEN_ETAPAS.indexOf(nombre);
        return idx >= 0 ? idx : ORDEN_ETAPAS.size();
    }

    private static double horasEntre(LocalDateTime inicio, LocalDateTime fin) {
        long ms = Math.max(0L, Duration.between(inicio, fin).toMillis());
        return ms / MS_POR_HORA;
    }

    private static double porcentaje(long parte, long total) {
        if (total == 0) {
            return 0.0;
        }
        return Math.round(1000.0 * parte / total) / 10.0;
    }

    private static double porcentajeDouble(double parte, double total) {
        if (total <= 0.0) {
            return 0.0;
        }
        return Math.round(1000.0 * parte / total) / 10.0;
    }

    private static double redondear(double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }
}
