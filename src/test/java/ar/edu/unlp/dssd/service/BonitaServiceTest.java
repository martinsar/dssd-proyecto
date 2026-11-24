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
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withUnauthorizedRequest;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.RequestMatcher;
import org.springframework.test.web.client.ResponseActions;
import org.springframework.web.client.RestTemplate;

import ar.edu.unlp.dssd.exception.BonitaException;
import ar.edu.unlp.dssd.exception.NoAutenticadoException;

/**
 * Tests de BonitaService contra un Bonita simulado (MockRestServiceServer).
 * Las expectativas se declaran en el mismo orden en que el servicio hace las llamadas.
 */
class BonitaServiceTest {

    private static final String BASE = "http://bonita.test";
    private static final String PROCESO = "GestionEmergencias";
    private static final String MUNICIPIO = "userMunicipio";
    private static final String COORDINADOR = "userCoordinador";
    private static final String ONG = "userOng";
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
                MUNICIPIO, pwMunicipio, COORDINADOR, pwCoordinador, ONG, "pwOng");
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
    void seleccionarOfertas_comoMunicipio_asignaYEjecutaLaTareaConContratoVacio() {
        esperarLogin();
        esperarSesion("3");
        esperarBusquedaTarea(999L, "Seleccionar%20ofertas", "[{\"id\":\"7001\"}]");
        esperarAsignacion("7001", "3");
        esperarEjecucion("7001");

        assertThat(bonitaService.seleccionarOfertas(999L)).isTrue();
        server.verify();
    }

    @Test
    void finalizarActividades_comoOngYSinLaTarea_devuelveFalseSinEjecutar() {
        esperarLogin(ONG, "pwOng");
        esperarSesion("9");
        // [Entrega final] Las tareas finales se buscan solo 4 veces
        for (int i = 0; i < 4; i++) {
            esperarBusquedaTarea(999L, "Finalizar%20actividades", "[]");
        }

        assertThat(bonitaService.finalizarActividades(999L)).isFalse();
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
    // [Entrega final] Login del usuario real
    // ------------------------------------------------------------------

    @Test
    void iniciarSesion_conCredencialesValidas_devuelveLaSesionConElUsuarioDeBonita() {
        esperarLogin("caritas", "bpm");
        esperarAutenticada(BASE + "/API/system/session/unusedid", HttpMethod.GET)
                .andRespond(withSuccess("{\"user_id\":\"12\",\"user_name\":\"caritas\"}",
                        MediaType.APPLICATION_JSON));

        SesionBonita sesion = bonitaService.iniciarSesion(" caritas ", "bpm");

        assertThat(sesion.userId()).isEqualTo("12");
        assertThat(sesion.username()).isEqualTo("caritas");
        assertThat(sesion.jSessionId()).isEqualTo("abc");
        assertThat(sesion.apiToken()).isEqualTo("tok");
        server.verify();
    }

    @Test
    void iniciarSesion_siBonitaResponde401_lanzaNoAutenticado() {
        server.expect(requestTo(BASE + "/loginservice"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withUnauthorizedRequest());

        assertThatThrownBy(() -> bonitaService.iniciarSesion("caritas", "mal"))
                .isInstanceOf(NoAutenticadoException.class)
                .hasMessage("Usuario o contraseña incorrectos.");
        server.verify();
    }

    @Test
    void iniciarSesion_siBonitaFalla_lanzaBonitaException() {
        server.expect(requestTo(BASE + "/loginservice"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withServerError());

        assertThatThrownBy(() -> bonitaService.iniciarSesion("caritas", "bpm"))
                .isInstanceOf(BonitaException.class);
        server.verify();
    }

    @Test
    void iniciarSesion_sinPassword_lanzaNoAutenticadoSinLlamarABonita() {
        assertThatThrownBy(() -> bonitaService.iniciarSesion("caritas", " "))
                .isInstanceOf(NoAutenticadoException.class);
        server.verify();
    }

    @Test
    void obtenerRolesUsuario_devuelveLosNombresDeRolDeLasMembresias() {
        esperarAutenticada(BASE + "/API/identity/membership?p=0&c=50&f=user_id=12&d=role_id", HttpMethod.GET)
                .andRespond(withSuccess("""
                        [{"role_id":{"id":"3","name":"Organizaciones"}},{"role_id":{"id":"4","name":"Otro"}}]
                        """, MediaType.APPLICATION_JSON));

        assertThat(bonitaService.obtenerRolesUsuario(new SesionBonita("12", "caritas", "abc", "tok")))
                .containsExactly("Organizaciones", "Otro");
        server.verify();
    }

    @Test
    void obtenerRolesUsuario_siBonitaResponde403_devuelveListaVacia() {
        esperarAutenticada(BASE + "/API/identity/membership?p=0&c=50&f=user_id=12&d=role_id", HttpMethod.GET)
                .andRespond(withStatus(HttpStatus.FORBIDDEN));

        assertThat(bonitaService.obtenerRolesUsuario(new SesionBonita("12", "caritas", "abc", "tok"))).isEmpty();
        server.verify();
    }

    @Test
    void cerrarSesion_llamaAlLogoutYToleraErrores() {
        esperarAutenticada(BASE + "/logoutservice?redirect=false", HttpMethod.GET)
                .andRespond(withServerError());

        bonitaService.cerrarSesion(new SesionBonita("12", "caritas", "abc", "tok"));

        server.verify();
    }

    // ------------------------------------------------------------------
    // [Entrega final] Consultas de métricas y auditoría
    // ------------------------------------------------------------------

    @Test
    void obtenerDatosMetricasProceso_usaSesionDelUsuarioSiTienePermisos() {
        esperarAutenticada(BASE + "/API/bpm/process?p=0&c=50&f=name=" + PROCESO, HttpMethod.GET)
                .andRespond(withSuccess("[{\"id\":\"8001\",\"name\":\"GestionEmergencias\",\"version\":\"1.0\",\"activationState\":\"ENABLED\"}]",
                        MediaType.APPLICATION_JSON));
        esperarAutenticada(BASE + "/API/bpm/archivedFlowNode?p=0&c=100&f=processId=8001",
                HttpMethod.GET)
                .andRespond(withSuccess("[{\"name\":\"Desglosar Lotes\",\"caseId\":\"10\"}]",
                        MediaType.APPLICATION_JSON));
        esperarAutenticada(BASE + "/API/bpm/archivedCase?p=0&c=100&f=processDefinitionId=8001", HttpMethod.GET)
                .andRespond(withSuccess("[{\"sourceObjectId\":\"10\"}]", MediaType.APPLICATION_JSON));
        esperarAutenticada(BASE + "/API/bpm/case?p=0&c=100&f=processDefinitionId=8001", HttpMethod.GET)
                .andRespond(withSuccess("[{\"id\":\"11\"}]", MediaType.APPLICATION_JSON));
        esperarAutenticada(BASE + "/API/bpm/humanTask?p=0&c=100&f=processId=8001", HttpMethod.GET)
                .andRespond(withSuccess("[{\"name\":\"Gestionar Convocatoria\",\"caseId\":\"11\"}]",
                        MediaType.APPLICATION_JSON));
        esperarAutenticada(BASE + "/API/bpm/flowNode?p=0&c=100&f=processId=8001&f=state=failed", HttpMethod.GET)
                .andRespond(withSuccess("[{\"caseId\":\"11\",\"state\":\"failed\"}]", MediaType.APPLICATION_JSON));
        esperarAutenticada(BASE + "/API/identity/user?p=0&c=100", HttpMethod.GET)
                .andRespond(withSuccess("[{\"id\":\"7\",\"userName\":\"agente_coordinador_1\"}]",
                        MediaType.APPLICATION_JSON));

        var datos = bonitaService.obtenerDatosMetricasProceso(new SesionBonita("5", "auditor", "abc", "tok"));

        assertThat(datos.procesos()).hasSize(1);
        assertThat(datos.flujosArchivados()).hasSize(1);
        assertThat(datos.casosArchivados()).hasSize(1);
        assertThat(datos.casosActivos()).hasSize(1);
        assertThat(datos.tareasPendientes()).hasSize(1);
        assertThat(datos.tareasFallidas()).hasSize(1);
        assertThat(datos.usuariosPorId()).containsEntry("7", "agente_coordinador_1");
        server.verify();
    }

    @Test
    void obtenerDatosMetricasProceso_siLaSesionRecibe403_reintentaConUsuarioAuditorYCierraSesion() {
        esperarAutenticada(BASE + "/API/bpm/process?p=0&c=50&f=name=" + PROCESO, HttpMethod.GET)
                .andRespond(withStatus(HttpStatus.FORBIDDEN));
        esperarLogin("install", "install");
        esperarAutenticada(BASE + "/API/bpm/process?p=0&c=50&f=name=" + PROCESO, HttpMethod.GET)
                .andRespond(withSuccess("[{\"id\":\"8001\"}]", MediaType.APPLICATION_JSON));
        esperarAutenticada(BASE + "/API/bpm/archivedFlowNode?p=0&c=100&f=processId=8001",
                HttpMethod.GET)
                .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));
        esperarAutenticada(BASE + "/API/bpm/archivedCase?p=0&c=100&f=processDefinitionId=8001", HttpMethod.GET)
                .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));
        esperarAutenticada(BASE + "/API/bpm/case?p=0&c=100&f=processDefinitionId=8001", HttpMethod.GET)
                .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));
        esperarAutenticada(BASE + "/API/bpm/humanTask?p=0&c=100&f=processId=8001", HttpMethod.GET)
                .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));
        esperarAutenticada(BASE + "/API/bpm/flowNode?p=0&c=100&f=processId=8001&f=state=failed", HttpMethod.GET)
                .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));
        esperarAutenticada(BASE + "/API/identity/user?p=0&c=100", HttpMethod.GET)
                .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));
        esperarAutenticada(BASE + "/logoutservice?redirect=false", HttpMethod.GET)
                .andRespond(withNoContent());

        var datos = bonitaService.obtenerDatosMetricasProceso(new SesionBonita("5", "auditor", "abc", "tok"));

        assertThat(datos.flujosArchivados()).isEmpty();
        assertThat(datos.casosArchivados()).isEmpty();
        assertThat(datos.casosActivos()).isEmpty();
        assertThat(datos.tareasPendientes()).isEmpty();
        assertThat(datos.tareasFallidas()).isEmpty();
        server.verify();
    }

    @Test
    void obtenerDatosMetricasCaso_sinSesion_autenticaConsultaFlujosPendientesYCasoYCierraSesion() {
        esperarLogin("install", "install");
        esperarAutenticada(BASE + "/API/bpm/archivedFlowNode?p=0&c=100&f=caseId=99",
                HttpMethod.GET)
                .andRespond(withSuccess("[{\"name\":\"Registrar Emergencia.\"}]", MediaType.APPLICATION_JSON));
        esperarAutenticada(BASE + "/API/bpm/humanTask?p=0&c=100&f=caseId=99", HttpMethod.GET)
                .andRespond(withSuccess("[{\"name\":\"Desglosar Lotes\"}]", MediaType.APPLICATION_JSON));
        esperarAutenticada(BASE + "/API/bpm/archivedCase?p=0&c=1&f=sourceObjectId=99", HttpMethod.GET)
                .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));
        esperarAutenticada(BASE + "/logoutservice?redirect=false", HttpMethod.GET)
                .andRespond(withNoContent());

        var datos = bonitaService.obtenerDatosMetricasCaso(99L, null);

        assertThat(datos.flujosArchivados()).hasSize(1);
        assertThat(datos.tareasPendientes()).hasSize(1);
        assertThat(datos.casoArchivado()).isEmpty();
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
