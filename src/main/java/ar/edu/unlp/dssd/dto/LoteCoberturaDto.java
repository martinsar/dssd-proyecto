package ar.edu.unlp.dssd.dto;

/** [Entrega final] Lote de la convocatoria con la cantidad requerida y la ofertada para su tipo de recurso. */
public record LoteCoberturaDto(Long necesidadId, Long tipoRecursoId, String codigoTipoRecurso, String tipoRecurso,
                               long cantidadRequerida, long cantidadOfertada) {
}
