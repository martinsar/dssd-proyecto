package ar.edu.unlp.dssd.dto;

/** [Entrega final] Resumen de estado y duración de proyectos del período (fuente: DSSD_DB) */
public record ProyectosIndicadorDto(String fuente, long enCurso, long finalizados, long sinProyecto,
                                    double porcentajeFinalizados, Double duracionPromedioFinalizadosHoras) {
}
