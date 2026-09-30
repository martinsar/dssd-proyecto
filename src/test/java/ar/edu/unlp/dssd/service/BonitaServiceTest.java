package ar.edu.unlp.dssd.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withNoContent;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.RequestMatcher;
import org.springframework.test.web.client.ResponseActions;
import org.springframework.web.client.RestTemplate;

import ar.edu.unlp.dssd.exception.BonitaException;

/**
 * Tests de BonitaService contra un Bonita simulado (MockRestServiceServer).
 * Las expectativas se declaran en el mismo orden en que el servicio hace las llamadas.
 */
class BonitaServiceTest {

    private static final String BASE = "http://bonita.test";
    private static final String PROCESO = "GestionEmergencias";
    private static final String MUNICIPIO = "userMunicipio";
    private static final String COORDINADOR = "userCoordinador";
    private static final String COOKIE_ESPERADA = "JSESSIONID=abc; X-Bonita-API-Token=tok";

    private MockRestServiceServer server;
    private BonitaService bonitaService;

    @BeforeEach
    void setUp() {
        RestTemplate restTemplate = new RestTemplate();
        server = MockRestServiceServer.bindTo(restTemplate).build();
        // Credenciales falsas
        bonitaService = crearServicio(restTemplate, "pwMunicipio", "pwCoordinador");
    }

    private BonitaService crearServicio(RestTemplate restTemplate, String pwMunicipio, String pwCoordinador) {
        return new BonitaService(restTemplate, BASE, PROCESO,
                MUNICIPIO, pwMunicipio, COORDINADOR, pwCoordinador);
    }

    // ------------------------------------------------------------------
    // Helpers de expectativas
    // ------------------------------------------------------------------

    /** Login del Municipio (instanciar y "Registrar Emergencia."). */
    private void esperarLogin() {
        esperarLogin(MUNICIPIO, "pwMunicipio");
    }

