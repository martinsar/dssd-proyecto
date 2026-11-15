package ar.edu.unlp.dssd.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import ar.edu.unlp.dssd.exception.ManejadorErrores;
import ar.edu.unlp.dssd.model.Convocatoria;
import ar.edu.unlp.dssd.model.EstadoConvocatoria;
import ar.edu.unlp.dssd.model.EstadoOferta;
import ar.edu.unlp.dssd.model.Necesidad;
import ar.edu.unlp.dssd.model.Oferta;
import ar.edu.unlp.dssd.model.Organizacion;
import ar.edu.unlp.dssd.model.TipoRecurso;
import ar.edu.unlp.dssd.repository.ConvocatoriaRepository;
import ar.edu.unlp.dssd.repository.NecesidadRepository;
import ar.edu.unlp.dssd.repository.OfertaVersionRepository;
import ar.edu.unlp.dssd.repository.UsuarioRepository;
import ar.edu.unlp.dssd.repository.OfertaRepository;
import ar.edu.unlp.dssd.repository.OrganizacionRepository;
import ar.edu.unlp.dssd.repository.TipoRecursoRepository;
import ar.edu.unlp.dssd.seguridad.AutenticacionInterceptor;
import ar.edu.unlp.dssd.seguridad.Rol;
import ar.edu.unlp.dssd.seguridad.SesionUsuario;
import ar.edu.unlp.dssd.seguridad.SesionUsuarioResolver;
import ar.edu.unlp.dssd.service.OfertaService;

@ExtendWith(MockitoExtension.class)
class OfertaControllerTest {

    @Mock private OfertaRepository ofertaRepository;
    @Mock private ConvocatoriaRepository convocatoriaRepository;
    @Mock private OrganizacionRepository organizacionRepository;
    @Mock private TipoRecursoRepository tipoRecursoRepository;
    // [Entrega final] Lotes pedidos, versiones y usuario de la versión
    @Mock private NecesidadRepository necesidadRepository;
    @Mock private OfertaVersionRepository ofertaVersionRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @InjectMocks private OfertaService ofertaService;

    private MockMvc mvc;

    // [Entrega final] Sesión de la ONG con organización 2
    private final SesionUsuario sesionOng = new SesionUsuario("t", 3L, "caritas", "Rosa", "Representante",
            Rol.ONG, 2L, "Cáritas", null, Instant.now());

    @BeforeEach
    void configurar() {
        OfertaController controller = new OfertaController();
        ReflectionTestUtils.setField(controller, "ofertaService", ofertaService);
        mvc = MockMvcBuilders.standaloneSetup(controller)
                .setCustomArgumentResolvers(new SesionUsuarioResolver())
                .setControllerAdvice(new ManejadorErrores()).build();
    }

    // [Entrega final] Convocatoria PUBLICADA con la ventana abierta
    private static Convocatoria convocatoriaAbierta() {
        Convocatoria convocatoria = new Convocatoria();
        convocatoria.setId(7L);
        convocatoria.setEstado(EstadoConvocatoria.PUBLICADA);
        convocatoria.setFechaApertura(LocalDateTime.now().minusDays(1));
        convocatoria.setFechaCierre(LocalDateTime.now().plusDays(1));
        return convocatoria;
    }

    private static Necesidad lote(TipoRecurso recurso) {
        Necesidad necesidad = new Necesidad();
        necesidad.setTipoRecurso(recurso);
        necesidad.setCantidad(100);
        return necesidad;
    }

