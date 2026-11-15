package ar.edu.unlp.dssd.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import ar.edu.unlp.dssd.controller.AuditorController;
import ar.edu.unlp.dssd.controller.AuthController;
import ar.edu.unlp.dssd.controller.ConvocatoriaController;
import ar.edu.unlp.dssd.controller.CoordinadorController;
import ar.edu.unlp.dssd.controller.EmergenciaController;
import ar.edu.unlp.dssd.controller.MunicipioController;
import ar.edu.unlp.dssd.controller.NecesidadController;
import ar.edu.unlp.dssd.controller.NotificacionController;
import ar.edu.unlp.dssd.controller.OfertaConjuntaController;
import ar.edu.unlp.dssd.controller.OfertaController;
import ar.edu.unlp.dssd.controller.OngController;
import ar.edu.unlp.dssd.controller.OrganizacionController;
import ar.edu.unlp.dssd.controller.ProyectoController;
import ar.edu.unlp.dssd.controller.TipoEmergenciaController;
import ar.edu.unlp.dssd.controller.TipoOrganizacionController;
import ar.edu.unlp.dssd.controller.TipoRecursoController;
import ar.edu.unlp.dssd.controller.UsuarioController;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;

class SwaggerConfigTest {

    private final SwaggerConfig config = new SwaggerConfig();

    @Test
    void documentaCodigosRestCorrectosParaCrearEliminarYPublicar() {
        Operation crear = operationWithOkResponse();
        Operation eliminar = operationWithOkResponse();
        Operation publicar = operationWithOkResponse();

        OpenAPI openApi = new OpenAPI().paths(new Paths()
                .addPathItem("/api/tipos-organizacion", new PathItem().post(crear))
                .addPathItem("/api/tipos-organizacion/{id}", new PathItem().delete(eliminar))
                .addPathItem("/api/convocatorias/{id}/publicar", new PathItem().post(publicar)));

        config.restResponsesCustomizer().customise(openApi);

        assertThat(crear.getResponses()).containsKey("201").doesNotContainKey("200");
        assertThat(eliminar.getResponses()).containsKey("204").doesNotContainKey("200");
        assertThat(eliminar.getResponses()).containsKey("404");
        assertThat(publicar.getResponses()).containsKey("200");
        assertThat(publicar.getResponses()).containsKeys("400", "404", "409", "502", "500");
    }

    @Test
    void conservaElCuerpoDeExitoAlCambiarPostDe200A201() {
        Operation crear = operationWithOkResponse();
        ApiResponse success = crear.getResponses().get("200");
        success.setContent(new io.swagger.v3.oas.models.media.Content());

        OpenAPI openApi = new OpenAPI().paths(new Paths()
                .addPathItem("/api/usuarios", new PathItem().post(crear)));

        config.restResponsesCustomizer().customise(openApi);

        assertThat(crear.getResponses().get("201").getContent()).isSameAs(success.getContent());
    }

    @Test
    void todosLosEndpointsTienenResumenYDescripcion() {
        List<Class<?>> controllers = List.of(
                AuditorController.class,
                AuthController.class,
                ConvocatoriaController.class,
                CoordinadorController.class,
                EmergenciaController.class,
                MunicipioController.class,
                NecesidadController.class,
                NotificacionController.class,
                OfertaConjuntaController.class,
                OfertaController.class,
                OngController.class,
                OrganizacionController.class,
                ProyectoController.class,
                TipoEmergenciaController.class,
                TipoOrganizacionController.class,
                TipoRecursoController.class,
                UsuarioController.class);

        for (Class<?> controller : controllers) {
            for (Method method : controller.getDeclaredMethods()) {
                if (isEndpoint(method)) {
                    io.swagger.v3.oas.annotations.Operation operation =
                            method.getAnnotation(io.swagger.v3.oas.annotations.Operation.class);
                    assertThat(operation)
                            .as("%s.%s debe tener @Operation", controller.getSimpleName(), method.getName())
                            .isNotNull();
                    assertThat(operation.summary()).isNotBlank();
                    assertThat(operation.description()).isNotBlank();
                }
            }
        }
    }

    @Test
    void documentaSeguridadYRespuestas401Y403() {
        Operation listar = operationWithOkResponse();
        Operation login = operationWithOkResponse();
        Operation validaciones = operationWithOkResponse();
        Operation consolidado = operationWithOkResponse();

        OpenAPI openApi = config.customOpenAPI().paths(new Paths()
                .addPathItem("/api/ofertas", new PathItem().get(listar))
                .addPathItem("/api/auth/login", new PathItem().post(login))
                .addPathItem("/api/emergencias/{id}/ofertas/validaciones", new PathItem().post(validaciones))
                .addPathItem("/api/emergencias/{id}/ofertas", new PathItem().get(consolidado)));

        config.restResponsesCustomizer().customise(openApi);

        assertThat(openApi.getComponents().getSecuritySchemes())
                .containsKeys(SwaggerConfig.ESQUEMA_BEARER, SwaggerConfig.ESQUEMA_API_KEY);
        assertThat(listar.getResponses()).containsKeys("200", "401", "403");
        assertThat(listar.getSecurity()).hasSize(1);
        assertThat(login.getResponses()).containsKeys("200", "401", "403", "502").doesNotContainKey("201");
        assertThat(login.getSecurity()).isNull();
        assertThat(validaciones.getResponses()).containsKeys("200", "401", "403", "404").doesNotContainKey("201");
        assertThat(validaciones.getSecurity()).hasSize(2);
        assertThat(consolidado.getSecurity()).hasSize(2);
    }

    private boolean isEndpoint(Method method) {
        return method.isAnnotationPresent(RequestMapping.class)
                || method.isAnnotationPresent(GetMapping.class)
                || method.isAnnotationPresent(PostMapping.class)
                || method.isAnnotationPresent(PutMapping.class)
                || method.isAnnotationPresent(PatchMapping.class)
                || method.isAnnotationPresent(DeleteMapping.class);
    }

    private Operation operationWithOkResponse() {
        return new Operation().responses(new ApiResponses()
                .addApiResponse("200", new ApiResponse().description("OK")));
    }
}
