package ar.edu.unlp.dssd.service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.DoubleSummaryStatistics;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ar.edu.unlp.dssd.dto.AuditoriaEmergenciaDto;
import ar.edu.unlp.dssd.dto.CantidadPorEtiquetaDto;
import ar.edu.unlp.dssd.dto.CoberturaNecesidadesDto;
import ar.edu.unlp.dssd.dto.CoberturaRecursoDto;
import ar.edu.unlp.dssd.dto.FuenteDatos;
import ar.edu.unlp.dssd.dto.IndicadoresDto;
import ar.edu.unlp.dssd.dto.LoteDetalleDto;
import ar.edu.unlp.dssd.dto.OfertasConjuntasIndicadorDto;
import ar.edu.unlp.dssd.dto.ProyectosIndicadorDto;
import ar.edu.unlp.dssd.dto.RankingOngItemDto;
import ar.edu.unlp.dssd.dto.RankingOngsDto;
import ar.edu.unlp.dssd.dto.SerieMensualDto;
import ar.edu.unlp.dssd.dto.TiemposEmergenciaDto;
import ar.edu.unlp.dssd.dto.TotalesDto;
import ar.edu.unlp.dssd.exception.RecursoNoEncontradoException;
import ar.edu.unlp.dssd.model.Convocatoria;
import ar.edu.unlp.dssd.model.Emergencia;
import ar.edu.unlp.dssd.model.EstadoConvocatoria;
import ar.edu.unlp.dssd.model.EstadoOferta;
import ar.edu.unlp.dssd.model.EtapaEmergencia;
import ar.edu.unlp.dssd.model.Oferta;
import ar.edu.unlp.dssd.model.Organizacion;
import ar.edu.unlp.dssd.model.Proyecto;
import ar.edu.unlp.dssd.repository.EmergenciaRepository;
import ar.edu.unlp.dssd.service.BonitaMetricasService.MetricasBonitaResumen;
import ar.edu.unlp.dssd.service.EtapaEmergenciaService.EstadoEmergencia;

/** [Entrega final] Indicadores del auditor combinando datos locales (DSSD_DB) con la API REST de Bonita */
@Service
public class IndicadoresService {

    private static final Set<EstadoOferta> ESTADOS_MOVILIZADOS = EnumSet.of(EstadoOferta.ADJUDICADA,
            EstadoOferta.FINALIZADA);
    private static final LocalDate DESDE_MINIMO = LocalDate.of(1900, 1, 1);
    private static final LocalDate HASTA_MAXIMO = LocalDate.of(9999, 12, 31);
    private static final double MS_POR_HORA = 3_600_000.0;
    private static final String FILTRO_SIN_PROYECTO = "SIN_PROYECTO";

    @Autowired
    private EmergenciaRepository emergenciaRepository;

    @Autowired
    private EtapaEmergenciaService etapaService;

    @Autowired
    private BonitaMetricasService bonitaMetricasService;

    @Transactional(readOnly = true)
    public IndicadoresDto indicadores(LocalDate desde, LocalDate hasta) {
        return indicadores(desde, hasta, null);
    }

