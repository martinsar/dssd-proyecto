package ar.edu.unlp.dssd.dto;

import java.time.LocalDateTime;
import java.util.List;

/** [Entrega final] Detalle de tiempos por etapa de una emergencia en Bonita */
public record TiemposEmergenciaDto(Long emergenciaId, Long caseId, boolean disponible, String fuente, String mensaje,
                                   LocalDateTime fechaInicioCaso, LocalDateTime fechaFinCaso,
                                   Double duracionTotalHoras, List<EtapaEjecucionDto> etapas) {
}
