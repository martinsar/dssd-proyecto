package ar.edu.unlp.dssd.dto;

import java.time.LocalDateTime;

/** [Entrega final] Respuesta del login: token, vencimiento y usuario */
public record LoginResponse(String token, LocalDateTime expiraEn, UsuarioSesionDto usuario) {
}
