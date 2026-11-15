package ar.edu.unlp.dssd.dto;

import java.util.List;

/** [Entrega final] Tiempos promedio por etapa del proceso (fuente: Bonita) */
public record TiemposEtapasDto(boolean disponible, String fuente, List<TiempoEtapaDto> etapas) {
}