    @Transactional(readOnly = true)
    public IndicadoresDto indicadores(LocalDate desde, LocalDate hasta, SesionBonita sesionBonita) {
        List<Evaluada> evaluadas = evaluar(desde, hasta);

        long convocatoriasPublicadas = 0;
        long convocatoriasConReapertura = 0;
        long convocatoriasConCoberturaParcial = 0;
        long proyectosEnCurso = 0;
        long proyectosFinalizados = 0;
        long sinProyecto = 0;
        DoubleSummaryStatistics duracionProyectosFinalizados = new DoubleSummaryStatistics();

        Map<EtapaEmergencia, Long> porEtapa = new EnumMap<>(EtapaEmergencia.class);
        Map<String, Long> porGravedad = new LinkedHashMap<>();
        Map<EstadoOferta, Long> porEstado = new EnumMap<>(EstadoOferta.class);
        Map<String, Long> suministrosPorTipo = new LinkedHashMap<>();

        long ofertasRecibidas = 0;
        long ofertasAdjudicadas = 0;
        long adjudicadasEnConjunta = 0;
        long unidadesAdjudicadas = 0;
        long unidadesEnConjunta = 0;
        long emergenciasConAdjudicacion = 0;
        long emergenciasConConsorcio = 0;

        // Cobertura de necesidades (DSSD_DB)
        long unidadesRequeridas = 0;
        long unidadesOfertadas = 0;
        long unidadesValidadas = 0;
        long unidadesAdjudicadasCobertura = 0;
        Map<String, AcumuladorCobertura> coberturaPorRecurso = new LinkedHashMap<>();

        // Ranking de ONGs (DSSD_DB)
        Map<String, AcumuladorOng> rankingPorOng = new LinkedHashMap<>();

        Map<Long, Emergencia> emergenciasPorCaseId = new LinkedHashMap<>();

        for (Evaluada evaluada : evaluadas) {
            Emergencia emergencia = evaluada.emergencia();
            EstadoEmergencia estado = evaluada.estado();

            if (emergencia.getCaseId() != null) {
                emergenciasPorCaseId.putIfAbsent(emergencia.getCaseId(), emergencia);
            }

            Convocatoria convocatoria = estado.convocatoria();
            if (convocatoria != null && convocatoria.getEstado() != EstadoConvocatoria.BORRADOR) {
                convocatoriasPublicadas++;
                if (convocatoria.getRonda() != null && convocatoria.getRonda() > 1) {
                    convocatoriasConReapertura++;
                }
                if (Boolean.TRUE.equals(convocatoria.getCoberturaParcialAceptada())) {
                    convocatoriasConCoberturaParcial++;
                }
            }

            for (LoteDetalleDto lote : estado.cobertura()) {
                long req = lote.cantidadRequerida() == null ? 0L : lote.cantidadRequerida();
                long ofe = lote.cantidadOfertada() == null ? 0L : lote.cantidadOfertada();
                unidadesRequeridas += req;
                unidadesOfertadas += ofe;
                unidadesValidadas += lote.cantidadValidada();
                unidadesAdjudicadasCobertura += lote.cantidadAdjudicada();
                String recurso = etiqueta(lote.tipoRecurso());
                AcumuladorCobertura accCob = coberturaPorRecurso.computeIfAbsent(recurso, k -> new AcumuladorCobertura());
                accCob.requeridas += req;
                accCob.ofertadas += ofe;
                accCob.adjudicadas += lote.cantidadAdjudicada();
            }

            Proyecto proyecto = emergencia.getProyecto();
            if (proyecto != null && Proyecto.EN_CURSO.equals(proyecto.getEstado())) {
                proyectosEnCurso++;
            } else if (proyecto != null && Proyecto.TERMINADO.equals(proyecto.getEstado())) {
                proyectosFinalizados++;
                Double duracion = duracionHoras(proyecto);
                if (duracion != null) {
                    duracionProyectosFinalizados.accept(duracion);
                }
            } else {
                sinProyecto++;
            }

            porEtapa.merge(estado.etapa(), 1L, Long::sum);
            porGravedad.merge(etiqueta(emergencia.getNivelGravedad()), 1L, Long::sum);

            for (Oferta oferta : estado.ofertas()) {
                if (oferta.getEstado() != null) {
                    porEstado.merge(oferta.getEstado(), 1L, Long::sum);
                }
                if (oferta.getEstado() != EstadoOferta.RETIRADA) {
                    ofertasRecibidas++;
                    registrarPresentacionOng(rankingPorOng, oferta);
                }
            }

            boolean tieneAdjudicada = false;
            boolean tieneConsorcio = false;
            for (Oferta oferta : estado.validas()) {
                if (ESTADOS_MOVILIZADOS.contains(oferta.getEstado())) {
                    long cantidad = cantidad(oferta);
                    tieneAdjudicada = true;
                    ofertasAdjudicadas++;
                    unidadesAdjudicadas += cantidad;
                    String recurso = oferta.getTipoRecurso() == null ? "Sin dato" : oferta.getTipoRecurso().getNombre();
                    suministrosPorTipo.merge(recurso, cantidad, Long::sum);
                    registrarAdjudicacionOng(rankingPorOng, oferta, cantidad);
                    if (oferta.getOfertaConjunta() != null) {
                        tieneConsorcio = true;
                        adjudicadasEnConjunta++;
                        unidadesEnConjunta += cantidad;
                    }
                }
            }
            if (tieneAdjudicada) {
                emergenciasConAdjudicacion++;
            }
            if (tieneConsorcio) {
                emergenciasConConsorcio++;
            }
        }

        TotalesDto totales = new TotalesDto(FuenteDatos.DSSD_DB, evaluadas.size(), convocatoriasPublicadas,
                ofertasRecibidas, ofertasAdjudicadas, proyectosEnCurso, proyectosFinalizados, unidadesAdjudicadas);
        ProyectosIndicadorDto proyectos = new ProyectosIndicadorDto(FuenteDatos.DSSD_DB, proyectosEnCurso,
                proyectosFinalizados, sinProyecto,
                porcentaje(proyectosFinalizados, proyectosEnCurso + proyectosFinalizados),
                duracionProyectosFinalizados.getCount() == 0 ? null
                        : redondearDosDecimales(duracionProyectosFinalizados.getAverage()));
        OfertasConjuntasIndicadorDto conjuntas = new OfertasConjuntasIndicadorDto(FuenteDatos.DSSD_DB,
                ofertasAdjudicadas, adjudicadasEnConjunta, porcentaje(adjudicadasEnConjunta, ofertasAdjudicadas),
                unidadesAdjudicadas, unidadesEnConjunta, porcentaje(unidadesEnConjunta, unidadesAdjudicadas),
                emergenciasConAdjudicacion, emergenciasConConsorcio,
                porcentaje(emergenciasConConsorcio, emergenciasConAdjudicacion));

        List<CoberturaRecursoDto> coberturaRecursos = coberturaPorRecurso.entrySet().stream()
                .map(entrada -> new CoberturaRecursoDto(
                        entrada.getKey(), entrada.getValue().requeridas, entrada.getValue().ofertadas,
                        entrada.getValue().adjudicadas,
                        porcentaje(entrada.getValue().adjudicadas, entrada.getValue().requeridas),
                        FuenteDatos.DSSD_DB))
                .sorted(Comparator.comparingLong(CoberturaRecursoDto::unidadesRequeridas).reversed())
                .toList();
        CoberturaNecesidadesDto coberturaNecesidades = new CoberturaNecesidadesDto(FuenteDatos.DSSD_DB,
                unidadesRequeridas, unidadesOfertadas, unidadesValidadas, unidadesAdjudicadasCobertura,
                porcentaje(unidadesOfertadas, unidadesRequeridas),
                porcentaje(unidadesAdjudicadasCobertura, unidadesRequeridas),
                convocatoriasConReapertura, convocatoriasConCoberturaParcial, coberturaRecursos);

        List<RankingOngItemDto> rankingItems = rankingPorOng.values().stream()
                .map(acc -> new RankingOngItemDto(acc.id, acc.nombre, acc.ofertasPresentadas,
                        acc.ofertasAdjudicadas, acc.unidadesMovilizadas, acc.participacionesEnConsorcio,
                        FuenteDatos.DSSD_DB))
                .sorted(Comparator.comparingLong(RankingOngItemDto::unidadesMovilizadas).reversed()
                        .thenComparing(Comparator.comparingLong(RankingOngItemDto::ofertasPresentadas).reversed())
                        .thenComparing(RankingOngItemDto::organizacionNombre))
                .toList();
        RankingOngsDto rankingOngs = new RankingOngsDto(FuenteDatos.DSSD_DB, rankingItems.size(), rankingItems);

        List<CantidadPorEtiquetaDto> etapas = new ArrayList<>();
        porEtapa.forEach((etapa, cantidad) -> etapas.add(new CantidadPorEtiquetaDto(etapa.name(), cantidad)));
        List<CantidadPorEtiquetaDto> estados = new ArrayList<>();
        porEstado.forEach((estadoOferta, cantidad) -> estados.add(new CantidadPorEtiquetaDto(estadoOferta.name(), cantidad)));

        MetricasBonitaResumen bonita = bonitaMetricasService.metricasProceso(emergenciasPorCaseId, sesionBonita);

        return new IndicadoresDto(FuenteDatos.COMBINADA, fuentesPorSeccion(), LocalDateTime.now().withNano(0),
                desde, hasta, totales, proyectos, ordenarPorCantidad(suministrosPorTipo), etapas,
                ordenarPorCantidad(porGravedad), estados, conjuntas, coberturaNecesidades, rankingOngs,
                bonita.tiemposPorEtapa(), bonita.cuellosDeBotellaBonita(),
                bonita.tiemposResolucionCruzados(), bonita.actividadUsuariosBonita(),
                bonita.estadosCasosBonita(), bonita.tiemposEsperaVsEjecucionBonita());
    }

