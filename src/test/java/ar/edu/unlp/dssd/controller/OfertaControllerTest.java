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

import java.time.LocalDate;
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
import ar.edu.unlp.dssd.model.Oferta;
import ar.edu.unlp.dssd.model.Organizacion;
import ar.edu.unlp.dssd.model.TipoRecurso;
import ar.edu.unlp.dssd.repository.ConvocatoriaRepository;
import ar.edu.unlp.dssd.repository.OfertaRepository;
import ar.edu.unlp.dssd.repository.OrganizacionRepository;
import ar.edu.unlp.dssd.repository.TipoRecursoRepository;
import ar.edu.unlp.dssd.service.OfertaService;

@ExtendWith(MockitoExtension.class)
class OfertaControllerTest {

    @Mock private OfertaRepository ofertaRepository;
    @Mock private ConvocatoriaRepository convocatoriaRepository;
    @Mock private OrganizacionRepository organizacionRepository;
    @Mock private TipoRecursoRepository tipoRecursoRepository;
    @InjectMocks private OfertaService ofertaService;

    private MockMvc mvc;

    @BeforeEach
    void configurar() {
        OfertaController controller = new OfertaController();
        ReflectionTestUtils.setField(controller, "ofertaService", ofertaService);
        mvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new ManejadorErrores()).build();
    }

    @Test
    void crear_conElJsonDelFormulario_guardaLasTresRelaciones() throws Exception {
        Convocatoria convocatoria = new Convocatoria();
        convocatoria.setId(7L);
        Organizacion organizacion = new Organizacion();
        organizacion.setId(2L);
        TipoRecurso recurso = new TipoRecurso();
        recurso.setId(1L);
        when(convocatoriaRepository.findById(7L)).thenReturn(Optional.of(convocatoria));
        when(organizacionRepository.findById(2L)).thenReturn(Optional.of(organizacion));
        when(tipoRecursoRepository.findById(1L)).thenReturn(Optional.of(recurso));
        when(ofertaRepository.save(any(Oferta.class))).then(returnsFirstArg());

        mvc.perform(post("/api/ofertas").contentType(MediaType.APPLICATION_JSON).content("""
                {"cantidad": 10, "descripcion": "Agua", "tipo_recurso_id": "1",
                 "organizacion_id": "2", "convocatoria_id": "7"}
                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.convocatoria.id").value(7))
                .andExpect(jsonPath("$.organizacion.id").value(2))
                .andExpect(jsonPath("$.tipoRecurso.id").value(1));

        ArgumentCaptor<Oferta> captor = ArgumentCaptor.forClass(Oferta.class);
        verify(ofertaRepository).save(captor.capture());
        Oferta guardada = captor.getValue();
        assertThat(guardada.getConvocatoria()).isSameAs(convocatoria);
        assertThat(guardada.getOrganizacion()).isSameAs(organizacion);
        assertThat(guardada.getTipoRecurso()).isSameAs(recurso);
        assertThat(guardada.getFechaCreacion()).isEqualTo(LocalDate.now());
        assertThat(guardada.getCantidad()).isEqualTo(10);
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
        mvc.perform(post("/api/ofertas").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").isNotEmpty());
        verifyNoInteractions(ofertaRepository, convocatoriaRepository,
                organizacionRepository, tipoRecursoRepository);
    }

    @Test
    void crear_conConvocatoriaInexistente_noGuardaLaOferta() throws Exception {
        when(convocatoriaRepository.findById(7L)).thenReturn(Optional.empty());
        mvc.perform(post("/api/ofertas").contentType(MediaType.APPLICATION_JSON).content("""
                {"cantidad":10,"descripcion":"Agua","tipoRecursoId":1,
                 "organizacionId":2,"convocatoriaId":7}
                """))
                .andExpect(status().isNotFound());
        verify(ofertaRepository, never()).save(any());
    }
}