    @Test
    void crear_conElJsonDelFormulario_guardaLasTresRelaciones() throws Exception {
        Convocatoria convocatoria = convocatoriaAbierta();
        Organizacion organizacion = new Organizacion();
        organizacion.setId(2L);
        organizacion.setNombre("Cáritas");
        TipoRecurso recurso = new TipoRecurso();
        recurso.setId(1L);
        when(convocatoriaRepository.findById(7L)).thenReturn(Optional.of(convocatoria));
        when(necesidadRepository.findByConvocatoriaId(7L)).thenReturn(List.of(lote(recurso)));
        when(organizacionRepository.findById(2L)).thenReturn(Optional.of(organizacion));
        when(tipoRecursoRepository.findById(1L)).thenReturn(Optional.of(recurso));
        when(ofertaRepository.save(any(Oferta.class))).then(returnsFirstArg());

        mvc.perform(post("/api/ong/ofertas").requestAttr(AutenticacionInterceptor.ATRIBUTO_SESION, sesionOng)
                .contentType(MediaType.APPLICATION_JSON).content("""
                {"cantidad": 10, "descripcion": "Agua", "tipo_recurso_id": "1",
                 "organizacion_id": "2", "convocatoria_id": "7"}
                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.convocatoriaId").value(7))
                .andExpect(jsonPath("$.organizacion").value("Cáritas"))
                .andExpect(jsonPath("$.tipoRecursoId").value(1))
                .andExpect(jsonPath("$.numeroVersion").value(1));

        ArgumentCaptor<Oferta> captor = ArgumentCaptor.forClass(Oferta.class);
        verify(ofertaRepository).save(captor.capture());
        Oferta guardada = captor.getValue();
        assertThat(guardada.getConvocatoria()).isSameAs(convocatoria);
        assertThat(guardada.getOrganizacion()).isSameAs(organizacion);
        assertThat(guardada.getTipoRecurso()).isSameAs(recurso);
        assertThat(guardada.getFechaCreacion()).isEqualTo(LocalDate.now());
        assertThat(guardada.getCantidad()).isEqualTo(10);
        assertThat(guardada.getEstado()).isEqualTo(EstadoOferta.PENDIENTE);
    }

    @Test
    void crear_ignoraLaOrganizacionDelBodyYUsaLaDeLaSesion() throws Exception {
        Convocatoria convocatoria = convocatoriaAbierta();
        Organizacion organizacion = new Organizacion();
        organizacion.setId(2L);
        organizacion.setNombre("Cáritas");
        TipoRecurso recurso = new TipoRecurso();
        recurso.setId(1L);
        when(convocatoriaRepository.findById(7L)).thenReturn(Optional.of(convocatoria));
        when(necesidadRepository.findByConvocatoriaId(7L)).thenReturn(List.of(lote(recurso)));
        when(organizacionRepository.findById(2L)).thenReturn(Optional.of(organizacion));
        when(tipoRecursoRepository.findById(1L)).thenReturn(Optional.of(recurso));
        when(ofertaRepository.save(any(Oferta.class))).then(returnsFirstArg());

        mvc.perform(post("/api/ofertas").requestAttr(AutenticacionInterceptor.ATRIBUTO_SESION, sesionOng)
                .contentType(MediaType.APPLICATION_JSON).content("""
                {"cantidad":10,"descripcion":"Agua","tipoRecursoId":1,"organizacionId":99,"convocatoriaId":7}
                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.organizacion").value("Cáritas"));
        verify(organizacionRepository, never()).findById(99L);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "{}",
            """
            {"cantidad":10,"descripcion":"Agua"}
            """,
            """
            {"cantidad":0,"descripcion":" ","tipo_recurso_id":1,"organizacion_id":2,"convocatoria_id":7}
            """,
            """
            {"cantidad":10,"descripcion":"Agua","tipo_recurso_id":-1,"organizacion_id":2,"convocatoria_id":7}
            """
    })
    void crear_conDatosIncompletosOInvalidos_rechazaAntesDeConsultarRepositorios(String body) throws Exception {
        mvc.perform(post("/api/ong/ofertas").requestAttr(AutenticacionInterceptor.ATRIBUTO_SESION, sesionOng)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").isNotEmpty());
        verifyNoInteractions(ofertaRepository, convocatoriaRepository,
                organizacionRepository, tipoRecursoRepository);
    }

    @Test
    void crear_conConvocatoriaInexistente_noGuardaLaOferta() throws Exception {
        when(convocatoriaRepository.findById(7L)).thenReturn(Optional.empty());
        mvc.perform(post("/api/ong/ofertas").requestAttr(AutenticacionInterceptor.ATRIBUTO_SESION, sesionOng)
                .contentType(MediaType.APPLICATION_JSON).content("""
                {"cantidad":10,"descripcion":"Agua","tipoRecursoId":1,
                 "organizacionId":2,"convocatoriaId":7}
                """))
                .andExpect(status().isNotFound());
        verify(ofertaRepository, never()).save(any());
    }
}
