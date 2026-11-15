package ar.edu.unlp.dssd.exception;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class ManejadorErroresTest {

    private final ManejadorErrores manejador = new ManejadorErrores();

    @Test
    void bonita_devuelveBadGatewayYElFormatoDeErrorExistente() {
        assertError(manejador.manejarBonita(new BonitaException("Bonita no disponible")),
                HttpStatus.BAD_GATEWAY, "Bonita no disponible");
    }

    @Test
    void recursoInexistente_devuelveNotFoundYElFormatoDeErrorExistente() {
        assertError(manejador.manejarNoEncontrado(new RecursoNoEncontradoException("No existe")),
                HttpStatus.NOT_FOUND, "No existe");
    }

    @Test
    void reglaDeNegocio_devuelveConflictYElFormatoDeErrorExistente() {
        assertError(manejador.manejarReglaNegocio(new ReglaNegocioException("Estado incompatible")),
                HttpStatus.CONFLICT, "Estado incompatible");
    }

    @Test
    void argumentoInvalido_devuelveBadRequestYElFormatoDeErrorExistente() {
        assertError(manejador.manejarArgumentoInvalido(new IllegalArgumentException("Dato invalido")),
                HttpStatus.BAD_REQUEST, "Dato invalido");
    }

    private void assertError(ResponseEntity<Map<String, String>> respuesta, HttpStatus estado, String mensaje) {
        assertThat(respuesta.getStatusCode()).isEqualTo(estado);
        assertThat(respuesta.getBody()).containsExactly(Map.entry("error", mensaje));
    }
}