    /** Evolución mensual (yyyy-MM) de emergencias, proyectos finalizados, suministros movilizados y consorcios. */
    @Transactional(readOnly = true)
    public List<SerieMensualDto> seriesMensuales(LocalDate desde, LocalDate hasta) {
        List<Evaluada> evaluadas = evaluar(desde, hasta);
        Map<YearMonth, AcumuladorMensual> porMes = new TreeMap<>();

        for (Evaluada evaluada : evaluadas) {
            Emergencia emergencia = evaluada.emergencia();
            EstadoEmergencia estado = evaluada.estado();

            YearMonth mesEmergencia = emergencia.getFechaCreacion() == null
                    ? null : YearMonth.from(emergencia.getFechaCreacion());
            if (mesEmergencia != null) {
                AcumuladorMensual acc = porMes.computeIfAbsent(mesEmergencia, k -> new AcumuladorMensual());
                acc.emergencias++;
                Proyecto proyecto = emergencia.getProyecto();
                if (proyecto != null && Proyecto.TERMINADO.equals(proyecto.getEstado())) {
                    YearMonth mesFin = proyecto.getFechaFin() != null
                            ? YearMonth.from(proyecto.getFechaFin()) : mesEmergencia;
                    porMes.computeIfAbsent(mesFin, k -> new AcumuladorMensual()).proyectosFinalizados++;
                }
            }

            for (Oferta oferta : estado.validas()) {
                if (!ESTADOS_MOVILIZADOS.contains(oferta.getEstado())) {
                    continue;
                }
                YearMonth mesMovimiento = oferta.getFechaAdjudicacion() != null
                        ? YearMonth.from(oferta.getFechaAdjudicacion())
                        : mesEmergencia;
                if (mesMovimiento == null) {
                    continue;
                }
                AcumuladorMensual acc = porMes.computeIfAbsent(mesMovimiento, k -> new AcumuladorMensual());
                long cantidad = cantidad(oferta);
                acc.suministrosMovilizados += cantidad;
                acc.ofertasAdjudicadas++;
                if (oferta.getOfertaConjunta() != null) {
                    acc.adjudicadasEnConjunta++;
                }
                String recurso = oferta.getTipoRecurso() == null ? "Sin dato" : oferta.getTipoRecurso().getNombre();
                acc.suministrosPorTipo.merge(recurso, cantidad, Long::sum);
            }
        }

        List<SerieMensualDto> resultado = new ArrayList<>();
        porMes.forEach((mes, acc) -> resultado.add(new SerieMensualDto(
                FuenteDatos.DSSD_DB, mes.toString(), acc.emergencias, acc.proyectosFinalizados,
                acc.suministrosMovilizados, acc.ofertasAdjudicadas, acc.adjudicadasEnConjunta,
                porcentaje(acc.adjudicadasEnConjunta, acc.ofertasAdjudicadas),
                ordenarPorCantidad(acc.suministrosPorTipo))));
        return resultado;
    }

