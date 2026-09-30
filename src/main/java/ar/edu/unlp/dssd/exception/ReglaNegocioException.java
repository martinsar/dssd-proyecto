package ar.edu.unlp.dssd.exception;

/** La operacion viola una regla de negocio (ej: estado incorrecto). Se traduce a HTTP 409. */
public class ReglaNegocioException extends RuntimeException {

    public ReglaNegocioException(String mensaje) {
        super(mensaje);
    }
}
