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

@Service
public class BonitaIntegrationService {

    // Asegurate de agregar estas variables en tu application.properties
    @Value("${bonita.api.base-url:http://localhost:52365/bonita}")
    private String baseUrl;

    @Value("${bonita.api.username:walter.bates}")
    private String username;

    @Value("${bonita.api.password:bpm}")
    private String password;

    @Value("${bonita.api.process-name:GestionEmergencias}")
    private String processName;

    private final RestTemplate restTemplate = new RestTemplate();

    public void iniciarProcesoEmergencia(Long idEmergencia, String tipoDesastre) {
        // 1. Login
        HttpHeaders authHeaders = new HttpHeaders();
        authHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> loginBody = new LinkedMultiValueMap<>();
        loginBody.add("username", username);
        loginBody.add("password", password);
        loginBody.add("redirect", "false");

        ResponseEntity<String> loginResponse = restTemplate.postForEntity(
                baseUrl + "/loginservice", new HttpEntity<>(loginBody, authHeaders), String.class);
        
        List<String> cookies = loginResponse.getHeaders().get(HttpHeaders.SET_COOKIE);
        String apiToken = extraerApiToken(cookies);

        // 2. Obtener Process ID
        HttpHeaders apiHeaders = new HttpHeaders();
        apiHeaders.setContentType(MediaType.APPLICATION_JSON);
        if (cookies != null) apiHeaders.put(HttpHeaders.COOKIE, cookies);
        apiHeaders.set("X-Bonita-API-Token", apiToken);

        ResponseEntity<List> processResponse = restTemplate.exchange(
                baseUrl + "/API/bpm/process?s=" + processName,
                HttpMethod.GET, new HttpEntity<>(apiHeaders), List.class);

        if (processResponse.getBody() == null || processResponse.getBody().isEmpty()) {
            throw new RuntimeException("No se encontró el proceso: " + processName);
        }

        Map<String, Object> processInfo = (Map<String, Object>) processResponse.getBody().get(0);
        String processId = processInfo.get("id").toString();

        // 3. Instanciar enviando el contrato
        Map<String, Object> contrato = new HashMap<>();
        contrato.put("idEmergenciaInput", idEmergencia);
        contrato.put("tipoDesastreInput", tipoDesastre);

        ResponseEntity<String> instanciationResponse = restTemplate.postForEntity(
                baseUrl + "/API/bpm/process/" + processId + "/instantiation",
                new HttpEntity<>(contrato, apiHeaders), String.class);

        System.out.println("Proceso instanciado en Bonita. Case ID: " + instanciationResponse.getBody());
    }

    private String extraerApiToken(List<String> cookies) {
        if (cookies == null) return "";
        for (String cookie : cookies) {
            if (cookie.startsWith("X-Bonita-API-Token=")) {
                return cookie.split(";")[0].split("=")[1];
            }
        }
        return "";
    }
}