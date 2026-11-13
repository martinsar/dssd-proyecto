package ar.edu.unlp.dssd.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

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

    private Operation operationWithOkResponse() {
        return new Operation().responses(new ApiResponses()
                .addApiResponse("200", new ApiResponse().description("OK")));
    }
}
