package ar.edu.unlp.dssd.exception;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Traduce las excepciones del dominio a respuestas HTTP con body {"error": mensaje}. */
@RestControllerAdvice
public class ManejadorErrores {

    @ExceptionHandler(BonitaException.class)
    public ResponseEntity<Map<String, String>> manejarBonita(BonitaException e) {
        return respuesta(HttpStatus.BAD_GATEWAY, e);
    }

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<Map<String, String>> manejarNoEncontrado(RecursoNoEncontradoException e) {
        return respuesta(HttpStatus.NOT_FOUND, e);
    }

    @ExceptionHandler(ReglaNegocioException.class)
    public ResponseEntity<Map<String, String>> manejarReglaNegocio(ReglaNegocioException e) {
        return respuesta(HttpStatus.CONFLICT, e);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> manejarArgumentoInvalido(IllegalArgumentException e) {
        return respuesta(HttpStatus.BAD_REQUEST, e);
    }

    private ResponseEntity<Map<String, String>> respuesta(HttpStatus estado, Exception e) {
        return ResponseEntity.status(estado).body(Map.of("error", String.valueOf(e.getMessage())));
    }
}
