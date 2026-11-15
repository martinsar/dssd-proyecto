package ar.edu.unlp.dssd.service;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ar.edu.unlp.dssd.dto.LoginRequest;
import ar.edu.unlp.dssd.dto.LoginResponse;
import ar.edu.unlp.dssd.dto.SesionActualDto;
import ar.edu.unlp.dssd.dto.UsuarioSesionDto;
import ar.edu.unlp.dssd.exception.AccesoDenegadoException;
import ar.edu.unlp.dssd.model.Usuario;
import ar.edu.unlp.dssd.repository.UsuarioRepository;
import ar.edu.unlp.dssd.seguridad.AlmacenSesiones;
import ar.edu.unlp.dssd.seguridad.Rol;
import ar.edu.unlp.dssd.seguridad.SesionUsuario;

/** [Entrega final] Ingreso con el usuario de Bonita y perfil según su rol */
@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    @Autowired
    private BonitaService bonitaService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private AlmacenSesiones almacen;

    /** Se autentica en Bonita, se vincula el usuario local, se resuelve el perfil y se abre la sesión */
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        SesionBonita sesionBonita = bonitaService.iniciarSesion(request.username(), request.password());
        List<String> rolesBonita = bonitaService.obtenerRolesUsuario(sesionBonita);

        Optional<Usuario> local = usuarioRepository.findByUsuarioBonita(sesionBonita.username());
        if (local.isEmpty()) {
            bonitaService.cerrarSesion(sesionBonita);
            throw new AccesoDenegadoException("Tu usuario no está habilitado en RescueSync.");
        }
        Usuario usuario = local.get();

        Optional<Rol> rol = Rol.desdeRolesBonita(rolesBonita);
        if (rol.isEmpty()) {
            rol = Rol.desdeNombre(usuario.getRol());
        }
        if (rol.isEmpty()) {
            bonitaService.cerrarSesion(sesionBonita);
            throw new AccesoDenegadoException("Tu usuario no tiene un perfil habilitado en RescueSync.");
        }
        if (rol.get() == Rol.ONG && usuario.getOrganizacion() == null) {
            bonitaService.cerrarSesion(sesionBonita);
            throw new AccesoDenegadoException("Tu usuario no está asociado a ninguna organización.");
        }

        SesionUsuario sesion = almacen.crear(usuario, rol.get(), sesionBonita);
        return new LoginResponse(sesion.token(), almacen.expiraEn(sesion), UsuarioSesionDto.desde(sesion));
    }

    public SesionActualDto sesionActual(SesionUsuario sesion) {
        return new SesionActualDto(almacen.expiraEn(sesion), UsuarioSesionDto.desde(sesion));
    }

    /** Se elimina la sesión y se cierra la de Bonita sin propagar errores */
    public void logout(SesionUsuario sesion) {
        almacen.eliminar(sesion.token());
        try {
            bonitaService.cerrarSesion(sesion.bonita());
        } catch (RuntimeException e) {
            log.debug("No se pudo cerrar la sesión de Bonita: {}", e.getMessage());
        }
    }
}
