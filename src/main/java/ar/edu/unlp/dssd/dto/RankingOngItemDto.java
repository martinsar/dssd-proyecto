package ar.edu.unlp.dssd.dto;

/** [Entrega final] Participación de una ONG/organización en el período (fuente: DSSD_DB) */
public record RankingOngItemDto(Long organizacionId, String organizacionNombre, long ofertasPresentadas,
                                long ofertasAdjudicadas, long unidadesMovilizadas, long participacionesEnConsorcio,
                                String fuente) {
}
