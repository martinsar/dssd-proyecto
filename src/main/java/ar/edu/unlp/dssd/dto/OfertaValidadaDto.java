package ar.edu.unlp.dssd.dto;

import ar.edu.unlp.dssd.model.EstadoOferta;
import ar.edu.unlp.dssd.model.Oferta;

/** [Entrega final] Lote validado que ve el municipio para seleccionar */
public record OfertaValidadaDto(Long id, String organizacion, Long tipoRecursoId, String tipoRecurso,
                                Integer cantidad, String descripcion, boolean conjunta, EstadoOferta estado) {

    public static OfertaValidadaDto desde(Oferta o) {
        return new OfertaValidadaDto(o.getId(),
                o.getOrganizacion() == null ? null : o.getOrganizacion().getNombre(),
                o.getTipoRecurso() == null ? null : o.getTipoRecurso().getId(),
                o.getTipoRecurso() == null ? null : o.getTipoRecurso().getNombre(),
                o.getCantidad(), o.getDescripcion(), o.getOfertaConjunta() != null, o.getEstado());
    }
}
