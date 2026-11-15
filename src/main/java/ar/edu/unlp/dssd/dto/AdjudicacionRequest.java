package ar.edu.unlp.dssd.dto;

import java.util.List;

/** [Entrega final] Lotes VALIDADA que elige el municipio */
public record AdjudicacionRequest(List<Long> ofertaIds) {
}
