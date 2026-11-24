package ar.edu.unlp.dssd.dto;

import java.time.LocalDateTime;

/** [Entrega final] Ejecución de una etapa/tarea de Bonita para una emergencia (fuente: BONITA_API) */
public record EtapaEjecucionDto(String fuente, String etapa, String estado, LocalDateTime inicio, LocalDateTime fin,
                                Double duracionHoras) {
}
