package ar.edu.unlp.dssd.seguridad;

import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import ar.edu.unlp.dssd.exception.NoAutenticadoException;

/** [Entrega final] Se inyecta la sesión validada por el interceptor en los controllers */
public class SesionUsuarioResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return SesionUsuario.class.equals(parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        Object sesion = webRequest.getAttribute(AutenticacionInterceptor.ATRIBUTO_SESION,
                RequestAttributes.SCOPE_REQUEST);
        if (sesion instanceof SesionUsuario sesionUsuario) {
            return sesionUsuario;
        }
        throw new NoAutenticadoException("Iniciá sesión para continuar.");
    }
}
