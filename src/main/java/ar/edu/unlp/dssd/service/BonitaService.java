package ar.edu.unlp.dssd.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import ar.edu.unlp.dssd.exception.BonitaException;
import ar.edu.unlp.dssd.exception.NoAutenticadoException;

/**
 * Unico punto de integracion con la API REST de Bonita.
 * Se usan Map/List de Java para el JSON (no hace falta mapear a clases).
 */
@Service
public class BonitaService {

    private static final Logger log = LoggerFactory.getLogger(BonitaService.class);

    // Nombres de tareas, nodos y mensajes del proceso "GestionEmergencias".
    // Tienen que coincidir EXACTAMENTE con los del diagrama de Bonita Studio.
    static final String TAREA_REGISTRAR_EMERGENCIA = "Registrar Emergencia.";
    private static final String NODO_RECIBIR_ALERTA = "Recibir Alerta";
    private static final String MENSAJE_ALERTA = "Se recibe un alerta de emergencia";
    static final String TAREA_DESGLOSAR_LOTES = "Desglosar Lotes";
    static final String TAREA_GESTIONAR_CONVOCATORIA = "Gestionar Convocatoria";
    private static final String NODO_RECIBIR_NOTIFICACION = "Recibir Notificación";
    private static final String MENSAJE_NOTIFICACION = "Recibe una notificación";
    // [Entrega final] Tarea humana de la decisión del coordinador cuando la cobertura no alcanza
    static final String TAREA_DECIDIR_CONTINUACION = "Decidir cómo continuar";
    // [Entrega final] Tareas finales del proceso
    static final String TAREA_SELECCIONAR_OFERTAS = "Seleccionar ofertas";
    static final String TAREA_FINALIZAR_ACTIVIDADES = "Finalizar actividades";

    // Bonita crea la tarea siguiente de forma asincronica (milisegundos), por eso reintentamos.
    private static final int MAX_INTENTOS_TAREA = 20;
    // [Entrega final] Las tareas finales pueden no existir en el diagrama: se reintenta menos
    private static final int MAX_INTENTOS_TAREA_FINAL = 4;
    private static final long ESPERA_ENTRE_INTENTOS_MS = 250;

    // Paginación para consultas históricas de métricas en Bonita
    private static final int TAMANIO_PAGINA_METRICAS = 100;
    private static final int MAX_PAGINAS_METRICAS = 50;

    // Formato que espera Bonita para un LOCALDATETIME en el contrato (sin fraccion de segundo)
    private static final DateTimeFormatter FORMATO_FECHA_HORA =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    private final RestTemplate restTemplate;
    private final String bonitaUrl;
    private final String processName;
    private final Credenciales credencialesMunicipio;
    private final Credenciales credencialesCoordinador;
    private final Credenciales credencialesOng;
    private final Credenciales credencialesAuditor;

    /** Usuario de Bonita con el que se opera; propiedad es el nombre de la propiedad de la contraseña. */
    private record Credenciales(String username, String password, String propiedadPassword) {
        boolean completas() {
            return username != null && !username.isBlank() && password != null && !password.isBlank();
        }
    }

    /** Datos crudos devueltos por Bonita para calcular métricas, salud y cuellos de botella de todo el proceso. */
    public record DatosMetricasBonita(List<Map<String, Object>> procesos,
                                      List<Map<String, Object>> flujosArchivados,
                                      List<Map<String, Object>> casosArchivados,
                                      List<Map<String, Object>> casosActivos,
                                      List<Map<String, Object>> tareasPendientes,
                                      List<Map<String, Object>> tareasFallidas,
                                      Map<String, String> usuariosPorId) {
    }

    /** Datos crudos devueltos por Bonita para el detalle de tiempos de un caso puntual. */
    public record DatosCasoBonita(List<Map<String, Object>> flujosArchivados,
                                  List<Map<String, Object>> tareasPendientes,
                                  Optional<Map<String, Object>> casoArchivado) {
    }

