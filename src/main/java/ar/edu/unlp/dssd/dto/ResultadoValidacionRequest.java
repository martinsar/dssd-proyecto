package ar.edu.unlp.dssd.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/** [Entrega final] Resultado binario de la validación de una oferta. */
public record ResultadoValidacionRequest(
        @NotNull(message = "El id de la oferta es obligatorio")
        @Positive(message = "El id de la oferta debe ser mayor que cero")
        Long ofertaId,

        @NotNull(message = "Hay que indicar si la oferta quedó habilitada")
        Boolean habilitada) {
}
