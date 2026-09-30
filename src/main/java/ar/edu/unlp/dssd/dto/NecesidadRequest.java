package ar.edu.unlp.dssd.dto;

/** Datos de entrada para agregar un lote (necesidad) a una convocatoria. */
public record NecesidadRequest(Long tipoRecursoId, Integer cantidad, String descripcion) {
}