    // El RestTemplate se inyecta (no "new") para poder reemplazarlo en tests con MockRestServiceServer
    @Autowired
    public BonitaService(RestTemplate restTemplate,
                         @Value("${bonita.api.url}") String bonitaUrl,
                         @Value("${bonita.process.name}") String processName,
                         @Value("${bonita.usuarios.municipio.username}") String municipioUsername,
                         @Value("${bonita.usuarios.municipio.password}") String municipioPassword,
                         @Value("${bonita.usuarios.coordinador.username}") String coordinadorUsername,
                         @Value("${bonita.usuarios.coordinador.password}") String coordinadorPassword,
                         @Value("${bonita.usuarios.ong.username}") String ongUsername,
                         @Value("${bonita.usuarios.ong.password}") String ongPassword,
                         @Value("${bonita.usuarios.auditor.username:install}") String auditorUsername,
                         @Value("${bonita.usuarios.auditor.password:install}") String auditorPassword) {
        this.restTemplate = restTemplate;
        this.bonitaUrl = bonitaUrl;
        this.processName = processName;
        this.credencialesMunicipio = new Credenciales(municipioUsername, municipioPassword,
                "bonita.usuarios.municipio.password");
        this.credencialesCoordinador = new Credenciales(coordinadorUsername, coordinadorPassword,
                "bonita.usuarios.coordinador.password");
        this.credencialesOng = new Credenciales(ongUsername, ongPassword, "bonita.usuarios.ong.password");
        this.credencialesAuditor = new Credenciales(auditorUsername, auditorPassword,
                "bonita.usuarios.auditor.password");
    }

    /** Constructor de conveniencia para tests que no configuran usuario auditor. */
    public BonitaService(RestTemplate restTemplate, String bonitaUrl, String processName,
                         String municipioUsername, String municipioPassword,
                         String coordinadorUsername, String coordinadorPassword,
                         String ongUsername, String ongPassword) {
        this(restTemplate, bonitaUrl, processName, municipioUsername, municipioPassword,
                coordinadorUsername, coordinadorPassword, ongUsername, ongPassword, "install", "install");
    }

    // ------------------------------------------------------------------
    // [Entrega final] Sesión del usuario real (login de la app)
    // ------------------------------------------------------------------

    /** Se autentica al usuario en Bonita; credenciales inválidas lanzan NoAutenticadoException */
    public SesionBonita iniciarSesion(String username, String password) {
        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            throw new NoAutenticadoException("Usuario o contraseña incorrectos.");
        }
        SesionBonita sinUsuario;
        try {
            sinUsuario = autenticar(username.trim(), password);
        } catch (BonitaException e) {
            if (e.getCause() instanceof HttpClientErrorException.Unauthorized) {
                throw new NoAutenticadoException("Usuario o contraseña incorrectos.");
            }
            throw e;
        }

