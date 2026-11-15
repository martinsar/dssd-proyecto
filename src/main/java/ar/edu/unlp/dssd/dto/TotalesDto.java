package ar.edu.unlp.dssd.dto;

/** [Entrega final] Totales del período para el auditor */
public record TotalesDto(long emergencias, long convocatoriasPublicadas, long ofertasRecibidas, long ofertasAdjudicadas,
                         long proyectosEnCurso, long proyectosFinalizados, long suministrosMovilizados) {
}
