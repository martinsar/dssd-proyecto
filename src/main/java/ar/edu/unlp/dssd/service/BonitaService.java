package ar.edu.unlp.dssd.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import ar.edu.unlp.dssd.exception.BonitaException;

/**
 * Unico punto de integracion con la API REST de Bonita.
 * Se usan Map/List de Java para el JSON (no hace falta mapear a clases).
 */
@Service
public class BonitaService {

    // Nombres de tareas, nodos y mensajes del proceso "GestionEmergencias".
    // Tienen que coincidir EXACTAMENTE con los del diagrama de Bonita Studio.
    private static final String TAREA_REGISTRAR_EMERGENCIA = "Registrar Emergencia.";
    private static final String NODO_RECIBIR_ALERTA = "Recibir Alerta";
    private static final String MENSAJE_ALERTA = "Se recibe un alerta de emergencia";
    private static final String TAREA_DESGLOSAR_LOTES = "Desglosar Lotes";
    private static final String TAREA_GESTIONAR_CONVOCATORIA = "Gestionar Convocatoria";
    private static final String NODO_RECIBIR_NOTIFICACION = "Recibir Notificación";
    private static final String MENSAJE_NOTIFICACION = "Recibe una notificación";

    // Bonita crea la tarea siguiente de forma asincronica (milisegundos), por eso reintentamos.
    private static final int MAX_INTENTOS_TAREA = 20;
    private static final long ESPERA_ENTRE_INTENTOS_MS = 250;

    // Formato que espera Bonita para un LOCALDATETIME en el contrato (sin fraccion de segundo)
    private static final DateTimeFormatter FORMATO_FECHA_HORA =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    private final RestTemplate restTemplate;
    private final String bonitaUrl;
    private final String processName;
    private final Credenciales credencialesMunicipio;
    private final Credenciales credencialesCoordinador;

    /** Usuario de Bonita con el que se opera; propiedad es el nombre de la propiedad de la contraseña. */
    private record Credenciales(String username, String password, String propiedadPassword) {
    }

    // El RestTemplate se inyecta (no "new") para poder reemplazarlo en tests con MockRestServiceServer
    public BonitaService(RestTemplate restTemplate,
                         @Value("${bonita.api.url}") String bonitaUrl,
                         @Value("${bonita.process.name}") String processName,
                         @Value("${bonita.usuarios.municipio.username}") String municipioUsername,
                         @Value("${bonita.usuarios.municipio.password}") String municipioPassword,
                         @Value("${bonita.usuarios.coordinador.username}") String coordinadorUsername,
                         @Value("${bonita.usuarios.coordinador.password}") String coordinadorPassword) {
        this.restTemplate = restTemplate;
        this.bonitaUrl = bonitaUrl;
        this.processName = processName;
        this.credencialesMunicipio = new Credenciales(municipioUsername, municipioPassword,
                "bonita.usuarios.municipio.password");
        this.credencialesCoordinador = new Credenciales(coordinadorUsername, coordinadorPassword,
                "bonita.usuarios.coordinador.password");
    }

    // ------------------------------------------------------------------
    // Operaciones de alto nivel (las que usan los otros servicios)
    // ------------------------------------------------------------------

    // Cada operacion usa el usuario del rol (actor) que corresponde a la tarea: en Bonita solo los
    // candidatos del actor pueden tomar/ejecutar la tarea, y asi queda registrado quien hizo que.
    // Municipio: instancia el proceso y ejecuta "Registrar Emergencia.".
    // Centro Coordinador: ejecuta "Desglosar Lotes" y "Gestionar Convocatoria".

    /** Crea un caso del proceso para la emergencia (como Municipio) y devuelve el caseId. */
    public Long iniciarInstanciaEmergencia(Long idEmergencia, String tipoDesastre) {
        HttpHeaders headers = login(credencialesMunicipio);
        String processId = obtenerIdProceso(headers);

        // Variables del contrato de inicio del proceso
        Map<String, Object> contrato = new HashMap<>();
        contrato.put("idEmergenciaInput", idEmergencia);
        contrato.put("tipoDesastreInput", tipoDesastre);

        Map<String, Object> respuesta = llamar("instanciar el proceso", () -> restTemplate.exchange(
                bonitaUrl + "/API/bpm/process/{id}/instantiation",
                HttpMethod.POST,
                new HttpEntity<>(contrato, headers),
                new ParameterizedTypeReference<Map<String, Object>>() {},
                processId).getBody());

        if (respuesta == null || respuesta.get("caseId") == null) {
            throw new BonitaException("Bonita no devolvió el caseId al instanciar el proceso");
        }
        // Bonita puede devolver el caseId como numero o como string
        return Long.valueOf(respuesta.get("caseId").toString());
    }

