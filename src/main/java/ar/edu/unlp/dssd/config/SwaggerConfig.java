package ar.edu.unlp.dssd.config; // <-- Esta es la línea que soluciona el error

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API de Gestión de Desastres - DSSD")
                        .version("1.0.0")
                        .description("API REST para la gestión de emergencias, convocatorias, necesidades, ofertas, organizaciones, proyectos, usuarios y catálogos."));
    }

    @Bean
    public OpenApiCustomizer restResponsesCustomizer() {
        return openApi -> openApi.getPaths().forEach((path, pathItem) ->
                pathItem.readOperationsMap().forEach((method, operation) ->
                        documentResponses(path, method, operation)));
    }

    private void documentResponses(String path, PathItem.HttpMethod method, Operation operation) {
        ApiResponses responses = operation.getResponses();
        ApiResponse success = responses.get("200");

        if (method == PathItem.HttpMethod.POST && !path.endsWith("/publicar")) {
            responses.remove("200");
            if (success == null) {
                success = new ApiResponse();
            }
            success.setDescription("Recurso creado correctamente.");
            responses.addApiResponse("201", success);
        } else if (method == PathItem.HttpMethod.POST || method == PathItem.HttpMethod.PUT
                || method == PathItem.HttpMethod.GET) {
            if (success != null) {
                success.setDescription(method == PathItem.HttpMethod.GET
                        ? "Solicitud procesada correctamente."
                        : "Operación completada correctamente.");
            }
        } else if (method == PathItem.HttpMethod.DELETE) {
            responses.remove("200");
            responses.addApiResponse("204", new ApiResponse()
                    .description("Recurso eliminado correctamente; la respuesta no contiene cuerpo."));
        }

        if (operation.getRequestBody() != null) {
            addResponse(responses, "400", "Solicitud inválida o datos de entrada incorrectos.");
            addResponse(responses, "415", "El tipo de contenido enviado no es compatible.");
        }

        if (path.contains("/{id}")) {
            addResponse(responses, "400", "El identificador debe tener un formato válido.");
        }

        if (path.contains("/{id}") && (method != PathItem.HttpMethod.GET
                || !path.equals("/api/emergencias/{id}/convocatorias"))) {
            addResponse(responses, "404", "No se encontró el recurso o una relación requerida.");
        }

        if (isBusinessRuleOperation(path, method)) {
            addResponse(responses, "409", "La operación entra en conflicto con el estado o las reglas del recurso.");
        }

        if (isBonitaOperation(path, method)) {
            addResponse(responses, "502", "No se pudo completar la operación con el servicio Bonita.");
        }

        addResponse(responses, "500", "Error interno inesperado al procesar la solicitud.");
    }

    private boolean isBusinessRuleOperation(String path, PathItem.HttpMethod method) {
        if (method == PathItem.HttpMethod.GET) {
            return false;
        }
        return path.startsWith("/api/necesidades")
                || path.startsWith("/api/convocatorias")
                || (path.equals("/api/emergencias/{id}/convocatorias")
                    && method == PathItem.HttpMethod.POST);
    }

    private boolean isBonitaOperation(String path, PathItem.HttpMethod method) {
        return (path.equals("/api/emergencias") && method == PathItem.HttpMethod.POST)
                || (path.equals("/api/emergencias/{id}/convocatorias") && method == PathItem.HttpMethod.POST)
                || (path.equals("/api/convocatorias/{id}/publicar") && method == PathItem.HttpMethod.POST);
    }

    private void addResponse(ApiResponses responses, String code, String description) {
        if (!responses.containsKey(code)) {
            responses.addApiResponse(code, new ApiResponse().description(description));
        }
    }
}