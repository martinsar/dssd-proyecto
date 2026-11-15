package ar.edu.unlp.dssd.service;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

/** [Entrega final] Sesión de un usuario en Bonita (cookie y token) */
public record SesionBonita(String userId, String username, String jSessionId, String apiToken) {

    /** Se arman headers nuevos en cada llamada (cookie, token y JSON) */
    public HttpHeaders headers() {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.COOKIE, "JSESSIONID=" + jSessionId + "; X-Bonita-API-Token=" + apiToken);
        headers.add("X-Bonita-API-Token", apiToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }
}