    /**
     * Resuelve automaticamente "Registrar Emergencia." (el registro ya lo hizo el coordinador
     * en nuestra app) y avisa "Recibir Alerta". Despues de esto el caso queda en "Desglosar Lotes".
     */
    public void completarPasosIniciales(Long caseId, Long idEmergencia) {
        HttpHeaders headers = login(credencialesMunicipio);
        String userId = obtenerIdUsuarioSesion(headers);

        String taskId = esperarTarea(headers, caseId, TAREA_REGISTRAR_EMERGENCIA);
        asignarTarea(headers, taskId, userId);
        ejecutarTarea(headers, taskId, new HashMap<>());

        enviarMensaje(headers, MENSAJE_ALERTA, NODO_RECIBIR_ALERTA, idEmergencia);
    }

    /**
     * Relevamiento: el coordinador ya cargo la convocatoria con sus lotes en nuestra app, asi que
     * completamos "Desglosar Lotes" (tarea sin contrato). Despues el caso queda en "Gestionar Convocatoria".
     * La tarea es obligatoria: si no aparece, se lanza BonitaException (y el servicio hace rollback).
     */
    public void completarRelevamiento(Long caseId) {
        HttpHeaders headers = login(credencialesCoordinador);
        String userId = obtenerIdUsuarioSesion(headers);

        String taskId = esperarTarea(headers, caseId, TAREA_DESGLOSAR_LOTES);
        asignarTarea(headers, taskId, userId);
        ejecutarTarea(headers, taskId, new HashMap<>());
    }

    /**
     * Publicacion formal: completa "Gestionar Convocatoria" con el contrato (si ya estaba completada,
     * la saltea) y envia el mensaje de notificacion a "Recibir Notificación".
     * "Desglosar Lotes" ya se completo en el relevamiento (completarRelevamiento).
     */
    public void publicarConvocatoria(Long caseId, Long idEmergencia, Long idConvocatoria,
                                     LocalDateTime fechaCierre) {
        HttpHeaders headers = login(credencialesCoordinador);
        String userId = obtenerIdUsuarioSesion(headers);

        Optional<String> gestionar = buscarTareaPendiente(headers, caseId, TAREA_GESTIONAR_CONVOCATORIA);
        if (gestionar.isPresent()) {
            String taskId = gestionar.get();
            asignarTarea(headers, taskId, userId);

            Map<String, Object> contrato = new HashMap<>();
            contrato.put("idConvocatoriaInput", idConvocatoria);
            contrato.put("fechaCierreInput", fechaCierre.truncatedTo(ChronoUnit.SECONDS).format(FORMATO_FECHA_HORA));
            ejecutarTarea(headers, taskId, contrato);
        } else if (!existeTareaArchivada(headers, caseId, TAREA_GESTIONAR_CONVOCATORIA)) {
            throw new BonitaException("La tarea '" + TAREA_GESTIONAR_CONVOCATORIA
                    + "' no apareció en el caso " + caseId + " de Bonita");
        }
        // Si la tarea ya esta archivada, un intento anterior la completo y fallo el envio del mensaje:
        // no se ejecuta de nuevo y se reintenta solo la notificacion.

        enviarMensaje(headers, MENSAJE_NOTIFICACION, NODO_RECIBIR_NOTIFICACION, idEmergencia);
    }

    // ------------------------------------------------------------------
    // Bloques reutilizables (reciben los headers para no loguearse de mas)
    // ------------------------------------------------------------------

