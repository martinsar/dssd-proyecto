package ar.edu.unlp.dssd.dto;

import java.util.List;

/** [Entrega final] Bloque local: nivel de cobertura de necesidades y rondas de convocatoria (fuente: DSSD_DB) */
public record CoberturaNecesidadesDto(String fuente, long unidadesRequeridas, long unidadesOfertadas,
                                      long unidadesValidadas, long unidadesAdjudicadas,
                                      double porcentajeCoberturaOfertada, double porcentajeCoberturaAdjudicada,
                                      long convocatoriasConReapertura, long convocatoriasConCoberturaParcial,
                                      List<CoberturaRecursoDto> porTipoRecurso) {
}