    @Transactional(readOnly = true)
    public List<AuditoriaEmergenciaDto> emergencias(LocalDate desde, LocalDate hasta) {
        return emergencias(desde, hasta, null, null);
    }

    @Transactional(readOnly = true)
    public List<AuditoriaEmergenciaDto> emergencias(LocalDate desde, LocalDate hasta,
                                                    EtapaEmergencia filtroEtapa, String filtroEstadoProyecto) {
        return evaluar(desde, hasta).stream()
                .filter(evaluada -> filtroEtapa == null || evaluada.estado().etapa() == filtroEtapa)
                .filter(evaluada -> coincideEstadoProyecto(evaluada.emergencia().getProyecto(), filtroEstadoProyecto))
                .map(evaluada -> {
                    Emergencia e = evaluada.emergencia();
                    EstadoEmergencia estado = evaluada.estado();
                    List<Oferta> movilizadas = estado.validas().stream()
                            .filter(o -> ESTADOS_MOVILIZADOS.contains(o.getEstado()))
                            .toList();
                    long unidades = movilizadas.stream().mapToLong(IndicadoresService::cantidad).sum();
                    long recibidas = estado.ofertas().stream().filter(o -> o.getEstado() != EstadoOferta.RETIRADA).count();
                    long enConjunta = estado.validas().stream().filter(o -> o.getOfertaConjunta() != null).count();
                    boolean resolucionConConsorcio = movilizadas.stream().anyMatch(o -> o.getOfertaConjunta() != null);
                    Proyecto proyecto = e.getProyecto();
                    return new AuditoriaEmergenciaDto(FuenteDatos.DSSD_DB, e.getId(), e.getCaseId(),
                            e.getTipoDesastre(), e.getNivelGravedad(), e.getZonaAfectada(), e.getFechaCreacion(),
                            estado.etapa(),
                            estado.convocatoria() == null ? null : estado.convocatoria().getFechaCierre(),
                            recibidas, movilizadas.size(), unidades, enConjunta, resolucionConConsorcio,
                            proyecto == null ? null : proyecto.getEstado(),
                            proyecto == null ? null : proyecto.getFechaInicio(),
                            proyecto == null ? null : proyecto.getFechaFin(),
                            duracionHoras(proyecto));
                }).toList();
    }