        Map<String, Object> datos = datosSesion(sinUsuario.headers());
        String userName = datos.get("user_name") != null ? datos.get("user_name").toString() : sinUsuario.username();
        return new SesionBonita(datos.get("user_id").toString(), userName,
                sinUsuario.jSessionId(), sinUsuario.apiToken());
    }

    /** Se obtienen los nombres de rol de las membresías del usuario; si Bonita responde 403 se devuelve vacío */
    public List<String> obtenerRolesUsuario(SesionBonita sesion) {
        List<Map<String, Object>> membresias;
        try {
            membresias = obtenerLista(sesion.headers(), "consultar los roles del usuario",
                    bonitaUrl + "/API/identity/membership?p=0&c=50&f=user_id={id}&d=role_id",
                    sesion.userId());
        } catch (BonitaException e) {
            if (e.getCause() instanceof HttpClientErrorException.Forbidden) {
                log.warn("Bonita no permite consultar las membresías del usuario {}; se usa el rol local",
                        sesion.username());
                return List.of();
            }
            throw e;
        }

        List<String> roles = new ArrayList<>();
        for (Map<String, Object> membresia : membresias) {
            if (membresia.get("role_id") instanceof Map<?, ?> rol && rol.get("name") != null) {
                roles.add(rol.get("name").toString());
            }
        }
        return roles;
    }

    /** Se cierra la sesión en Bonita; los errores se ignoran */
    public void cerrarSesion(SesionBonita sesion) {
        if (sesion == null) {
            return;
        }
        try {
            restTemplate.exchange(bonitaUrl + "/logoutservice?redirect=false", HttpMethod.GET,
                    new HttpEntity<>(sesion.headers()), Void.class);
        } catch (RestClientException e) {
            log.debug("No se pudo cerrar la sesión de Bonita: {}", e.getMessage());
        }
    }

    // ------------------------------------------------------------------
    // [Entrega final] Consultas de métricas y auditoría vía API REST de Bonita
    // ------------------------------------------------------------------

    /**
     * Obtiene tareas de usuario archivadas y casos archivados de todas las versiones del proceso
     * GestionEmergencias. Si la sesión del usuario no tiene permisos en Bonita (401/403), usa las
     * credenciales configuradas en bonita.usuarios.auditor.
     */
    public DatosMetricasBonita obtenerDatosMetricasProceso(SesionBonita sesionUsuario) {
        return ejecutarConsultaAuditoria(sesionUsuario, headers -> {
            List<Map<String, Object>> procesos = obtenerProcesos(headers);
            if (procesos.isEmpty()) {
                return new DatosMetricasBonita(List.of(), List.of(), List.of(), List.of(), List.of(), List.of(),
                        Map.of());
            }
            List<Map<String, Object>> flujos = new ArrayList<>();
            List<Map<String, Object>> casos = new ArrayList<>();
            List<Map<String, Object>> casosActivos = new ArrayList<>();
            List<Map<String, Object>> tareasPendientes = new ArrayList<>();
            List<Map<String, Object>> tareasFallidas = new ArrayList<>();
            for (Map<String, Object> proceso : procesos) {
                Object id = proceso.get("id");
                if (id == null) {
                    continue;
                }
                String processId = id.toString();
                flujos.addAll(obtenerListaPaginada(headers,
                        "consultar tareas archivadas del proceso " + processId,
                        bonitaUrl + "/API/bpm/archivedFlowNode?p={p}&c={c}&f=processId={processId}",
                        processId));
                casos.addAll(obtenerListaPaginada(headers,
                        "consultar casos archivados del proceso " + processId,
                        bonitaUrl + "/API/bpm/archivedCase?p={p}&c={c}&f=processDefinitionId={processId}",
                        processId));
                casosActivos.addAll(obtenerListaPaginada(headers,
                        "consultar casos activos del proceso " + processId,
                        bonitaUrl + "/API/bpm/case?p={p}&c={c}&f=processDefinitionId={processId}",
                        processId));
                tareasPendientes.addAll(obtenerListaPaginada(headers,
                        "consultar tareas pendientes del proceso " + processId,
                        bonitaUrl + "/API/bpm/humanTask?p={p}&c={c}&f=processId={processId}",
                        processId));
                tareasFallidas.addAll(obtenerTareasFallidas(headers, processId));
            }
            Map<String, String> usuariosPorId = obtenerMapaUsuarios(headers);
            return new DatosMetricasBonita(procesos, flujos, casos, casosActivos, tareasPendientes,
                    tareasFallidas, usuariosPorId);
        });
    }

    /** Nodos fallidos del proceso; si Bonita restringe /API/bpm/flowNode se devuelve lista vacía. */
    private List<Map<String, Object>> obtenerTareasFallidas(HttpHeaders headers, String processId) {
        try {
            return obtenerListaPaginada(headers,
                    "consultar tareas fallidas del proceso " + processId,
                    bonitaUrl + "/API/bpm/flowNode?p={p}&c={c}&f=processId={processId}&f=state=failed",
                    processId);
        } catch (BonitaException e) {
            log.debug("No se pudieron consultar tareas fallidas del proceso {}: {}", processId, e.getMessage());
            return List.of();
        }
    }

    /** Mapa id -> userName de Bonita; si Bonita niega el acceso a /API/identity/user, devuelve mapa vacío. */
    private Map<String, String> obtenerMapaUsuarios(HttpHeaders headers) {
        try {
            List<Map<String, Object>> usuarios = obtenerListaPaginada(headers,
                    "consultar usuarios de Bonita",
                    bonitaUrl + "/API/identity/user?p={p}&c={c}");
            Map<String, String> mapa = new HashMap<>();
            for (Map<String, Object> usuario : usuarios) {
                Object id = usuario.get("id");
                Object userName = usuario.get("userName");
                if (id != null && userName != null && !userName.toString().isBlank()) {
                    mapa.put(id.toString(), userName.toString().trim());
                }
            }
            return mapa;
        } catch (BonitaException e) {
            log.debug("No se pudo resolver el catálogo de usuarios de Bonita: {}", e.getMessage());
            return Map.of();
        }
    }

    /** Obtiene tareas archivadas, tareas pendientes y estado del caso archivado para una emergencia puntual. */
    public DatosCasoBonita obtenerDatosMetricasCaso(Long caseId, SesionBonita sesionUsuario) {
        return ejecutarConsultaAuditoria(sesionUsuario, headers -> {
            List<Map<String, Object>> flujos = obtenerListaPaginada(headers,
                    "consultar tareas archivadas del caso " + caseId,
                    bonitaUrl + "/API/bpm/archivedFlowNode?p={p}&c={c}&f=caseId={caseId}",
                    caseId);
            List<Map<String, Object>> pendientes = obtenerLista(headers,
                    "consultar tareas pendientes del caso " + caseId,
                    bonitaUrl + "/API/bpm/humanTask?p=0&c=100&f=caseId={caseId}",
                    caseId);
            List<Map<String, Object>> casos = obtenerLista(headers,
                    "consultar caso archivado " + caseId,
                    bonitaUrl + "/API/bpm/archivedCase?p=0&c=1&f=sourceObjectId={caseId}",
                    caseId);
            Optional<Map<String, Object>> casoArchivado = casos.isEmpty() ? Optional.empty() : Optional.of(casos.get(0));
            return new DatosCasoBonita(flujos, pendientes, casoArchivado);
        });
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
            completarGestionarConvocatoria(headers, userId, gestionar.get(), idConvocatoria, fechaCierre);
        } else if (!existeTareaArchivada(headers, caseId, TAREA_GESTIONAR_CONVOCATORIA)) {
            throw new BonitaException("La tarea '" + TAREA_GESTIONAR_CONVOCATORIA
                    + "' no apareció en el caso " + caseId + " de Bonita");
        }
        // Si la tarea ya esta archivada, un intento anterior la completo y fallo el envio del mensaje:
        // no se ejecuta de nuevo y se reintenta solo la notificacion.

        enviarMensaje(headers, MENSAJE_NOTIFICACION, NODO_RECIBIR_NOTIFICACION, idEmergencia);
    }

    /** [Entrega final] Indica si "Decidir cómo continuar" está pendiente en el caso (pocos reintentos) */
    public boolean decisionPendiente(Long caseId) {
        HttpHeaders headers = login(credencialesCoordinador);
        return buscarTareaPendiente(headers, caseId, TAREA_DECIDIR_CONTINUACION, MAX_INTENTOS_TAREA_FINAL).isPresent();
    }

    /**
     * [Entrega final] Se ejecuta "Decidir cómo continuar" como Coordinador con la decisión
     * (CONTINUAR_PARCIAL, REABRIR o REFORMULAR). Si la tarea no está pendiente se lanza BonitaException.
     */
    public void decidirContinuacion(Long caseId, String decision) {
        HttpHeaders headers = login(credencialesCoordinador);
        String userId = obtenerIdUsuarioSesion(headers);

        String taskId = buscarTareaPendiente(headers, caseId, TAREA_DECIDIR_CONTINUACION, MAX_INTENTOS_TAREA_FINAL)
                .orElseThrow(() -> new BonitaException("La tarea '" + TAREA_DECIDIR_CONTINUACION
                        + "' no está pendiente en el caso " + caseId + " de Bonita"));
        asignarTarea(headers, taskId, userId);
        Map<String, Object> contrato = new HashMap<>();
        contrato.put("decisionInput", decision);
        ejecutarTarea(headers, taskId, contrato);
    }

    /**
     * [Entrega final] Reapertura: tras "Decidir cómo continuar" "Gestionar Convocatoria" aparece como una
     * instancia nueva. Se espera pendiente (sin atajo de archivada, que corresponde a la instancia vieja),
     * se completa y se envía la notificación.
     */
    public void reabrirConvocatoria(Long caseId, Long idEmergencia, Long idConvocatoria,
                                    LocalDateTime fechaCierre) {
        HttpHeaders headers = login(credencialesCoordinador);
        String userId = obtenerIdUsuarioSesion(headers);

        String taskId = esperarTarea(headers, caseId, TAREA_GESTIONAR_CONVOCATORIA);
        completarGestionarConvocatoria(headers, userId, taskId, idConvocatoria, fechaCierre);
        enviarMensaje(headers, MENSAJE_NOTIFICACION, NODO_RECIBIR_NOTIFICACION, idEmergencia);
    }

    private void completarGestionarConvocatoria(HttpHeaders headers, String userId, String taskId,
                                                Long idConvocatoria, LocalDateTime fechaCierre) {
        asignarTarea(headers, taskId, userId);
        Map<String, Object> contrato = new HashMap<>();
        contrato.put("idConvocatoriaInput", idConvocatoria);
        contrato.put("fechaCierreInput", fechaCierre.truncatedTo(ChronoUnit.SECONDS).format(FORMATO_FECHA_HORA));
        ejecutarTarea(headers, taskId, contrato);
    }

    /** [Entrega final] Se ejecuta "Seleccionar ofertas" como Municipio; false si la tarea no está pendiente */
    public boolean seleccionarOfertas(Long caseId) {
        return ejecutarTareaSinContrato(credencialesMunicipio, caseId, TAREA_SELECCIONAR_OFERTAS);
    }

    /** [Entrega final] Se ejecuta "Finalizar actividades" como ONG; false si la tarea no está pendiente */
    public boolean finalizarActividades(Long caseId) {
        return ejecutarTareaSinContrato(credencialesOng, caseId, TAREA_FINALIZAR_ACTIVIDADES);
    }

    /** Se busca la tarea pendiente, se asigna al usuario del rol y se ejecuta con contrato vacío */
    private boolean ejecutarTareaSinContrato(Credenciales credenciales, Long caseId, String nombreTarea) {
        HttpHeaders headers = login(credenciales);
        String userId = obtenerIdUsuarioSesion(headers);

        Optional<String> tarea = buscarTareaPendiente(headers, caseId, nombreTarea, MAX_INTENTOS_TAREA_FINAL);
        if (tarea.isEmpty()) {
            log.warn("La tarea '{}' no está pendiente en el caso {} de Bonita", nombreTarea, caseId);
            return false;
        }
        asignarTarea(headers, tarea.get(), userId);
        ejecutarTarea(headers, tarea.get(), new HashMap<>());
        return true;
    }

    // ------------------------------------------------------------------
    // Bloques reutilizables (reciben los headers para no loguearse de mas)
    // ------------------------------------------------------------------

    /** Ejecuta una consulta de lectura con la sesión del usuario o, si Bonita devuelve 401/403, con el usuario auditor/admin. */
    private <T> T ejecutarConsultaAuditoria(SesionBonita sesionUsuario, Function<HttpHeaders, T> consulta) {
        if (sesionUsuario != null) {
            try {
                return consulta.apply(sesionUsuario.headers());
            } catch (BonitaException e) {
                if (!esErrorAutorizacion(e) || !credencialesAuditor.completas()) {
                    throw e;
                }
                log.debug("La sesión de {} no tiene permisos de auditoría en Bonita; se reintenta con {}",
                        sesionUsuario.username(), credencialesAuditor.username());
            }
        }

        if (!credencialesAuditor.completas()) {
            throw new BonitaException("Falta configurar las credenciales de auditoría de Bonita: definir "
                    + "bonita.usuarios.auditor.username y " + credencialesAuditor.propiedadPassword());
        }
        SesionBonita sesionAdmin = autenticar(credencialesAuditor.username(), credencialesAuditor.password());
        try {
            return consulta.apply(sesionAdmin.headers());
        } finally {
            cerrarSesion(sesionAdmin);
        }
    }

    private static boolean esErrorAutorizacion(BonitaException e) {
        return e.getCause() instanceof HttpClientErrorException.Unauthorized
                || e.getCause() instanceof HttpClientErrorException.Forbidden;
    }

    /** Hace login y devuelve los headers listos para las siguientes llamadas (cookies + token + JSON). */
    private HttpHeaders login(Credenciales credenciales) {
        if (credenciales.password() == null || credenciales.password().isBlank()) {
            throw new BonitaException("Falta configurar la contraseña del usuario de Bonita '"
                    + credenciales.username() + "': definir la propiedad " + credenciales.propiedadPassword());
        }
        return autenticar(credenciales.username(), credenciales.password()).headers();
    }

    /** [Entrega final] Se hace el login en Bonita y se arma la sesión (todavía sin user_id) */
    private SesionBonita autenticar(String username, String password) {
        HttpHeaders loginHeaders = new HttpHeaders();
        loginHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("username", username);
        form.add("password", password);
        form.add("redirect", "false");

        ResponseEntity<String> respuesta = llamar("autenticarse", () -> restTemplate.postForEntity(
                bonitaUrl + "/loginservice", new HttpEntity<>(form, loginHeaders), String.class));

        String jSessionId = extraerCookie(respuesta, "JSESSIONID");
        String apiToken = extraerCookie(respuesta, "X-Bonita-API-Token");
        if (jSessionId.isEmpty() || apiToken.isEmpty()) {
            throw new BonitaException("Bonita no devolvió la sesión (JSESSIONID / X-Bonita-API-Token) al autenticarse");
        }

        return new SesionBonita(null, username, jSessionId, apiToken);
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

    /** Devuelve todas las versiones del proceso (habilitadas o archivadas) para consultar métricas. */
    private List<Map<String, Object>> obtenerProcesos(HttpHeaders headers) {
        return obtenerLista(headers, "buscar las versiones del proceso",
                bonitaUrl + "/API/bpm/process?p=0&c=50&f=name={nombre}",
                processName);
    }

    /** Id del usuario logueado; hay que asignarle las tareas para poder ejecutarlas. */
    public String obtenerIdUsuarioSesion(HttpHeaders headers) {
        return datosSesion(headers).get("user_id").toString();
    }

    /** Datos de la sesión actual de Bonita (user_id, user_name, ...) */
    private Map<String, Object> datosSesion(HttpHeaders headers) {
        Map<String, Object> sesion = llamar("obtener la sesión", () -> restTemplate.exchange(
                bonitaUrl + "/API/system/session/unusedid",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                new ParameterizedTypeReference<Map<String, Object>>() {}).getBody());
        if (sesion == null || sesion.get("user_id") == null) {
            throw new BonitaException("Bonita no devolvió el user_id de la sesión");
        }
        return sesion;
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

    /** Recorre páginas de 100 elementos hasta agotar resultados o alcanzar el tope de seguridad. */
    private List<Map<String, Object>> obtenerListaPaginada(HttpHeaders headers, String accion,
                                                           String urlConPaginaYCantidad, Object... variablesFijas) {
        List<Map<String, Object>> acumulado = new ArrayList<>();
        Object[] variables = new Object[2 + variablesFijas.length];
        variables[1] = TAMANIO_PAGINA_METRICAS;
        System.arraycopy(variablesFijas, 0, variables, 2, variablesFijas.length);

        for (int pagina = 0; pagina < MAX_PAGINAS_METRICAS; pagina++) {
            variables[0] = pagina;
            List<Map<String, Object>> lote = obtenerLista(headers, accion, urlConPaginaYCantidad, variables);
            acumulado.addAll(lote);
            if (lote.size() < TAMANIO_PAGINA_METRICAS) {
                break;
            }
        }
        return acumulado;
    }

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
