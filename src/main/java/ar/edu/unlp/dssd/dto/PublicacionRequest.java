package ar.edu.unlp.dssd.dto;

import java.time.LocalDateTime;

/** Body de la publicacion formal. fechaApertura es opcional (por defecto: ahora); fechaCierre es obligatoria. */
public record PublicacionRequest(LocalDateTime fechaApertura, LocalDateTime fechaCierre) {
}
