package ar.edu.unlp.dssd.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import ar.edu.unlp.dssd.dto.LoginRequest;
import ar.edu.unlp.dssd.dto.LoginResponse;
import ar.edu.unlp.dssd.exception.AccesoDenegadoException;
import ar.edu.unlp.dssd.exception.BonitaException;
import ar.edu.unlp.dssd.exception.NoAutenticadoException;
import ar.edu.unlp.dssd.model.Organizacion;
import ar.edu.unlp.dssd.model.Usuario;
import ar.edu.unlp.dssd.repository.UsuarioRepository;
import ar.edu.unlp.dssd.seguridad.AlmacenSesiones;
import ar.edu.unlp.dssd.seguridad.Rol;
import ar.edu.unlp.dssd.seguridad.SesionUsuario;

/** [Entrega final] Tests del login contra Bonita y la resolución del perfil. */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private BonitaService bonitaService;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Spy
    private AlmacenSesiones almacen = new AlmacenSesiones(30, 8);

    @InjectMocks
    private AuthService authService;

    private final SesionBonita sesionBonita = new SesionBonita("7", "caritas", "js", "tok");

    private Usuario usuario(String rolLocal, Organizacion organizacion) {
        Usuario u = new Usuario();
        u.setId(3L);
        u.setNombre("Rosa");
        u.setApellido("Representante");
        u.setUsuarioBonita("caritas");
        u.setRol(rolLocal);
        u.setOrganizacion(organizacion);
        return u;
    }

    private Organizacion caritas() {
        Organizacion o = new Organizacion();
        o.setId(1L);
        o.setNombre("Cáritas");
        o.setCodigoOng("ONG-CARITAS");
        return o;
    }

    @Test
    void login_conRolDeBonita_abreLaSesionConLaOrganizacion() {
        when(bonitaService.iniciarSesion("caritas", "bpm")).thenReturn(sesionBonita);
        when(bonitaService.obtenerRolesUsuario(sesionBonita)).thenReturn(List.of("Organizaciones"));
        when(usuarioRepository.findByUsuarioBonita("caritas")).thenReturn(Optional.of(usuario(null, caritas())));

        LoginResponse respuesta = authService.login(new LoginRequest("caritas", "bpm"));

        assertThat(respuesta.token()).isNotBlank();
        assertThat(respuesta.expiraEn()).isNotNull();
        assertThat(respuesta.usuario().rol()).isEqualTo(Rol.ONG);
        assertThat(respuesta.usuario().username()).isEqualTo("caritas");
        assertThat(respuesta.usuario().organizacionId()).isEqualTo(1L);
        assertThat(respuesta.usuario().organizacionNombre()).isEqualTo("Cáritas");
        assertThat(almacen.obtenerVigente(respuesta.token())).isPresent();
    }

    @Test
    void login_siBonitaNoDevuelveRoles_usaElRolLocal() {
        when(bonitaService.iniciarSesion("caritas", "bpm")).thenReturn(sesionBonita);
        when(bonitaService.obtenerRolesUsuario(sesionBonita)).thenReturn(List.of());
        when(usuarioRepository.findByUsuarioBonita("caritas"))
                .thenReturn(Optional.of(usuario("COORDINADOR", null)));

        LoginResponse respuesta = authService.login(new LoginRequest("caritas", "bpm"));

        assertThat(respuesta.usuario().rol()).isEqualTo(Rol.COORDINADOR);
        assertThat(respuesta.usuario().organizacionId()).isNull();
    }

    @Test
    void login_conCredencialesInvalidas_propagaElNoAutenticado() {
        when(bonitaService.iniciarSesion("caritas", "mal"))
                .thenThrow(new NoAutenticadoException("Usuario o contraseña incorrectos."));

        assertThatThrownBy(() -> authService.login(new LoginRequest("caritas", "mal")))
                .isInstanceOf(NoAutenticadoException.class);
        verifyNoInteractions(usuarioRepository);
    }

    @Test
    void login_siBonitaNoResponde_propagaLaBonitaException() {
        when(bonitaService.iniciarSesion("caritas", "bpm")).thenThrow(new BonitaException("Bonita caído"));

        assertThatThrownBy(() -> authService.login(new LoginRequest("caritas", "bpm")))
                .isInstanceOf(BonitaException.class);
    }

    @Test
    void login_sinUsuarioLocal_responde403YCierraLaSesionDeBonita() {
        when(bonitaService.iniciarSesion("caritas", "bpm")).thenReturn(sesionBonita);
        when(bonitaService.obtenerRolesUsuario(sesionBonita)).thenReturn(List.of("Organizaciones"));
        when(usuarioRepository.findByUsuarioBonita("caritas")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(new LoginRequest("caritas", "bpm")))
                .isInstanceOf(AccesoDenegadoException.class);
        verify(bonitaService).cerrarSesion(sesionBonita);
        verify(almacen, never()).crear(any(), any(), any());
    }

    @Test
    void login_ongSinOrganizacion_responde403() {
        when(bonitaService.iniciarSesion("caritas", "bpm")).thenReturn(sesionBonita);
        when(bonitaService.obtenerRolesUsuario(sesionBonita)).thenReturn(List.of("Organizaciones"));
        when(usuarioRepository.findByUsuarioBonita("caritas")).thenReturn(Optional.of(usuario(null, null)));

        assertThatThrownBy(() -> authService.login(new LoginRequest("caritas", "bpm")))
                .isInstanceOf(AccesoDenegadoException.class)
                .hasMessageContaining("organización");
        verify(bonitaService).cerrarSesion(sesionBonita);
    }

    @Test
    void login_sinPerfilEnBonitaNiLocal_responde403() {
        when(bonitaService.iniciarSesion("caritas", "bpm")).thenReturn(sesionBonita);
        when(bonitaService.obtenerRolesUsuario(sesionBonita)).thenReturn(List.of("Otro"));
        when(usuarioRepository.findByUsuarioBonita("caritas")).thenReturn(Optional.of(usuario("municipio", null)));

        assertThatThrownBy(() -> authService.login(new LoginRequest("caritas", "bpm")))
                .isInstanceOf(AccesoDenegadoException.class);
    }

    @Test
    void logout_eliminaLaSesionAunqueFalleElCierreEnBonita() {
        SesionUsuario sesion = almacen.crear(usuario("ONG", caritas()), Rol.ONG, sesionBonita);
        doThrow(new BonitaException("Bonita caído")).when(bonitaService).cerrarSesion(sesionBonita);

        authService.logout(sesion);

        assertThat(almacen.obtenerVigente(sesion.token())).isEmpty();
    }

    @Test
    void sesionActual_devuelveElUsuarioYElVencimiento() {
        SesionUsuario sesion = almacen.crear(usuario("ONG", caritas()), Rol.ONG, sesionBonita);

        var actual = authService.sesionActual(sesion);

        assertThat(actual.usuario().id()).isEqualTo(3L);
        assertThat(actual.expiraEn()).isNotNull();
    }
}
