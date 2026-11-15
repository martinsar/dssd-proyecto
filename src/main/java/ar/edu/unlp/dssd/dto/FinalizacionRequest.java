package ar.edu.unlp.dssd.dto;

import jakarta.validation.constraints.Size;

/** [Entrega final] Informe opcional al finalizar la actividad de un lote */
public record FinalizacionRequest(
        @Size(max = 500, message = "El informe puede tener hasta 500 caracteres")
        String informe) {
}
