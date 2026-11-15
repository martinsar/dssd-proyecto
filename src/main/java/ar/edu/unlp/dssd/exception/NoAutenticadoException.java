package ar.edu.unlp.dssd.exception;

/** [Entrega final] Sin sesión, sesión vencida o credenciales inválidas (HTTP 401) */
public class NoAutenticadoException extends RuntimeException {

    public NoAutenticadoException(String mensaje) {
        super(mensaje);
    }
}
