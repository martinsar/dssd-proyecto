package ar.edu.unlp.dssd.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/** [Entrega final] Indicadores combinados del tablero del auditor (DSSD_DB + 6 bloques de BONITA_API) */
public record IndicadoresDto(String fuente, Map<String, String> fuentesPorSeccion, LocalDateTime generadoEn,
                             LocalDate desde, LocalDate hasta, TotalesDto totales,
                             ProyectosIndicadorDto proyectos,
                             List<CantidadPorEtiquetaDto> suministrosPorTipo,
                             List<CantidadPorEtiquetaDto> emergenciasPorEtapa,
                             List<CantidadPorEtiquetaDto> emergenciasPorGravedad,
                             List<CantidadPorEtiquetaDto> ofertasPorEstado,
                             OfertasConjuntasIndicadorDto ofertasConjuntas,
                             CoberturaNecesidadesDto coberturaNecesidades,
                             RankingOngsDto rankingOngs,
                             TiemposEtapasDto tiemposPorEtapa,
                             CuellosDeBotellaBonitaDto cuellosDeBotellaBonita,
                             TiemposResolucionCruzadosDto tiemposResolucionCruzados,
                             ActividadUsuariosBonitaDto actividadUsuariosBonita,
                             EstadosCasosBonitaDto estadosCasosBonita,
                             TiemposEsperaEjecucionBonitaDto tiemposEsperaVsEjecucionBonita) {
}
