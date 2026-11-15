package ar.edu.unlp.dssd.seguridad;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.method.HandlerMethod;

import ar.edu.unlp.dssd.config.CorsConfig;
import ar.edu.unlp.dssd.controller.EmergenciaController;
import ar.edu.unlp.dssd.controller.OfertaController;
import ar.edu.unlp.dssd.dto.OfertasConsolidadasDto;
import ar.edu.unlp.dssd.exception.ManejadorErrores;
import ar.edu.unlp.dssd.model.Organizacion;
import ar.edu.unlp.dssd.model.Usuario;
import ar.edu.unlp.dssd.service.ConvocatoriaService;
import ar.edu.unlp.dssd.service.EmergenciaService;
import ar.edu.unlp.dssd.service.IntegracionService;
import ar.edu.unlp.dssd.service.OfertaService;

/** [Entrega final] Tests del control de acceso: token, perfil, clave de integración y CORS. */
class AutenticacionInterceptorTest {

    private static final String API_KEY = "clave-de-prueba";
    private static final String ORIGEN = "http://localhost:4200";

    @RestController
    @RequestMapping("/api/prueba")
    static class ControladorPrueba {

        @Publico
        @GetMapping("/publico")
        String publico() {
            return "publico";
        }

        @GetMapping("/autenticado")
        String autenticado(SesionUsuario sesion) {
            return sesion.username();
        }

        @RolesPermitidos(Rol.COORDINADOR)
        @GetMapping("/coordinador")
        String coordinador(SesionUsuario sesion) {
            return sesion.rol().name();
        }

        @AccesoIntegracion
        @RolesPermitidos(Rol.COORDINADOR)
        @GetMapping("/integracion")
        String integracion() {
            return "integracion";
        }

        @AccesoIntegracion
        @GetMapping("/solo-bonita")
        String soloBonita() {
            return "bonita";
        }
    }

    @RestController
    @RequestMapping("/api/prueba-ong")
    @RolesPermitidos(Rol.ONG)
    static class ControladorOng {

        @GetMapping
        String ong(SesionUsuario sesion) {
            return String.valueOf(sesion.organizacionId());
        }
    }

    private AlmacenSesiones almacen;
    private MockMvc mvc;
    private EmergenciaService emergenciaService;
    private IntegracionService integracionService;
    private OfertaService ofertaService;

    @BeforeEach
    void configurar() {
        almacen = new AlmacenSesiones(30, 8);
        mvc = armarMvc(almacen);
    }

    private MockMvc armarMvc(AlmacenSesiones almacenSesiones) {
        emergenciaService = mock(EmergenciaService.class);
        integracionService = mock(IntegracionService.class);
        ofertaService = mock(OfertaService.class);

        EmergenciaController emergencias = new EmergenciaController();
        ReflectionTestUtils.setField(emergencias, "emergenciaService", emergenciaService);
        ReflectionTestUtils.setField(emergencias, "convocatoriaService", mock(ConvocatoriaService.class));
        ReflectionTestUtils.setField(emergencias, "integracionService", integracionService);
        OfertaController ofertas = new OfertaController();
        ReflectionTestUtils.setField(ofertas, "ofertaService", ofertaService);

        return MockMvcBuilders.standaloneSetup(new ControladorPrueba(), new ControladorOng(), emergencias, ofertas)
                .addMappedInterceptors(new String[] {"/api/**"},
                        new AutenticacionInterceptor(almacenSesiones, API_KEY))
                .setCustomArgumentResolvers(new SesionUsuarioResolver())
                .setControllerAdvice(new ManejadorErrores())
                .addFilters(new CorsFilter(CorsConfig.fuenteCors(ORIGEN + ",http://127.0.0.1:4200")))
                .build();
    }

    private String token(AlmacenSesiones almacenSesiones, Rol rol, Long organizacionId) {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNombre("Nombre");
        usuario.setUsuarioBonita("usuario_" + rol.name().toLowerCase());
        if (organizacionId != null) {
            Organizacion organizacion = new Organizacion();
            organizacion.setId(organizacionId);
            organizacion.setNombre("Cáritas");
            usuario.setOrganizacion(organizacion);
        }
        return almacenSesiones.crear(usuario, rol, null).token();
    }

    private String bearer(Rol rol) {
        return "Bearer " + token(almacen, rol, rol == Rol.ONG ? 7L : null);
    }

    // ------------------------------------------------------------------
    // Token y perfil
    // ------------------------------------------------------------------

