package ar.edu.unlp.dssd.config;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class CorsConfig {

    // [Entrega final] Se leen los orígenes permitidos (separados por comas)
    @Value("${rescuesync.cors.origenes:http://localhost:4200,http://127.0.0.1:4200}")
    private String origenes;

    // [Entrega final] Se usa un filtro para que también las respuestas 401/403 lleven los headers CORS
    @Bean
    public CorsFilter corsFilter() {
        return new CorsFilter(fuenteCors(origenes));
    }

    public static UrlBasedCorsConfigurationSource fuenteCors(String origenes) {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(Arrays.stream(origenes.split(","))
                .map(String::trim)
                .filter(origen -> !origen.isEmpty())
                .toList());
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource fuente = new UrlBasedCorsConfigurationSource();
        fuente.registerCorsConfiguration("/**", config);
        return fuente;
    }
}
