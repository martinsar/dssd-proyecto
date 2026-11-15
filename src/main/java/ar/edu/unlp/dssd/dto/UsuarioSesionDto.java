package ar.edu.unlp.dssd.dto;

import ar.edu.unlp.dssd.seguridad.Rol;
import ar.edu.unlp.dssd.seguridad.SesionUsuario;

/** [Entrega final] Datos del usuario logueado */
public record UsuarioSesionDto(Long id, String username, String nombre, String apellido, Rol rol,
                               Long organizacionId, String organizacionNombre) {

    public static UsuarioSesionDto desde(SesionUsuario sesion) {
        return new UsuarioSesionDto(sesion.usuarioId(), sesion.username(), sesion.nombre(), sesion.apellido(),
                sesion.rol(), sesion.organizacionId(), sesion.organizacionNombre());
    }
}
