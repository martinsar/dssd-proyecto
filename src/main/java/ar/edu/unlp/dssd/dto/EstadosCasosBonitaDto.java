package ar.edu.unlp.dssd.dto;

import java.util.List;

/** [Entrega final] Bloque 5 de Bonita: salud del proceso, instancias activas/archivadas y tareas fallidas */
public record EstadosCasosBonitaDto(boolean disponible, String fuente, String mensaje,
                                    int versionesProcesoDesplegadas, int casosActivos, int casosFinalizados,
                                    int tareasFallidas, double tasaFinalizacionPorcentaje,
                                    List<ProcesoBonitaResumenDto> procesos) {
}
