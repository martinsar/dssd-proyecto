package ar.edu.unlp.dssd.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/** [Entrega final] Modificación de un lote; versionEsperada evita pisar cambios ajenos */
public record OfertaEdicionRequest(
        @NotNull(message = "El tipo de recurso es obligatorio")
        @Positive(message = "El id del tipo de recurso debe ser mayor que cero")
        Long tipoRecursoId,

        @NotNull(message = "La cantidad es obligatoria")
        @Positive(message = "La cantidad debe ser mayor que cero")
        Integer cantidad,

        @NotBlank(message = "La descripción es obligatoria")
        String descripcion,

        @NotNull(message = "Falta la versión del lote que se está modificando")
        Integer versionEsperada) {
}
