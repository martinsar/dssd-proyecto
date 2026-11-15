package ar.edu.unlp.dssd.exception;

import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

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

    // [Entrega final] Se responde 401 sin sesión, con sesión vencida o con credenciales inválidas
    @ExceptionHandler(NoAutenticadoException.class)
    public ResponseEntity<Map<String, String>> manejarNoAutenticado(NoAutenticadoException e) {
        return respuesta(HttpStatus.UNAUTHORIZED, e);
    }

    // [Entrega final] Se responde 403 cuando el perfil no tiene permiso
    @ExceptionHandler(AccesoDenegadoException.class)
    public ResponseEntity<Map<String, String>> manejarAccesoDenegado(AccesoDenegadoException e) {
        return respuesta(HttpStatus.FORBIDDEN, e);
    }

    // [Entrega final] Se responde 400 con JSON mal formado o valores que no se pueden convertir
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> manejarCuerpoIlegible(HttpMessageNotReadableException e) {
        return ResponseEntity.badRequest().body(Map.of("error", "Los datos enviados no son válidos."));
    }

    // [Entrega final] Se responde 400 con parámetros de la URL que no se pueden convertir (ej: etapa o fecha)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, String>> manejarParametroInvalido(MethodArgumentTypeMismatchException e) {
        return ResponseEntity.badRequest().body(Map.of("error", "El valor de '" + e.getName() + "' no es válido."));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> manejarValidacion(MethodArgumentNotValidException e) {
        String mensaje = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return ResponseEntity.badRequest().body(Map.of("error", mensaje));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> manejarArgumentoInvalido(IllegalArgumentException e) {
        return respuesta(HttpStatus.BAD_REQUEST, e);
    }

    private ResponseEntity<Map<String, String>> respuesta(HttpStatus estado, Exception e) {
        return ResponseEntity.status(estado).body(Map.of("error", String.valueOf(e.getMessage())));
    }
}
