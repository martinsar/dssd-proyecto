package ar.edu.unlp.dssd.exception;

/** [Entrega final] El perfil no tiene permiso para la acción (HTTP 403) */
public class AccesoDenegadoException extends RuntimeException {

    public AccesoDenegadoException(String mensaje) {
        super(mensaje);
    }
}
