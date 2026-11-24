package ar.edu.unlp.dssd.dto;

/** [Entrega final] Peso de las ofertas conjuntas (consorcios) en lo adjudicado (fuente: DSSD_DB) */
public record OfertasConjuntasIndicadorDto(String fuente, long ofertasAdjudicadas, long adjudicadasEnConjunta,
                                           double porcentajeOfertas, long unidadesAdjudicadas,
                                           long unidadesEnConjunta, double porcentajeUnidades,
                                           long emergenciasConAdjudicacion, long emergenciasConConsorcio,
                                           double porcentajeEmergenciasConConsorcio) {
}