    /** Login exitoso: Bonita responde 204 con las dos cookies. */
    private void esperarLogin(String usuario, String password) {
        server.expect(requestTo(BASE + "/loginservice"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().string("username=" + usuario + "&password=" + password + "&redirect=false"))
                .andRespond(withNoContent().header(HttpHeaders.SET_COOKIE,
                        "JSESSIONID=abc; Path=/bonita", "X-Bonita-API-Token=tok; Path=/bonita"));
    }

    /**
     * Compara URLs ya decodificadas. RestTemplate (Spring 7) codifica el "=" dentro del valor de un
     * query param ("f=caseId%3D999"), que el servidor decodifica igual que "f=caseId=999": para Bonita
     * son la misma URL, asi que el test no debe depender de ese detalle de codificacion.
     */
    private static RequestMatcher urlDecodificada(String esperada) {
        return request -> assertThat(URLDecoder.decode(request.getURI().toString(), StandardCharsets.UTF_8))
                .isEqualTo(URLDecoder.decode(esperada, StandardCharsets.UTF_8));
    }

    /** Toda llamada posterior al login tiene que llevar el token y las cookies de sesion. */
    private ResponseActions esperarAutenticada(String url, HttpMethod metodo) {
        return server.expect(urlDecodificada(url))
                .andExpect(method(metodo))
                .andExpect(header("X-Bonita-API-Token", "tok"))
                .andExpect(header(HttpHeaders.COOKIE, COOKIE_ESPERADA));
    }

    private void esperarSesion(String userId) {
        esperarAutenticada(BASE + "/API/system/session/unusedid", HttpMethod.GET)
                .andRespond(withSuccess("{\"user_id\":\"" + userId + "\"}", MediaType.APPLICATION_JSON));
    }

    private void esperarProcesoHabilitado(String processId) {
        esperarAutenticada(BASE + "/API/bpm/process?p=0&c=1&f=name=" + PROCESO + "&f=activationState=ENABLED",
                HttpMethod.GET)
                .andRespond(withSuccess("[{\"id\":\"" + processId + "\"}]", MediaType.APPLICATION_JSON));
    }

    /** nombreCodificado: el nombre de la tarea tal como debe viajar en la URL (espacios como %20). */
    private void esperarBusquedaTarea(long caseId, String nombreCodificado, String respuestaJson) {
        esperarAutenticada(BASE + "/API/bpm/humanTask?p=0&c=10&f=caseId=" + caseId + "&f=name=" + nombreCodificado,
                HttpMethod.GET)
                .andRespond(withSuccess(respuestaJson, MediaType.APPLICATION_JSON));
    }

    private void esperarAsignacion(String taskId, String userId) {
        esperarAutenticada(BASE + "/API/bpm/userTask/" + taskId, HttpMethod.PUT)
                .andExpect(jsonPath("$.assigned_id").value(userId))
                .andRespond(withNoContent());
    }

    private void esperarEjecucion(String taskId) {
        esperarAutenticada(BASE + "/API/bpm/userTask/" + taskId + "/execution", HttpMethod.POST)
                .andRespond(withNoContent());
    }

    // ------------------------------------------------------------------
    // iniciarInstanciaEmergencia
    // ------------------------------------------------------------------

    @Test
    void iniciarInstanciaEmergencia_conCaseIdNumerico_devuelveElCaseId() {
        esperarLogin();
        esperarProcesoHabilitado("8001");
        esperarAutenticada(BASE + "/API/bpm/process/8001/instantiation", HttpMethod.POST)
                .andExpect(jsonPath("$.idEmergenciaInput").value(42))
                .andExpect(jsonPath("$.tipoDesastreInput").value("Inundacion"))
                .andRespond(withSuccess("{\"caseId\":12345}", MediaType.APPLICATION_JSON));

        Long caseId = bonitaService.iniciarInstanciaEmergencia(42L, "Inundacion");

        assertThat(caseId).isEqualTo(12345L);
        server.verify();
    }

    @Test
    void iniciarInstanciaEmergencia_conCaseIdComoString_lo_convierteALong() {
        esperarLogin();
        esperarProcesoHabilitado("8001");
        esperarAutenticada(BASE + "/API/bpm/process/8001/instantiation", HttpMethod.POST)
                .andRespond(withSuccess("{\"caseId\":\"777\"}", MediaType.APPLICATION_JSON));

        Long caseId = bonitaService.iniciarInstanciaEmergencia(1L, "Sismo");

        assertThat(caseId).isEqualTo(777L);
        server.verify();
    }

    @Test
    void iniciarInstanciaEmergencia_sinProcesoHabilitado_lanzaBonitaExceptionYNoInstancia() {
        esperarLogin();
        esperarAutenticada(BASE + "/API/bpm/process?p=0&c=1&f=name=" + PROCESO + "&f=activationState=ENABLED",
                HttpMethod.GET)
                .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));
        // No se declara la expectativa de instantiation: si el servicio la llamara, el server falla.

