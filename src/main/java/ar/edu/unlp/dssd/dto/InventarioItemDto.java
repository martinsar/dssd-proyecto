package ar.edu.unlp.dssd.dto;

import java.time.LocalDateTime;

import ar.edu.unlp.dssd.model.RecursoInventario;

/** [Entrega final] Recurso del inventario; ofertado = PENDIENTE y VALIDADA, comprometido = SELECCIONADA y ADJUDICADA */
public record InventarioItemDto(Long tipoRecursoId, String tipoRecurso, Integer cantidadDisponible,
                                long cantidadOfertada, long cantidadComprometida, String descripcion,
                                LocalDateTime fechaActualizacion) {

    public static InventarioItemDto desde(RecursoInventario r, long ofertada, long comprometida) {
        return new InventarioItemDto(
                r.getTipoRecurso() == null ? null : r.getTipoRecurso().getId(),
                r.getTipoRecurso() == null ? null : r.getTipoRecurso().getNombre(),
                r.getCantidadDisponible(), ofertada, comprometida, r.getDescripcion(), r.getFechaActualizacion());
    }
}
