package ar.edu.unlp.dssd.seguridad;

import java.util.Collection;
import java.util.Optional;

/** [Entrega final] Perfiles de la app y su rol equivalente en Bonita */
public enum Rol {
    OPERADOR_MUNICIPAL("Municipio"),
    COORDINADOR("Coordinador"),
    ONG("Organizaciones"),
    AUDITOR("Auditor");

    private final String rolBonita;

    Rol(String rolBonita) {
        this.rolBonita = rolBonita;
    }

    public String rolBonita() {
        return rolBonita;
    }

    /** Se toma el primer rol de la app (orden de declaración) presente en los roles de Bonita */
    public static Optional<Rol> desdeRolesBonita(Collection<String> nombresRolBonita) {
        if (nombresRolBonita == null || nombresRolBonita.isEmpty()) {
            return Optional.empty();
        }
        for (Rol rol : values()) {
            if (nombresRolBonita.contains(rol.rolBonita)) {
                return Optional.of(rol);
            }
        }
        return Optional.empty();
    }

    /** Se mapea el valor de la columna local usuario.rol (nombre del enum) */
    public static Optional<Rol> desdeNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            return Optional.empty();
        }
        for (Rol rol : values()) {
            if (rol.name().equalsIgnoreCase(nombre.trim())) {
                return Optional.of(rol);
            }
        }
        return Optional.empty();
    }
}
