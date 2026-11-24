package ar.edu.unlp.dssd.dto;

import java.util.List;

/** [Entrega final] Bloque 2 de Bonita: casos activos y tareas humanas pendientes por etapa (cuellos de botella) */
public record CuellosDeBotellaBonitaDto(boolean disponible, String fuente, String mensaje, int casosActivosEnBonita,
                                        int totalTareasPendientes, List<TareaPendienteEtapaDto> pendientesPorEtapa) {
}
