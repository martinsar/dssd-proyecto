package ar.edu.unlp.dssd.config; // <-- Esta es la línea que soluciona el error

import java.util.Set;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springdoc.core.utils.SpringDocUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import ar.edu.unlp.dssd.seguridad.SesionUsuario;

@Configuration
public class SwaggerConfig {

    // [Entrega final] Esquemas de seguridad: token de la app y clave de integración de Bonita
    public static final String ESQUEMA_BEARER = "bearer";
    public static final String ESQUEMA_API_KEY = "apiKey";

    // [Entrega final] Endpoints que no exigen sesión
    private static final Set<String> RUTAS_PUBLICAS = Set.of("/api/auth/login");

    // [Entrega final] Endpoints que también aceptan X-Api-Key (los consume Bonita)
    private static final Set<String> RUTAS_INTEGRACION = Set.of(
            "/api/emergencias/{id}/ofertas",
            "/api/emergencias/{id}/ofertas/validaciones",
            "/api/emergencias/{id}/adjudicacion/notificaciones",
            "/api/convocatorias/{id}/ofertas/validacion");

    // [Entrega final] POST que responden 200 en lugar de 201
    private static final Set<String> POST_SIN_CREACION = Set.of(
            "/api/auth/login",
            "/api/emergencias/{id}/ofertas/validaciones",
            "/api/emergencias/{id}/adjudicacion/notificaciones",
            "/api/ong/ofertas/{id}/retiro",
            "/api/ong/ofertas/{id}/finalizacion",
            "/api/municipio/emergencias/{id}/adjudicacion",
            "/api/coordinador/convocatorias/{id}/decision");

    // [Entrega final] POST que responden 204 sin cuerpo
    private static final Set<String> POST_SIN_CONTENIDO = Set.of(
            "/api/auth/logout",
            "/api/ong/notificaciones/{id}/lectura",
            "/api/ong/notificaciones/lectura");

    // [Entrega final] Operaciones que pueden responder 409 por reglas de negocio
    private static final Set<String> PREFIJOS_REGLAS_NEGOCIO = Set.of(
            "/api/necesidades", "/api/convocatorias", "/api/ofertas", "/api/ong/ofertas",
            "/api/municipio", "/api/coordinador");

    // [Entrega final] Operaciones que llaman a Bonita
    private static final Set<String> RUTAS_BONITA = Set.of(
            "/api/auth/login",
            "/api/emergencias",
            "/api/emergencias/{id}/convocatorias",
            "/api/convocatorias/{id}/publicar",
            "/api/municipio/emergencias/{id}/adjudicacion",
            "/api/ong/ofertas/{id}/finalizacion");

    static {
        // [Entrega final] La sesión la inyecta el servidor: no es un parámetro de la API
        SpringDocUtils.getConfig().addRequestWrapperToIgnore(SesionUsuario.class);
    }

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API de Gestión de Desastres - DSSD")
                        .version("1.0.0")
                        .description("API REST para la gestión de emergencias, convocatorias, necesidades, ofertas, organizaciones, proyectos, usuarios y catálogos."))
                .components(new Components()
                        .addSecuritySchemes(ESQUEMA_BEARER, new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .description("Token devuelto por POST /api/auth/login."))
                        .addSecuritySchemes(ESQUEMA_API_KEY, new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.HEADER)
                                .name("X-Api-Key")
                                .description("Clave de integración que envía Bonita.")));
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

        if (POST_SIN_CONTENIDO.contains(path)) {
            responses.remove("200");
            responses.addApiResponse("204", new ApiResponse()
                    .description(path.equals("/api/auth/logout")
                            ? "Sesión cerrada correctamente; la respuesta no contiene cuerpo."
                            : "Operación completada correctamente; la respuesta no contiene cuerpo."));
        } else if (method == PathItem.HttpMethod.POST && !path.endsWith("/publicar")
                && !POST_SIN_CREACION.contains(path)) {
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

        if (path.contains("/{")) {
            addResponse(responses, "400", "El identificador debe tener un formato válido.");
        }

        if (path.contains("/{") && (method != PathItem.HttpMethod.GET
                || !path.equals("/api/emergencias/{id}/convocatorias"))) {
            addResponse(responses, "404", "No se encontró el recurso o una relación requerida.");
        }

        if (isBusinessRuleOperation(path, method)) {
            addResponse(responses, "409", "La operación entra en conflicto con el estado o las reglas del recurso.");
        }

        if (isBonitaOperation(path, method)) {
            addResponse(responses, "502", "No se pudo completar la operación con el servicio Bonita.");
        }

        documentSecurity(path, operation, responses);

        addResponse(responses, "500", "Error interno inesperado al procesar la solicitud.");
    }

    /** [Entrega final] Se documentan el esquema de seguridad y las respuestas 401/403 */
    private void documentSecurity(String path, Operation operation, ApiResponses responses) {
        if (path.equals("/api/auth/login")) {
            addResponse(responses, "401", "Usuario o contraseña incorrectos.");
            addResponse(responses, "403", "El usuario no está habilitado en RescueSync o no tiene organización asociada.");
            return;
        }
        if (RUTAS_PUBLICAS.contains(path) || !path.startsWith("/api/")) {
            return;
        }
        operation.addSecurityItem(new SecurityRequirement().addList(ESQUEMA_BEARER));
        if (RUTAS_INTEGRACION.contains(path)) {
            operation.addSecurityItem(new SecurityRequirement().addList(ESQUEMA_API_KEY));
        }
        addResponse(responses, "401", "Falta el token, la sesión expiró o la clave de integración es inválida.");
        addResponse(responses, "403", "El perfil del usuario no tiene permiso para esta operación o recurso.");
    }

    private boolean isBusinessRuleOperation(String path, PathItem.HttpMethod method) {
        if (method == PathItem.HttpMethod.GET) {
            return false;
        }
        return PREFIJOS_REGLAS_NEGOCIO.stream().anyMatch(path::startsWith)
                || (path.equals("/api/emergencias/{id}/convocatorias")
                    && method == PathItem.HttpMethod.POST)
                || path.equals("/api/emergencias/{id}/ofertas/validaciones");
    }

    private boolean isBonitaOperation(String path, PathItem.HttpMethod method) {
        return method == PathItem.HttpMethod.POST && RUTAS_BONITA.contains(path);
    }

    private void addResponse(ApiResponses responses, String code, String description) {
        if (!responses.containsKey(code)) {
            responses.addApiResponse(code, new ApiResponse().description(description));
        }
    }
}