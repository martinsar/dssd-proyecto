package ar.edu.unlp.dssd.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import ar.edu.unlp.dssd.dto.OfertaRequest;
import ar.edu.unlp.dssd.model.Necesidad;
import ar.edu.unlp.dssd.model.Oferta;
import ar.edu.unlp.dssd.model.Organizacion;
import ar.edu.unlp.dssd.model.Proyecto;
import ar.edu.unlp.dssd.model.TipoEmergencia;
import ar.edu.unlp.dssd.model.TipoOrganizacion;
import ar.edu.unlp.dssd.model.TipoRecurso;
import ar.edu.unlp.dssd.model.Usuario;
import ar.edu.unlp.dssd.service.NecesidadService;
import ar.edu.unlp.dssd.service.OfertaService;
import ar.edu.unlp.dssd.service.OrganizacionService;
import ar.edu.unlp.dssd.service.ProyectoService;
import ar.edu.unlp.dssd.service.TipoEmergenciaService;
import ar.edu.unlp.dssd.service.TipoOrganizacionService;
import ar.edu.unlp.dssd.service.TipoRecursoService;
import ar.edu.unlp.dssd.service.UsuarioService;

@ExtendWith(MockitoExtension.class)
class RestCreationStatusTest {

    @Mock
    private NecesidadService necesidadService;
    @Mock
    private OfertaService ofertaService;
    @Mock
    private OrganizacionService organizacionService;
    @Mock
    private ProyectoService proyectoService;
    @Mock
    private TipoEmergenciaService tipoEmergenciaService;
    @Mock
    private TipoOrganizacionService tipoOrganizacionService;
    @Mock
    private TipoRecursoService tipoRecursoService;
    @Mock
    private UsuarioService usuarioService;

    @InjectMocks
    private NecesidadController necesidadController;
    @InjectMocks
    private OfertaController ofertaController;
    @InjectMocks
    private OrganizacionController organizacionController;
    @InjectMocks
    private ProyectoController proyectoController;
    @InjectMocks
    private TipoEmergenciaController tipoEmergenciaController;
    @InjectMocks
    private TipoOrganizacionController tipoOrganizacionController;
    @InjectMocks
    private TipoRecursoController tipoRecursoController;
    @InjectMocks
    private UsuarioController usuarioController;

    @Test
    void crearNecesidad_devuelveCreatedYConservaElResultadoDelServicio() {
        Necesidad recurso = new Necesidad();
        when(necesidadService.guardar(recurso)).thenReturn(recurso);

        assertCreated(necesidadController.crear(recurso), recurso);

        verify(necesidadService).guardar(recurso);
    }

    @Test
    void crearOferta_devuelveCreatedYConservaElResultadoDelServicio() {
        OfertaRequest request = new OfertaRequest(1, "Agua", 1L, 1L, 1L);
        Oferta recurso = new Oferta();
        when(ofertaService.crear(request)).thenReturn(recurso);

        assertCreated(ofertaController.crear(request), recurso);

        verify(ofertaService).crear(request);
    }

    @Test
    void crearOrganizacion_devuelveCreatedYConservaElResultadoDelServicio() {
        Organizacion recurso = new Organizacion();
        when(organizacionService.guardar(recurso)).thenReturn(recurso);

        assertCreated(organizacionController.crear(recurso), recurso);

        verify(organizacionService).guardar(recurso);
    }

    @Test
    void crearProyecto_devuelveCreatedYConservaElResultadoDelServicio() {
        Proyecto recurso = new Proyecto();
        when(proyectoService.guardar(recurso)).thenReturn(recurso);

        assertCreated(proyectoController.crear(recurso), recurso);

        verify(proyectoService).guardar(recurso);
    }

    @Test
    void crearTipoEmergencia_devuelveCreatedYConservaElResultadoDelServicio() {
        TipoEmergencia recurso = new TipoEmergencia();
        when(tipoEmergenciaService.guardar(recurso)).thenReturn(recurso);

        assertCreated(tipoEmergenciaController.crear(recurso), recurso);

        verify(tipoEmergenciaService).guardar(recurso);
    }

    @Test
    void crearTipoOrganizacion_devuelveCreatedYConservaElResultadoDelServicio() {
        TipoOrganizacion recurso = new TipoOrganizacion();
        when(tipoOrganizacionService.guardar(recurso)).thenReturn(recurso);

        assertCreated(tipoOrganizacionController.crear(recurso), recurso);

        verify(tipoOrganizacionService).guardar(recurso);
    }

    @Test
    void crearTipoRecurso_devuelveCreatedYConservaElResultadoDelServicio() {
        TipoRecurso recurso = new TipoRecurso();
        when(tipoRecursoService.guardar(recurso)).thenReturn(recurso);

        assertCreated(tipoRecursoController.crearTipoRecurso(recurso), recurso);

        verify(tipoRecursoService).guardar(recurso);
    }

    @Test
    void crearUsuario_devuelveCreatedYConservaElResultadoDelServicio() {
        Usuario recurso = new Usuario();
        when(usuarioService.guardarUsuario(recurso)).thenReturn(recurso);

        assertCreated(usuarioController.crearUsuario(recurso), recurso);

        verify(usuarioService).guardarUsuario(recurso);
    }

    private <T> void assertCreated(ResponseEntity<T> respuesta, T recurso) {
        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(respuesta.getBody()).isSameAs(recurso);
    }
}
