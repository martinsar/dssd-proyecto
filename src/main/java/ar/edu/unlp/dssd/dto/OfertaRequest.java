package ar.edu.unlp.dssd.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/** Datos de alta de una oferta. Acepta los nombres de IDs enviados por el formulario Angular. */
public record OfertaRequest(
        @NotNull(message = "La cantidad es obligatoria")
        @Positive(message = "La cantidad debe ser mayor que cero")
        Integer cantidad,

        @NotBlank(message = "La descripcion es obligatoria")
        String descripcion,

        @JsonAlias("tipo_recurso_id")
        @NotNull(message = "El tipo de recurso es obligatorio")
        @Positive(message = "El id del tipo de recurso debe ser mayor que cero")
        Long tipoRecursoId,

        // [Entrega final] Se ignora: la organización se toma de la sesión del usuario ONG
        @JsonAlias("organizacion_id")
        Long organizacionId,

        @JsonAlias("convocatoria_id")
        @NotNull(message = "La convocatoria es obligatoria")
        @Positive(message = "El id de la convocatoria debe ser mayor que cero")
        Long convocatoriaId,

        // [Entrega final] Oferta conjunta en la que se carga el lote (null si es individual)
        @JsonAlias("oferta_conjunta_id")
        @Positive(message = "El id de la oferta conjunta debe ser mayor que cero")
        Long ofertaConjuntaId) {
}
