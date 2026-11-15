package ar.edu.unlp.dssd.seguridad;

import java.util.List;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/** [Entrega final] Se registran el interceptor de autenticación y el resolver de la sesión */
@Configuration
public class SeguridadWebConfig implements WebMvcConfigurer {

    private final AutenticacionInterceptor autenticacionInterceptor;

    public SeguridadWebConfig(AutenticacionInterceptor autenticacionInterceptor) {
        this.autenticacionInterceptor = autenticacionInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(autenticacionInterceptor).addPathPatterns("/api/**");
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new SesionUsuarioResolver());
    }
}
