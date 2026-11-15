package ar.edu.unlp.dssd.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

/** [Entrega final] Resultados de la validación del Sistema Nacional que registra Bonita. */
public record ValidacionesRequest(
        @NotEmpty(message = "Hay que informar al menos un resultado")
        List<@Valid ResultadoValidacionRequest> resultados) {
}
