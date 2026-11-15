package ar.edu.unlp.dssd.dto;

import ar.edu.unlp.dssd.model.EstadoOferta;

/** [Entrega final] Oferta del listado consolidado, con los códigos que entiende el Sistema Nacional. */
public record OfertaConsolidadaDto(Long ofertaId, EstadoOferta estado, Long organizacionId, String codigoOng,
                                   String organizacion, Long tipoRecursoId, String codigoTipoRecurso,
                                   String tipoRecurso, Integer cantidad, String descripcion) {
}
