package ar.edu.unlp.dssd.dto;

/** [Entrega final] Carga operativa por usuario/actor de Bonita en las tareas completadas del período */
public record ActividadUsuarioDto(String usuario, int tareasCompletadas, double promedioHoras, String fuente) {
}
