package ar.edu.unlp.dssd.seguridad;

import java.time.Instant;

import ar.edu.unlp.dssd.service.SesionBonita;

/** [Entrega final] Sesión de la app: datos del usuario y su sesión de Bonita */
public final class SesionUsuario {

    private final String token;
    private final Long usuarioId;
    private final String username;
    private final String nombre;
    private final String apellido;
    private final Rol rol;
    private final Long organizacionId;
    private final String organizacionNombre;
    private final SesionBonita bonita;
    private final Instant creada;
    private volatile Instant ultimoAcceso;

    public SesionUsuario(String token, Long usuarioId, String username, String nombre, String apellido,
                         Rol rol, Long organizacionId, String organizacionNombre, SesionBonita bonita,
                         Instant creada) {
        this.token = token;
        this.usuarioId = usuarioId;
        this.username = username;
        this.nombre = nombre;
        this.apellido = apellido;
        this.rol = rol;
        this.organizacionId = organizacionId;
        this.organizacionNombre = organizacionNombre;
        this.bonita = bonita;
        this.creada = creada;
        this.ultimoAcceso = creada;
    }

    public String token() {
        return token;
    }

    public Long usuarioId() {
        return usuarioId;
    }

    public String username() {
        return username;
    }

    public String nombre() {
        return nombre;
    }

    public String apellido() {
        return apellido;
    }

    public Rol rol() {
        return rol;
    }

    public Long organizacionId() {
        return organizacionId;
    }

    public String organizacionNombre() {
        return organizacionNombre;
    }

    public SesionBonita bonita() {
        return bonita;
    }

    public Instant creada() {
        return creada;
    }

    public Instant ultimoAcceso() {
        return ultimoAcceso;
    }

    /** Se registra el último acceso */
    public void tocar() {
        this.ultimoAcceso = Instant.now();
    }

    public boolean esOng() {
        return rol == Rol.ONG;
    }
}