    /** Hace login y devuelve los headers listos para las siguientes llamadas (cookies + token + JSON). */
    private HttpHeaders login(Credenciales credenciales) {
        if (credenciales.password() == null || credenciales.password().isBlank()) {
            throw new BonitaException("Falta configurar la contraseña del usuario de Bonita '"
                    + credenciales.username() + "': definir la propiedad " + credenciales.propiedadPassword());
        }

        HttpHeaders loginHeaders = new HttpHeaders();
        loginHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("username", credenciales.username());
        form.add("password", credenciales.password());
        form.add("redirect", "false");

        ResponseEntity<String> respuesta = llamar("autenticarse", () -> restTemplate.postForEntity(
                bonitaUrl + "/loginservice", new HttpEntity<>(form, loginHeaders), String.class));

        String jSessionId = extraerCookie(respuesta, "JSESSIONID");
        String apiToken = extraerCookie(respuesta, "X-Bonita-API-Token");
        if (jSessionId.isEmpty() || apiToken.isEmpty()) {
            throw new BonitaException("Bonita no devolvió la sesión (JSESSIONID / X-Bonita-API-Token) al autenticarse");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.COOKIE, "JSESSIONID=" + jSessionId + "; X-Bonita-API-Token=" + apiToken);
        headers.add("X-Bonita-API-Token", apiToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    private String obtenerIdProceso(HttpHeaders headers) {
        // Filtro exacto por nombre y solo procesos habilitados (la busqueda ?s= es "contiene")
        List<Map<String, Object>> procesos = obtenerLista(headers, "buscar el proceso",
                bonitaUrl + "/API/bpm/process?p=0&c=1&f=name={nombre}&f=activationState=ENABLED",
                processName);
        if (procesos.isEmpty() || procesos.get(0).get("id") == null) {
            throw new BonitaException("Proceso no encontrado o no habilitado: " + processName);
        }
        return procesos.get(0).get("id").toString();
    }

    /** Id del usuario logueado; hay que asignarle las tareas para poder ejecutarlas. */
    public String obtenerIdUsuarioSesion(HttpHeaders headers) {
        Map<String, Object> sesion = llamar("obtener la sesión", () -> restTemplate.exchange(
                bonitaUrl + "/API/system/session/unusedid",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                new ParameterizedTypeReference<Map<String, Object>>() {}).getBody());
        if (sesion == null || sesion.get("user_id") == null) {
            throw new BonitaException("Bonita no devolvió el user_id de la sesión");
        }
        return sesion.get("user_id").toString();
    }

    /**
     * Busca una tarea humana pendiente del caso, con reintentos (hasta 20 x 250ms).
     * Devuelve Optional vacio si no aparecio (asi el que llama decide si es un error o no).
     */
    public Optional<String> buscarTareaPendiente(HttpHeaders headers, Long caseId, String nombreTarea) {
        return buscarTareaPendiente(headers, caseId, nombreTarea, MAX_INTENTOS_TAREA);
    }

    private Optional<String> buscarTareaPendiente(HttpHeaders headers, Long caseId, String nombreTarea,
                                                  int maxIntentos) {
        // Con variables de URI, RestTemplate codifica bien nombres con espacios, puntos y acentos
        String url = bonitaUrl + "/API/bpm/humanTask?p=0&c=10&f=caseId={caseId}&f=name={nombre}";
        for (int intento = 1; intento <= maxIntentos; intento++) {
            List<Map<String, Object>> tareas = obtenerLista(headers, "buscar la tarea '" + nombreTarea + "'",
                    url, caseId, nombreTarea);
            if (!tareas.isEmpty() && tareas.get(0).get("id") != null) {
                return Optional.of(tareas.get(0).get("id").toString());
            }
            if (intento < maxIntentos) {
                esperar();
            }
        }
        return Optional.empty();
    }

    /** Indica si la tarea del caso ya fue completada (Bonita la mueve a las tareas archivadas). */
    private boolean existeTareaArchivada(HttpHeaders headers, Long caseId, String nombreTarea) {
        List<Map<String, Object>> archivadas = obtenerLista(headers,
                "buscar la tarea archivada '" + nombreTarea + "'",
                bonitaUrl + "/API/bpm/archivedHumanTask?p=0&c=1&f=caseId={caseId}&f=name={nombre}",
                caseId, nombreTarea);
        return !archivadas.isEmpty();
    }

    /** Igual que buscarTareaPendiente pero si la tarea nunca aparece lanza BonitaException. */
    private String esperarTarea(HttpHeaders headers, Long caseId, String nombreTarea) {
        return buscarTareaPendiente(headers, caseId, nombreTarea)
                .orElseThrow(() -> new BonitaException("La tarea '" + nombreTarea
                        + "' no apareció en el caso " + caseId + " de Bonita"));
    }

    public void asignarTarea(HttpHeaders headers, String taskId, String userId) {
        Map<String, Object> cuerpo = new HashMap<>();
        cuerpo.put("assigned_id", userId);
        llamar("asignar la tarea " + taskId, () -> {
            restTemplate.exchange(bonitaUrl + "/API/bpm/userTask/{id}", HttpMethod.PUT,
                    new HttpEntity<>(cuerpo, headers), Void.class, taskId);
            return null;
        });
    }

    /** Ejecuta la tarea con su contrato (Map vacio si la tarea no tiene contrato). Bonita responde 204. */
    public void ejecutarTarea(HttpHeaders headers, String taskId, Map<String, Object> contrato) {
        llamar("ejecutar la tarea " + taskId, () -> {
            restTemplate.exchange(bonitaUrl + "/API/bpm/userTask/{id}/execution", HttpMethod.POST,
                    new HttpEntity<>(contrato, headers), Void.class, taskId);
            return null;
        });
    }

    /**
     * Envia un mensaje a una Receive Task del proceso. La correlacion por idEmergencia hace que
     * llegue solo al caso de esa emergencia (hay que configurarla tambien en Bonita Studio).
     */
    public void enviarMensaje(HttpHeaders headers, String nombreMensaje, String nodoDestino, Long idEmergencia) {
        Map<String, Object> idCorrelacion = new HashMap<>();
        idCorrelacion.put("value", idEmergencia);
        idCorrelacion.put("type", "java.lang.Long");

        Map<String, Object> cuerpo = new HashMap<>();
        cuerpo.put("messageName", nombreMensaje);
        cuerpo.put("targetProcess", processName);
        cuerpo.put("targetFlowNode", nodoDestino);
        cuerpo.put("messageContent", new HashMap<String, Object>());
        cuerpo.put("correlations", Map.of("idEmergencia", idCorrelacion));

        llamar("enviar el mensaje '" + nombreMensaje + "'", () -> {
            restTemplate.exchange(bonitaUrl + "/API/bpm/message", HttpMethod.POST,
                    new HttpEntity<>(cuerpo, headers), Void.class);
            return null;
        });
    }

    // ------------------------------------------------------------------
    // Utilidades internas
    // ------------------------------------------------------------------

    private List<Map<String, Object>> obtenerLista(HttpHeaders headers, String accion, String url,
                                                   Object... variablesUri) {
        List<Map<String, Object>> lista = llamar(accion, () -> restTemplate.exchange(
                url,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                new ParameterizedTypeReference<List<Map<String, Object>>>() {},
                variablesUri).getBody());
        return lista == null ? List.of() : lista;
    }

    /** Ejecuta una llamada REST y convierte cualquier falla de RestTemplate en BonitaException. */
    private <T> T llamar(String accion, Supplier<T> llamada) {
        try {
            return llamada.get();
        } catch (RestClientException e) {
            throw new BonitaException("Error de comunicación con Bonita al " + accion + ": " + e.getMessage(), e);
        }
    }

    private void esperar() {
        try {
            Thread.sleep(ESPERA_ENTRE_INTENTOS_MS);
        } catch (InterruptedException e) {
            // Restauramos el flag de interrupcion para no "tragarnos" la señal
            Thread.currentThread().interrupt();
            throw new BonitaException("Se interrumpió la espera de una tarea de Bonita", e);
        }
    }

    private String extraerCookie(ResponseEntity<String> respuesta, String nombreCookie) {
        Pattern patron = Pattern.compile("^" + Pattern.quote(nombreCookie) + "=([^;]+)");
        for (String cookie : respuesta.getHeaders().getOrEmpty(HttpHeaders.SET_COOKIE)) {
            Matcher matcher = patron.matcher(cookie);
            if (matcher.find()) {
                return matcher.group(1);
            }
        }
        return "";
    }
}
