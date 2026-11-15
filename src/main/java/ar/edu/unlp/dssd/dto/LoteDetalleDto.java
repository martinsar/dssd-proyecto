package ar.edu.unlp.dssd.dto;

/**
 * [Entrega final] Cobertura de un lote: ofertado (null para el municipio), validado
 * (VALIDADA, SELECCIONADA, ADJUDICADA, NO_SELECCIONADA, FINALIZADA) y adjudicado (SELECCIONADA, ADJUDICADA, FINALIZADA).
 */
public record LoteDetalleDto(Long necesidadId, Long tipoRecursoId, String codigoTipoRecurso, String tipoRecurso,
                             String descripcion, Integer cantidadRequerida, Long cantidadOfertada,
                             long cantidadValidada, long cantidadAdjudicada) {

    /** Se oculta lo ofertado */
    public LoteDetalleDto sinOfertado() {
        return new LoteDetalleDto(necesidadId, tipoRecursoId, codigoTipoRecurso, tipoRecurso, descripcion,
                cantidadRequerida, null, cantidadValidada, cantidadAdjudicada);
    }
}
