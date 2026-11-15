package ar.edu.unlp.dssd.seguridad;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import ar.edu.unlp.dssd.model.Organizacion;
import ar.edu.unlp.dssd.model.Usuario;
import ar.edu.unlp.dssd.service.SesionBonita;

/** [Entrega final] Sesiones de la app en memoria, indexadas por token opaco */
@Component
public class AlmacenSesiones {

    private static final int BYTES_TOKEN = 32;

    private final SecureRandom random = new SecureRandom();
    private final Map<String, SesionUsuario> sesiones = new ConcurrentHashMap<>();
    private final Duration inactividad;
    private final Duration duracionMaxima;

    public AlmacenSesiones(@Value("${rescuesync.sesion.minutos-inactividad:30}") long minutosInactividad,
                           @Value("${rescuesync.sesion.horas-maximas:8}") long horasMaximas) {
        this.inactividad = Duration.ofMinutes(minutosInactividad);
        this.duracionMaxima = Duration.ofHours(horasMaximas);
    }

    /** Se purgan las vencidas, se genera el token y se guarda la sesión */
    public SesionUsuario crear(Usuario usuario, Rol rol, SesionBonita bonita) {
        purgarVencidas();

        Organizacion organizacion = rol == Rol.ONG ? usuario.getOrganizacion() : null;
        String username = bonita != null && bonita.username() != null ? bonita.username() : usuario.getUsuarioBonita();

        String token;
        do {
            token = generarToken();
        } while (sesiones.containsKey(token));

        SesionUsuario sesion = new SesionUsuario(token, usuario.getId(), username, usuario.getNombre(),
                usuario.getApellido(), rol,
                organizacion == null ? null : organizacion.getId(),
                organizacion == null ? null : organizacion.getNombre(),
                bonita, Instant.now());
        sesiones.put(token, sesion);
        return sesion;
    }

    /** Se devuelve la sesión si sigue vigente y se registra el acceso */
    public Optional<SesionUsuario> obtenerVigente(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }
        SesionUsuario sesion = sesiones.get(token);
        if (sesion == null) {
            return Optional.empty();
        }
        if (vencida(sesion, Instant.now())) {
            sesiones.remove(token);
            return Optional.empty();
        }
        sesion.tocar();
        return Optional.of(sesion);
    }

    public Optional<SesionUsuario> eliminar(String token) {
        if (token == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(sesiones.remove(token));
    }

    /** Se calcula el vencimiento: el menor entre inactividad y duración máxima */
    public LocalDateTime expiraEn(SesionUsuario sesion) {
        Instant limite = limite(sesion);
        return LocalDateTime.ofInstant(limite, ZoneId.systemDefault()).withNano(0);
    }

    private Instant limite(SesionUsuario sesion) {
        Instant porInactividad = sesion.ultimoAcceso().plus(inactividad);
        Instant porMaximo = sesion.creada().plus(duracionMaxima);
        return porInactividad.isBefore(porMaximo) ? porInactividad : porMaximo;
    }

    private boolean vencida(SesionUsuario sesion, Instant ahora) {
        return !ahora.isBefore(limite(sesion));
    }

    private void purgarVencidas() {
        Instant ahora = Instant.now();
        sesiones.values().removeIf(sesion -> vencida(sesion, ahora));
    }

    private String generarToken() {
        byte[] bytes = new byte[BYTES_TOKEN];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
