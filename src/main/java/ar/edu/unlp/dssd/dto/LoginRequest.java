package ar.edu.unlp.dssd.dto;

import jakarta.validation.constraints.NotBlank;

/** [Entrega final] Credenciales de ingreso (las mismas de Bonita) */
public record LoginRequest(
        @NotBlank(message = "Ingresá tu usuario")
        String username,

        @NotBlank(message = "Ingresá tu contraseña")
        String password) {
}
