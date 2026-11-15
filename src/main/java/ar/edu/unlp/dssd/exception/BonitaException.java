package ar.edu.unlp.dssd.exception;

/** Falla de comunicacion o de respuesta inesperada al hablar con Bonita. Se traduce a HTTP 502. */
public class BonitaException extends RuntimeException {

    public BonitaException(String mensaje) {
        super(mensaje);
    }

    public BonitaException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