    @Test
    void sinToken_responde401ConElFormatoDeError() throws Exception {
        mvc.perform(get("/api/prueba/autenticado"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Iniciá sesión para continuar."));
    }

    @Test
    void conTokenDesconocido_responde401() throws Exception {
        mvc.perform(get("/api/prueba/autenticado").header(HttpHeaders.AUTHORIZATION, "Bearer inventado"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Tu sesión expiró. Iniciá sesión nuevamente."));
    }

    @Test
    void conSesionVencidaPorInactividad_responde401() throws Exception {
        AlmacenSesiones sinTolerancia = new AlmacenSesiones(0, 8);
        MockMvc mvcVencido = armarMvc(sinTolerancia);
        String token = token(sinTolerancia, Rol.COORDINADOR, null);

        mvcVencido.perform(get("/api/prueba/autenticado").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void endpointPublico_noExigeToken() throws Exception {
        mvc.perform(get("/api/prueba/publico"))
                .andExpect(status().isOk())
                .andExpect(content().string("publico"));
    }

    @Test
    void sinRolesPermitidos_alcanzaConCualquierSesion() throws Exception {
        mvc.perform(get("/api/prueba/autenticado").header(HttpHeaders.AUTHORIZATION, bearer(Rol.AUDITOR)))
                .andExpect(status().isOk())
                .andExpect(content().string("usuario_auditor"));
    }

    @Test
    void conElRolPermitido_responde200() throws Exception {
        mvc.perform(get("/api/prueba/coordinador").header(HttpHeaders.AUTHORIZATION, bearer(Rol.COORDINADOR)))
                .andExpect(status().isOk())
                .andExpect(content().string("COORDINADOR"));
    }

    @Test
    void conOtroRol_responde403ConElFormatoDeError() throws Exception {
        mvc.perform(get("/api/prueba/coordinador").header(HttpHeaders.AUTHORIZATION, bearer(Rol.ONG)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("No tenés permisos para realizar esta acción."));
    }

    @Test
    void rolesEnLaClase_seAplicanATodosSusMetodos() throws Exception {
        mvc.perform(get("/api/prueba-ong").header(HttpHeaders.AUTHORIZATION, bearer(Rol.ONG)))
                .andExpect(status().isOk())
                .andExpect(content().string("7"));
        mvc.perform(get("/api/prueba-ong").header(HttpHeaders.AUTHORIZATION, bearer(Rol.COORDINADOR)))
                .andExpect(status().isForbidden());
    }

    // ------------------------------------------------------------------
    // Clave de integración (Bonita)
    // ------------------------------------------------------------------

    @Test
    void integracion_conApiKeyValida_noExigeToken() throws Exception {
        mvc.perform(get("/api/prueba/integracion").header(AutenticacionInterceptor.HEADER_API_KEY, API_KEY))
                .andExpect(status().isOk())
                .andExpect(content().string("integracion"));
    }

    @Test
    void integracion_conApiKeyInvalida_responde401() throws Exception {
        mvc.perform(get("/api/prueba/integracion").header(AutenticacionInterceptor.HEADER_API_KEY, "otra"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Clave de integración inválida."));
    }

    @Test
    void integracion_tambienAceptaElRolPermitido() throws Exception {
        mvc.perform(get("/api/prueba/integracion").header(HttpHeaders.AUTHORIZATION, bearer(Rol.COORDINADOR)))
                .andExpect(status().isOk());
        mvc.perform(get("/api/prueba/integracion").header(HttpHeaders.AUTHORIZATION, bearer(Rol.ONG)))
                .andExpect(status().isForbidden());
    }

    @Test
    void soloIntegracion_conSesionDeUsuario_responde403() throws Exception {
        mvc.perform(get("/api/prueba/solo-bonita").header(HttpHeaders.AUTHORIZATION, bearer(Rol.COORDINADOR)))
                .andExpect(status().isForbidden());
    }

    @Test
    void apiKey_enUnEndpointSinIntegracion_seIgnoraYExigeToken() throws Exception {
        mvc.perform(get("/api/prueba/coordinador").header(AutenticacionInterceptor.HEADER_API_KEY, API_KEY))
                .andExpect(status().isUnauthorized());
    }

    // ------------------------------------------------------------------
    // CORS
    // ------------------------------------------------------------------

    @Test
    void preflight_pasaSinTokenYDevuelveLosHeadersCors() throws Exception {
        mvc.perform(options("/api/prueba/coordinador")
                        .header(HttpHeaders.ORIGIN, ORIGEN)
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "Authorization"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, ORIGEN));
    }

    @Test
    void preflight_elInterceptorLoDejaPasar() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("OPTIONS", "/api/prueba/coordinador");
        request.addHeader(HttpHeaders.ORIGIN, ORIGEN);
        request.addHeader(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET");
        HandlerMethod handler = new HandlerMethod(new ControladorPrueba(),
                ControladorPrueba.class.getDeclaredMethod("coordinador", SesionUsuario.class));

        boolean continua = new AutenticacionInterceptor(almacen, API_KEY)
                .preHandle(request, new MockHttpServletResponse(), handler);

        assertThat(continua).isTrue();
    }

    @Test
    void respuestas401Y403_llevanLosHeadersCors() throws Exception {
        mvc.perform(get("/api/prueba/coordinador").header(HttpHeaders.ORIGIN, ORIGEN))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, ORIGEN));
        mvc.perform(get("/api/prueba/coordinador").header(HttpHeaders.ORIGIN, "http://127.0.0.1:4200")
                        .header(HttpHeaders.AUTHORIZATION, bearer(Rol.ONG)))
                .andExpect(status().isForbidden())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://127.0.0.1:4200"));
    }

    // ------------------------------------------------------------------
    // Permisos de los controllers reales
    // ------------------------------------------------------------------

    @Test
    void consolidado_conApiKey_responde200() throws Exception {
        when(integracionService.obtenerConsolidado(eq(5L), any()))
                .thenReturn(new OfertasConsolidadasDto(5L, 9L, null, true, List.of(), List.of()));

        mvc.perform(get("/api/emergencias/5/ofertas").header(AutenticacionInterceptor.HEADER_API_KEY, API_KEY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.convocatoriaId").value(9))
                .andExpect(jsonPath("$.coberturaCompleta").value(true));
    }

    @Test
    void consolidado_conPerfilOng_responde403() throws Exception {
        mvc.perform(get("/api/emergencias/5/ofertas").header(HttpHeaders.AUTHORIZATION, bearer(Rol.ONG)))
                .andExpect(status().isForbidden());
        verifyNoInteractions(integracionService);
    }

    @Test
    void validaciones_conBodyInvalido_responde400SinRegistrar() throws Exception {
        mvc.perform(post("/api/emergencias/5/ofertas/validaciones")
                        .header(AutenticacionInterceptor.HEADER_API_KEY, API_KEY)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"resultados\":[{\"ofertaId\":21}]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").isNotEmpty());
        mvc.perform(post("/api/emergencias/5/ofertas/validaciones")
                        .header(AutenticacionInterceptor.HEADER_API_KEY, API_KEY)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"resultados\":[]}"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(integracionService);
    }

    @Test
    void validaciones_conApiKey_devuelveLaCantidadRegistrada() throws Exception {
        when(integracionService.registrarValidaciones(eq(5L), any())).thenReturn(2);

        mvc.perform(post("/api/emergencias/5/ofertas/validaciones")
                        .header(AutenticacionInterceptor.HEADER_API_KEY, API_KEY)
                        .contentType(MediaType.APPLICATION_JSON).content("""
                                {"resultados":[{"ofertaId":21,"habilitada":true},{"ofertaId":22,"habilitada":false}]}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.registradas").value(2));
    }

    @Test
    void emergencias_ongNoPuedeCrearNiListar() throws Exception {
        mvc.perform(get("/api/emergencias").header(HttpHeaders.AUTHORIZATION, bearer(Rol.ONG)))
                .andExpect(status().isForbidden());
        mvc.perform(post("/api/emergencias").header(HttpHeaders.AUTHORIZATION, bearer(Rol.ONG))
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden());
        verifyNoInteractions(emergenciaService);
    }

    @Test
    void emergencias_pendientesSoloParaElCoordinador() throws Exception {
        mvc.perform(get("/api/emergencias/pendientes").header(HttpHeaders.AUTHORIZATION, bearer(Rol.OPERADOR_MUNICIPAL)))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/emergencias/pendientes").header(HttpHeaders.AUTHORIZATION, bearer(Rol.COORDINADOR)))
                .andExpect(status().isOk());
    }

    @Test
    void ofertas_crearYListarSoloParaOng() throws Exception {
        mvc.perform(post("/api/ofertas").header(HttpHeaders.AUTHORIZATION, bearer(Rol.COORDINADOR))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"cantidad\":1,\"descripcion\":\"Agua\",\"tipoRecursoId\":1,\"convocatoriaId\":1}"))
                .andExpect(status().isForbidden());
        // [Entrega final] Los lotes se consultan en /api/ong/ofertas; el coordinador los ve en su seguimiento
        mvc.perform(get("/api/ong/ofertas").header(HttpHeaders.AUTHORIZATION, bearer(Rol.ONG)))
                .andExpect(status().isOk());
        mvc.perform(get("/api/ong/ofertas").header(HttpHeaders.AUTHORIZATION, bearer(Rol.COORDINADOR)))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/ong/ofertas").header(HttpHeaders.AUTHORIZATION, bearer(Rol.OPERADOR_MUNICIPAL)))
                .andExpect(status().isForbidden());
        verifyNoInteractions(emergenciaService);
    }
}
