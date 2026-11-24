package ar.edu.unlp.dssd.dto;

import java.util.List;

/** [Entrega final] Evolución mensual de suministros movilizados, consorcios y proyectos (fuente: DSSD_DB) */
public record SerieMensualDto(String fuente, String periodo, long emergencias, long proyectosFinalizados,
                              long suministrosMovilizados, long ofertasAdjudicadas,
                              long adjudicadasEnConjunta, double porcentajeConsorcios,
                              List<CantidadPorEtiquetaDto> suministrosPorTipo) {
}
