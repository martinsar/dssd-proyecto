package ar.edu.unlp.dssd.dto;

/** [Entrega final] Cobertura de necesidades por tipo de recurso (fuente: DSSD_DB) */
public record CoberturaRecursoDto(String recurso, long unidadesRequeridas, long unidadesOfertadas,
                                  long unidadesAdjudicadas, double porcentajeCoberturaAdjudicada, String fuente) {
}
