package ar.edu.unlp.dssd.dto;

import java.time.LocalDateTime;

import ar.edu.unlp.dssd.model.EstadoOferta;
import ar.edu.unlp.dssd.model.Oferta;

/** [Entrega final] Lote en el seguimiento del coordinador (todos, con la validez de su oferta conjunta) */
public record OfertaSeguimientoDto(Long id, String organizacion, Long ofertaConjuntaId, boolean conjuntaValida,
                                   Long tipoRecursoId, String tipoRecurso, Integer cantidad, String descripcion,
                                   EstadoOferta estado, String nivelHabilitacion, String perfilCompetencia,
                                   Integer numeroVersion, LocalDateTime fechaModificacion,
                                   LocalDateTime fechaAdjudicacion, LocalDateTime fechaFinalizacion,
                                   String informeFinalizacion) {

    public static OfertaSeguimientoDto desde(Oferta o, boolean conjuntaValida) {
        return new OfertaSeguimientoDto(o.getId(),
                o.getOrganizacion() == null ? null : o.getOrganizacion().getNombre(),
                o.getOfertaConjunta() == null ? null : o.getOfertaConjunta().getId(),
                conjuntaValida,
                o.getTipoRecurso() == null ? null : o.getTipoRecurso().getId(),
                o.getTipoRecurso() == null ? null : o.getTipoRecurso().getNombre(),
                o.getCantidad(), o.getDescripcion(), o.getEstado(), o.getNivelAprobacion(),
                o.getPerfilCompetencia(), o.getNumeroVersion() == null ? 1 : o.getNumeroVersion(),
                o.getFechaModificacion(), o.getFechaAdjudicacion(), o.getFechaFinalizacion(),
                o.getInformeFinalizacion());
    }
}