    /** Detalle de tiempos de cada etapa de Bonita para una emergencia puntual. */
    @Transactional(readOnly = true)
    public TiemposEmergenciaDto tiemposEmergencia(Long emergenciaId, SesionBonita sesionBonita) {
        Emergencia emergencia = emergenciaRepository.findById(emergenciaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Emergencia no encontrada."));
        return bonitaMetricasService.tiemposDeEmergencia(emergencia, sesionBonita);
    }

    // Utilidades internas

    private static Map<String, String> fuentesPorSeccion() {
        Map<String, String> mapa = new LinkedHashMap<>();
        mapa.put("totales", FuenteDatos.DSSD_DB);
        mapa.put("proyectos", FuenteDatos.DSSD_DB);
        mapa.put("suministrosPorTipo", FuenteDatos.DSSD_DB);
        mapa.put("emergenciasPorEtapa", FuenteDatos.DSSD_DB);
        mapa.put("emergenciasPorGravedad", FuenteDatos.DSSD_DB);
        mapa.put("ofertasPorEstado", FuenteDatos.DSSD_DB);
        mapa.put("ofertasConjuntas", FuenteDatos.DSSD_DB);
        mapa.put("coberturaNecesidades", FuenteDatos.DSSD_DB);
        mapa.put("rankingOngs", FuenteDatos.DSSD_DB);
        mapa.put("tiemposPorEtapa", FuenteDatos.BONITA_API);
        mapa.put("cuellosDeBotellaBonita", FuenteDatos.BONITA_API);
        mapa.put("tiemposResolucionCruzados", FuenteDatos.COMBINADA);
        mapa.put("actividadUsuariosBonita", FuenteDatos.BONITA_API);
        mapa.put("estadosCasosBonita", FuenteDatos.BONITA_API);
        mapa.put("tiemposEsperaVsEjecucionBonita", FuenteDatos.BONITA_API);
        return mapa;
    }

    private static void registrarPresentacionOng(Map<String, AcumuladorOng> mapa, Oferta oferta) {
        AcumuladorOng acc = obtenerOng(mapa, oferta.getOrganizacion());
        if (acc == null) {
            return;
        }
        acc.ofertasPresentadas++;
    }

    private static void registrarAdjudicacionOng(Map<String, AcumuladorOng> mapa, Oferta oferta, long cantidad) {
        AcumuladorOng acc = obtenerOng(mapa, oferta.getOrganizacion());
        if (acc == null) {
            return;
        }
        acc.ofertasAdjudicadas++;
        acc.unidadesMovilizadas += cantidad;
        if (oferta.getOfertaConjunta() != null) {
            acc.participacionesEnConsorcio++;
        }
    }

    private static AcumuladorOng obtenerOng(Map<String, AcumuladorOng> mapa, Organizacion org) {
        if (org == null) {
            return null;
        }
        String nombre = etiqueta(org.getNombre());
        String clave = org.getId() != null ? "id:" + org.getId() : "nombre:" + nombre;
        return mapa.computeIfAbsent(clave, k -> new AcumuladorOng(org.getId(), nombre));
    }

    private record Evaluada(Emergencia emergencia, EstadoEmergencia estado) {
    }

    private static final class AcumuladorCobertura {
        long requeridas;
        long ofertadas;
        long adjudicadas;
    }

    private static final class AcumuladorOng {
        final Long id;
        final String nombre;
        long ofertasPresentadas;
        long ofertasAdjudicadas;
        long unidadesMovilizadas;
        long participacionesEnConsorcio;

        AcumuladorOng(Long id, String nombre) {
            this.id = id;
            this.nombre = nombre;
        }
    }

    private static final class AcumuladorMensual {
        long emergencias;
        long proyectosFinalizados;
        long suministrosMovilizados;
        long ofertasAdjudicadas;
        long adjudicadasEnConjunta;
        final Map<String, Long> suministrosPorTipo = new LinkedHashMap<>();
    }

    private List<Evaluada> evaluar(LocalDate desde, LocalDate hasta) {
        if (desde != null && hasta != null && desde.isAfter(hasta)) {
            throw new IllegalArgumentException("La fecha desde no puede ser posterior a la fecha hasta.");
        }
        List<Emergencia> emergencias = desde == null && hasta == null
                ? emergenciaRepository.findAllByOrderByIdDesc()
                : emergenciaRepository.findByFechaCreacionBetweenOrderByIdDesc(
                        desde == null ? DESDE_MINIMO : desde, hasta == null ? HASTA_MAXIMO : hasta);
        return emergencias.stream().map(e -> new Evaluada(e, etapaService.evaluar(e))).toList();
    }

    private static boolean coincideEstadoProyecto(Proyecto proyecto, String filtro) {
        if (filtro == null || filtro.isBlank()) {
            return true;
        }
        String normalizado = filtro.trim();
        if (FILTRO_SIN_PROYECTO.equalsIgnoreCase(normalizado)) {
            return proyecto == null;
        }
        return proyecto != null && normalizado.equalsIgnoreCase(proyecto.getEstado());
    }

    private static Double duracionHoras(Proyecto proyecto) {
        if (proyecto == null || proyecto.getFechaInicio() == null || proyecto.getFechaFin() == null) {
            return null;
        }
        long ms = Math.max(0L, Duration.between(proyecto.getFechaInicio(), proyecto.getFechaFin()).toMillis());
        return redondearDosDecimales(ms / MS_POR_HORA);
    }

    private static String etiqueta(String valor) {
        return valor == null || valor.isBlank() ? "Sin dato" : valor;
    }

    private static List<CantidadPorEtiquetaDto> ordenarPorCantidad(Map<String, Long> valores) {
        return valores.entrySet().stream()
                .map(entrada -> new CantidadPorEtiquetaDto(entrada.getKey(), entrada.getValue()))
                .sorted(Comparator.comparingLong(CantidadPorEtiquetaDto::cantidad).reversed())
                .toList();
    }

    private static double porcentaje(long parte, long total) {
        if (total == 0) {
            return 0.0;
        }
        return Math.round(1000.0 * parte / total) / 10.0;
    }

    private static double redondearDosDecimales(double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }

    private static long cantidad(Oferta oferta) {
        return oferta.getCantidad() == null ? 0L : oferta.getCantidad();
    }
}