        assertThatThrownBy(() -> bonitaService.iniciarInstanciaEmergencia(42L, "Inundacion"))
                .isInstanceOf(BonitaException.class)
                .hasMessageContaining(PROCESO);
        server.verify();
    }

    @Test
    void iniciarInstanciaEmergencia_siBonitaNoDevuelveCaseId_lanzaBonitaException() {
        esperarLogin();
        esperarProcesoHabilitado("8001");
        esperarAutenticada(BASE + "/API/bpm/process/8001/instantiation", HttpMethod.POST)
                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> bonitaService.iniciarInstanciaEmergencia(42L, "Inundacion"))
                .isInstanceOf(BonitaException.class);
        server.verify();
    }

    @Test
    void iniciarInstanciaEmergencia_siElLoginNoDevuelveCookies_lanzaBonitaException() {
        server.expect(requestTo(BASE + "/loginservice"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withNoContent());

        assertThatThrownBy(() -> bonitaService.iniciarInstanciaEmergencia(42L, "Inundacion"))
                .isInstanceOf(BonitaException.class);
        server.verify();
    }

    @Test
    void iniciarInstanciaEmergencia_siBonitaRespondeError500_seTraduceABonitaException() {
        server.expect(requestTo(BASE + "/loginservice"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withServerError());

        assertThatThrownBy(() -> bonitaService.iniciarInstanciaEmergencia(42L, "Inundacion"))
                .isInstanceOf(BonitaException.class);
        server.verify();
    }

    // ------------------------------------------------------------------
    // completarPasosIniciales
    // ------------------------------------------------------------------

    @Test
    void completarPasosIniciales_buscaAsignaEjecutaYEnviaMensajeDeAlerta() {
        esperarLogin();
        esperarSesion("7");
        // La tarea aparece al primer intento (asi no se espera entre reintentos)
        esperarBusquedaTarea(999L, "Registrar%20Emergencia.", "[{\"id\":\"5001\"}]");
        esperarAsignacion("5001", "7");
        esperarEjecucion("5001");
        esperarAutenticada(BASE + "/API/bpm/message", HttpMethod.POST)
                .andExpect(jsonPath("$.messageName").value("Se recibe un alerta de emergencia"))
                .andExpect(jsonPath("$.targetProcess").value(PROCESO))
                .andExpect(jsonPath("$.targetFlowNode").value("Recibir Alerta"))
                .andExpect(jsonPath("$.correlations.idEmergencia.value").value(42))
                .andExpect(jsonPath("$.correlations.idEmergencia.type").value("java.lang.Long"))
                .andRespond(withNoContent());

        bonitaService.completarPasosIniciales(999L, 42L);

        server.verify();
    }

    @Test
    void completarPasosIniciales_siLaSesionNoTrae_userId_lanzaBonitaExceptionYNoBuscaTareas() {
        esperarLogin();
        esperarAutenticada(BASE + "/API/system/session/unusedid", HttpMethod.GET)
                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> bonitaService.completarPasosIniciales(999L, 42L))
                .isInstanceOf(BonitaException.class);
        server.verify();
    }

    @Test
    void completarPasosIniciales_siFallaLaEjecucionDeLaTarea_noEnviaElMensaje() {
        esperarLogin();
        esperarSesion("7");
        esperarBusquedaTarea(999L, "Registrar%20Emergencia.", "[{\"id\":\"5001\"}]");
        esperarAsignacion("5001", "7");
        esperarAutenticada(BASE + "/API/bpm/userTask/5001/execution", HttpMethod.POST)
                .andRespond(withServerError());
        // Sin expectativa de /API/bpm/message

        assertThatThrownBy(() -> bonitaService.completarPasosIniciales(999L, 42L))
                .isInstanceOf(BonitaException.class);
        server.verify();
    }

    // ------------------------------------------------------------------
    // completarRelevamiento
    // ------------------------------------------------------------------

    @Test
    void completarRelevamiento_conCoordinador_buscaAsignaYEjecutaDesglosarLotes() {
        esperarLogin(COORDINADOR, "pwCoordinador");
        esperarSesion("7");
        esperarBusquedaTarea(999L, "Desglosar%20Lotes", "[{\"id\":\"6001\"}]");
        esperarAsignacion("6001", "7");
        esperarEjecucion("6001");
        // Sin expectativa de mensaje: el relevamiento no notifica

        bonitaService.completarRelevamiento(999L);

        server.verify();
    }

    @Test
    void completarRelevamiento_siLaTareaNoAparece_lanzaBonitaException() {
        esperarLogin(COORDINADOR, "pwCoordinador");
        esperarSesion("7");
        // Se agotan los 20 intentos (con 250ms de espera entre cada uno)
        for (int i = 0; i < 20; i++) {
            esperarBusquedaTarea(999L, "Desglosar%20Lotes", "[]");
        }

        assertThatThrownBy(() -> bonitaService.completarRelevamiento(999L))
                .isInstanceOf(BonitaException.class)
                .hasMessageContaining("Desglosar Lotes");
        server.verify();
    }

    // ------------------------------------------------------------------
    // publicarConvocatoria
    // ------------------------------------------------------------------

    @Test
    void publicarConvocatoria_completaGestionarConvocatoriaYNotifica_sinTocarDesglosarLotes() {
        esperarLogin(COORDINADOR, "pwCoordinador");
        esperarSesion("7");
        // Solo se busca "Gestionar Convocatoria": cualquier busqueda de "Desglosar Lotes" haria fallar el test
        esperarBusquedaTarea(999L, "Gestionar%20Convocatoria", "[{\"id\":\"6002\"}]");
        esperarAsignacion("6002", "7");
        esperarAutenticada(BASE + "/API/bpm/userTask/6002/execution", HttpMethod.POST)
                .andExpect(jsonPath("$.idConvocatoriaInput").value(77))
                // Sin fraccion de segundo, formato yyyy-MM-dd'T'HH:mm:ss
                .andExpect(jsonPath("$.fechaCierreInput").value("2030-01-15T10:30:45"))
                .andRespond(withNoContent());
        esperarAutenticada(BASE + "/API/bpm/message", HttpMethod.POST)
                .andExpect(jsonPath("$.messageName").value("Recibe una notificación"))
                .andExpect(jsonPath("$.targetFlowNode").value("Recibir Notificación"))
                .andExpect(jsonPath("$.correlations.idEmergencia.value").value(42))
                .andRespond(withNoContent());

        bonitaService.publicarConvocatoria(999L, 42L, 77L,
                LocalDateTime.of(2030, 1, 15, 10, 30, 45, 999_000_000));

        server.verify();
    }

    @Test
    void publicarConvocatoria_siGestionarConvocatoriaYaEstaArchivada_soloReenviaLaNotificacion() {
        esperarLogin(COORDINADOR, "pwCoordinador");
        esperarSesion("7");
        // 20 busquedas vacias (con 250ms de espera entre cada una) y despues la tarea aparece archivada
        for (int i = 0; i < 20; i++) {
            esperarBusquedaTarea(999L, "Gestionar%20Convocatoria", "[]");
        }
        esperarAutenticada(BASE + "/API/bpm/archivedHumanTask?p=0&c=1&f=caseId=999&f=name=Gestionar%20Convocatoria",
                HttpMethod.GET)
                .andRespond(withSuccess("[{\"id\":\"6002\"}]", MediaType.APPLICATION_JSON));
        esperarAutenticada(BASE + "/API/bpm/message", HttpMethod.POST)
                .andExpect(jsonPath("$.targetFlowNode").value("Recibir Notificación"))
                .andRespond(withNoContent());

        bonitaService.publicarConvocatoria(999L, 42L, 77L, LocalDateTime.of(2030, 1, 15, 10, 30, 0));

        server.verify();
    }

    // ------------------------------------------------------------------
    // Contraseñas sin configurar
    // ------------------------------------------------------------------

    @Test
    void iniciarInstanciaEmergencia_conPasswordMunicipioVacia_lanzaBonitaExceptionSinLlamarABonita() {
        BonitaService servicio = crearServicio(restTemplateNuevo(), "", "pwCoordinador");

        assertThatThrownBy(() -> servicio.iniciarInstanciaEmergencia(42L, "Inundacion"))
                .isInstanceOf(BonitaException.class)
                .hasMessageContaining("bonita.usuarios.municipio.password");
        server.verify(); // sin expectativas: no se hizo ninguna llamada HTTP
    }

    @Test
    void publicarConvocatoria_conPasswordCoordinadorNula_lanzaBonitaExceptionSinLlamarABonita() {
        BonitaService servicio = crearServicio(restTemplateNuevo(), "pwMunicipio", null);

        assertThatThrownBy(() -> servicio.publicarConvocatoria(999L, 42L, 77L,
                LocalDateTime.of(2030, 1, 15, 10, 30, 0)))
                .isInstanceOf(BonitaException.class)
                .hasMessageContaining("bonita.usuarios.coordinador.password");
        server.verify();
    }

    /** Crea un RestTemplate con un mock sin expectativas (cualquier llamada HTTP haria fallar el test). */
    private RestTemplate restTemplateNuevo() {
        RestTemplate restTemplate = new RestTemplate();
        server = MockRestServiceServer.bindTo(restTemplate).build();
        return restTemplate;
    }
}
