package ar.edu.unlp.dssd.dto;

import java.util.List;

/** [Entrega final] Bloque local: ranking de ONGs/organizaciones por suministros movilizados y ofertas (fuente: DSSD_DB) */
public record RankingOngsDto(String fuente, int totalOrganizacionesParticipantes,
                             List<RankingOngItemDto> organizaciones) {
}
