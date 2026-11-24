package ar.edu.unlp.dssd.dto;

import java.util.List;

/** [Entrega final] Bloque 1 de Bonita: tiempos promedio por etapa y resolución total del proceso */
public record TiemposEtapasDto(boolean disponible, String fuente, String mensaje, int casosAnalizados,
                               int casosFinalizadosEnBonita, Double promedioResolucionTotalHoras,
                               List<TiempoEtapaDto> etapas) {
}
