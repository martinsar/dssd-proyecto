package ar.edu.unlp.dssd.seguridad;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.cors.CorsUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import ar.edu.unlp.dssd.exception.AccesoDenegadoException;
import ar.edu.unlp.dssd.exception.NoAutenticadoException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/** [Entrega final] Se valida el token o la clave de integración y el perfil de cada request a /api */
@Component
public class AutenticacionInterceptor implements HandlerInterceptor {

    public static final String ATRIBUTO_SESION = "rescuesync.sesion";
    public static final String HEADER_API_KEY = "X-Api-Key";

    private static final String PREFIJO_BEARER = "Bearer ";
    private static final String MENSAJE_SIN_PERMISO = "No tenés permisos para realizar esta acción.";

    private final AlmacenSesiones almacen;
    private final byte[] apiKey;

    public AutenticacionInterceptor(AlmacenSesiones almacen,
                                    @Value("${rescuesync.integracion.api-key}") String apiKey) {
        this.almacen = almacen;
        this.apiKey = apiKey == null ? new byte[0] : apiKey.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (CorsUtils.isPreFlightRequest(request) || !(handler instanceof HandlerMethod metodo)) {
            return true;
        }

        if (buscar(metodo, Publico.class) != null) {
            return true;
        }

        AccesoIntegracion integracion = buscar(metodo, AccesoIntegracion.class);
        RolesPermitidos roles = buscar(metodo, RolesPermitidos.class);

        // [Entrega final] Se valida la clave de integración que manda Bonita
        String claveRecibida = request.getHeader(HEADER_API_KEY);
        if (integracion != null && claveRecibida != null) {
            if (apiKey.length > 0
                    && MessageDigest.isEqual(apiKey, claveRecibida.getBytes(StandardCharsets.UTF_8))) {
                return true;
            }
            throw new NoAutenticadoException("Clave de integración inválida.");
        }

        // [Entrega final] Se valida el token del usuario
        String token = extraerToken(request);
        if (token == null) {
            throw new NoAutenticadoException("Iniciá sesión para continuar.");
        }
        SesionUsuario sesion = almacen.obtenerVigente(token)
                .orElseThrow(() -> new NoAutenticadoException("Tu sesión expiró. Iniciá sesión nuevamente."));

        // [Entrega final] Se valida el perfil
        if (integracion != null && roles == null) {
            throw new AccesoDenegadoException(MENSAJE_SIN_PERMISO);
        }
        if (roles != null && Arrays.stream(roles.value()).noneMatch(rol -> rol == sesion.rol())) {
            throw new AccesoDenegadoException(MENSAJE_SIN_PERMISO);
        }

        request.setAttribute(ATRIBUTO_SESION, sesion);
        return true;
    }

    /** Se busca la anotación en el método y, si no está, en la clase */
    private <A extends java.lang.annotation.Annotation> A buscar(HandlerMethod metodo, Class<A> tipo) {
        A enMetodo = AnnotatedElementUtils.findMergedAnnotation(metodo.getMethod(), tipo);
        if (enMetodo != null) {
            return enMetodo;
        }
        return AnnotatedElementUtils.findMergedAnnotation(metodo.getBeanType(), tipo);
    }

    private String extraerToken(HttpServletRequest request) {
        String autorizacion = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (autorizacion == null || !autorizacion.regionMatches(true, 0, PREFIJO_BEARER, 0, PREFIJO_BEARER.length())) {
            return null;
        }
        String token = autorizacion.substring(PREFIJO_BEARER.length()).trim();
        return token.isEmpty() ? null : token;
    }
}
