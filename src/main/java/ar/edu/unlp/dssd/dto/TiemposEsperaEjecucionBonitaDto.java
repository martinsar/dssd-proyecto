package ar.edu.unlp.dssd.dto;

import java.util.List;

/** [Entrega final] Bloque 6 de Bonita: análisis de tiempo en espera (sin asignar) vs. tiempo de ejecución */
public record TiemposEsperaEjecucionBonitaDto(boolean disponible, String fuente, String mensaje,
                                              Double esperaPromedioGlobalHoras, Double ejecucionPromedioGlobalHoras,
                                              Double porcentajeEsperaGlobal,
                                              List<EsperaEjecucionEtapaDto> etapas) {
}
