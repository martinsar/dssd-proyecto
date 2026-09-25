package ar.edu.unlp.dssd.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class BonitaService {

    @Value("${bonita.api.url}")
    private String bonitaUrl;

    @Value("${bonita.api.username}")
    private String username;

    @Value("${bonita.api.password}")
    private String password;

    @Value("${bonita.process.name}")
    private String processName;

    private final RestTemplate restTemplate = new RestTemplate();

    public void iniciarInstanciaEmergencia(Long idEmergencia, String tipoDesastre) {
        // 1. Autenticación
        ResponseEntity<String> loginResponse = login();
        String jSessionId = extractCookie(loginResponse, "JSESSIONID");
        String bonitaApiToken = extractCookie(loginResponse, "X-Bonita-API-Token");

        HttpHeaders headers = new HttpHeaders();
        headers.add("Cookie", "JSESSIONID=" + jSessionId + "; X-Bonita-API-Token=" + bonitaApiToken);
        headers.add("X-Bonita-API-Token", bonitaApiToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        // 2. Obtener el ID del proceso
        String processId = getProcessId(headers);

        // 3. Instanciar el proceso enviando las variables
        instanciarProceso(processId, headers, idEmergencia, tipoDesastre);
    }

    private ResponseEntity<String> login() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("username", username);
        map.add("password", password);
        map.add("redirect", "false");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
        return restTemplate.postForEntity(bonitaUrl + "/loginservice", request, String.class);
    }

    private String getProcessId(HttpHeaders headers) {
        HttpEntity<String> request = new HttpEntity<>(headers);
        ResponseEntity<List> response = restTemplate.exchange(
                bonitaUrl + "/API/bpm/process?s=" + processName,
                HttpMethod.GET,
                request,
                List.class
        );
        
        List<Map<String, Object>> body = response.getBody();
        if (body != null && !body.isEmpty()) {
            return body.get(0).get("id").toString();
        }
        throw new RuntimeException("Proceso no encontrado en Bonita: " + processName);
    }

    private void instanciarProceso(String processId, HttpHeaders headers, Long idEmergencia, String tipoDesastre) {
        // Mapeo de variables que recibirá Bonita en su contrato de inicio
        Map<String, Object> variables = new HashMap<>();
        variables.put("idEmergenciaInput", idEmergencia);
        variables.put("tipoDesastreInput", tipoDesastre);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(variables, headers);
        restTemplate.postForEntity(
                bonitaUrl + "/API/bpm/process/" + processId + "/instantiation",
                request,
                String.class
        );
    }

    private String extractCookie(ResponseEntity<String> response, String cookieName) {
        List<String> cookies = response.getHeaders().get(HttpHeaders.SET_COOKIE);
        if (cookies != null) {
            for (String cookie : cookies) {
                if (cookie.startsWith(cookieName)) {
                    Pattern pattern = Pattern.compile(cookieName + "=([^;]+)");
                    Matcher matcher = pattern.matcher(cookie);
                    if (matcher.find()) {
                        return matcher.group(1);
                    }
                }
            }
        }
        return "";
    }
}