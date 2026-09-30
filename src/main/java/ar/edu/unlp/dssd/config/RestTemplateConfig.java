package ar.edu.unlp.dssd.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

    // Lo exponemos como bean para poder inyectarlo (y reemplazarlo por un mock en los tests)
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
