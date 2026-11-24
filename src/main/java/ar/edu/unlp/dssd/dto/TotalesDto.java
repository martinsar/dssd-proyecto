package ar.edu.unlp.dssd.dto;

/** [Entrega final] Totales del período para el auditor (fuente: DSSD_DB) */
public record TotalesDto(String fuente, long emergencias, long convocatoriasPublicadas, long ofertasRecibidas,
                         long ofertasAdjudicadas, long proyectosEnCurso, long proyectosFinalizados,
                         long suministrosMovilizados) {
}
