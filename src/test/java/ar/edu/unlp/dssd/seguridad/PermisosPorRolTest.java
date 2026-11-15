package ar.edu.unlp.dssd.seguridad;

import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import ar.edu.unlp.dssd.controller.AuditorController;
import ar.edu.unlp.dssd.controller.CoordinadorController;
import ar.edu.unlp.dssd.controller.EmergenciaController;
import ar.edu.unlp.dssd.controller.MunicipioController;
import ar.edu.unlp.dssd.controller.NotificacionController;
import ar.edu.unlp.dssd.controller.OfertaConjuntaController;
import ar.edu.unlp.dssd.controller.OngController;
import ar.edu.unlp.dssd.exception.ManejadorErrores;
import ar.edu.unlp.dssd.model.Organizacion;
import ar.edu.unlp.dssd.model.Usuario;
import ar.edu.unlp.dssd.service.ConvocatoriaService;
import ar.edu.unlp.dssd.service.EmergenciaService;
import ar.edu.unlp.dssd.service.IndicadoresService;
import ar.edu.unlp.dssd.service.IntegracionService;
import ar.edu.unlp.dssd.service.InventarioService;
import ar.edu.unlp.dssd.service.MonitoreoService;
import ar.edu.unlp.dssd.service.MunicipioService;
import ar.edu.unlp.dssd.service.NotificacionService;
import ar.edu.unlp.dssd.service.OfertaConjuntaService;
import ar.edu.unlp.dssd.service.OngService;

/** [Entrega final] Cada perfil accede solo a sus endpoints de la etapa 2 */
class PermisosPorRolTest {

    private static final String API_KEY = "clave-de-prueba";

    private AlmacenSesiones almacen;
    private MockMvc mvc;

    @BeforeEach
    void configurar() {
        almacen = new AlmacenSesiones(30, 8);

        MunicipioController municipio = new MunicipioController();
        ReflectionTestUtils.setField(municipio, "municipioService", mock(MunicipioService.class));
        CoordinadorController coordinador = new CoordinadorController();
        ReflectionTestUtils.setField(coordinador, "monitoreoService", mock(MonitoreoService.class));
        ReflectionTestUtils.setField(coordinador, "convocatoriaService", mock(ConvocatoriaService.class));
        AuditorController auditor = new AuditorController();
        ReflectionTestUtils.setField(auditor, "indicadoresService", mock(IndicadoresService.class));
        OngController ong = new OngController();
        ReflectionTestUtils.setField(ong, "ongService", mock(OngService.class));
        ReflectionTestUtils.setField(ong, "inventarioService", mock(InventarioService.class));
        NotificacionController notificaciones = new NotificacionController();
        ReflectionTestUtils.setField(notificaciones, "notificacionService", mock(NotificacionService.class));
        OfertaConjuntaController conjuntas = new OfertaConjuntaController();
        ReflectionTestUtils.setField(conjuntas, "ofertaConjuntaService", mock(OfertaConjuntaService.class));
        EmergenciaController emergencias = new EmergenciaController();
        ReflectionTestUtils.setField(emergencias, "emergenciaService", mock(EmergenciaService.class));
        ReflectionTestUtils.setField(emergencias, "convocatoriaService", mock(ConvocatoriaService.class));
        ReflectionTestUtils.setField(emergencias, "integracionService", mock(IntegracionService.class));

        mvc = MockMvcBuilders.standaloneSetup(municipio, coordinador, auditor, ong, notificaciones, conjuntas,
                        emergencias)
                .addMappedInterceptors(new String[] {"/api/**"}, new AutenticacionInterceptor(almacen, API_KEY))
                .setCustomArgumentResolvers(new SesionUsuarioResolver())
                .setControllerAdvice(new ManejadorErrores())
                .build();
    }

    private String bearer(Rol rol) {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setUsuarioBonita("usuario_" + rol.name().toLowerCase());
        if (rol == Rol.ONG) {
            Organizacion organizacion = new Organizacion();
            organizacion.setId(7L);
            organizacion.setNombre("Cáritas");
            usuario.setOrganizacion(organizacion);
        }
        return "Bearer " + almacen.crear(usuario, rol, null).token();
    }

    private void esperar(String ruta, Rol rol, ResultMatcher resultado) throws Exception {
        mvc.perform(get(ruta).header(HttpHeaders.AUTHORIZATION, bearer(rol))).andExpect(resultado);
    }

    @Test
    void municipio_soloParaElOperadorMunicipal() throws Exception {
        esperar("/api/municipio/emergencias", Rol.OPERADOR_MUNICIPAL, status().isOk());
        esperar("/api/municipio/emergencias", Rol.COORDINADOR, status().isForbidden());
        esperar("/api/municipio/emergencias", Rol.ONG, status().isForbidden());
        esperar("/api/municipio/emergencias", Rol.AUDITOR, status().isForbidden());
    }

    @Test
    void coordinador_soloParaElCoordinador() throws Exception {
        esperar("/api/coordinador/monitoreo", Rol.COORDINADOR, status().isOk());
        esperar("/api/coordinador/monitoreo", Rol.OPERADOR_MUNICIPAL, status().isForbidden());
        esperar("/api/coordinador/monitoreo", Rol.ONG, status().isForbidden());
    }

    @Test
    void coordinador_conUnaEtapaInvalida_responde400() throws Exception {
        esperar("/api/coordinador/monitoreo?etapa=INVENTADA", Rol.COORDINADOR, status().isBadRequest());
    }

    @Test
    void auditor_soloParaElAuditor() throws Exception {
        esperar("/api/auditor/indicadores", Rol.AUDITOR, status().isOk());
        esperar("/api/auditor/emergencias", Rol.COORDINADOR, status().isForbidden());
    }

    @Test
    void ong_soloParaLasOngs() throws Exception {
        esperar("/api/ong/convocatorias", Rol.ONG, status().isOk());
        esperar("/api/ong/notificaciones/no-leidas", Rol.ONG, status().isOk());
        esperar("/api/ong/ofertas-conjuntas", Rol.ONG, status().isOk());
        esperar("/api/ong/inventario", Rol.COORDINADOR, status().isForbidden());
        esperar("/api/ong/notificaciones", Rol.OPERADOR_MUNICIPAL, status().isForbidden());
        esperar("/api/ong/ofertas-conjuntas", Rol.AUDITOR, status().isForbidden());
    }

    @Test
    void confirmacionDeAdjudicacion_aceptaLaClaveDeBonitaYNoAUnaOng() throws Exception {
        mvc.perform(post("/api/emergencias/5/adjudicacion/notificaciones")
                        .header(AutenticacionInterceptor.HEADER_API_KEY, API_KEY))
                .andExpect(status().isOk());
        mvc.perform(post("/api/emergencias/5/adjudicacion/notificaciones")
                        .header(HttpHeaders.AUTHORIZATION, bearer(Rol.ONG)))
                .andExpect(status().isForbidden());
        mvc.perform(post("/api/emergencias/5/adjudicacion/notificaciones")
                        .header(AutenticacionInterceptor.HEADER_API_KEY, "otra"))
                .andExpect(status().isUnauthorized());
    }
}
