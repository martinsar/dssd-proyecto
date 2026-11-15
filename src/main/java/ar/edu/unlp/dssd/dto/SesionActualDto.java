package ar.edu.unlp.dssd.dto;

import java.time.LocalDateTime;

/** [Entrega final] Sesión vigente: vencimiento y usuario */
public record SesionActualDto(LocalDateTime expiraEn, UsuarioSesionDto usuario) {
}
