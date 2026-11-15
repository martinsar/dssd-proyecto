package ar.edu.unlp.dssd.dto;

/** [Entrega final] Peso de las ofertas conjuntas en lo adjudicado (porcentajes con un decimal) */
public record OfertasConjuntasIndicadorDto(long ofertasAdjudicadas, long adjudicadasEnConjunta,
                                           double porcentajeOfertas, long unidadesAdjudicadas,
                                           long unidadesEnConjunta, double porcentajeUnidades) {
}
