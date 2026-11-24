package ar.edu.unlp.dssd.dto;

import java.util.List;

/** [Entrega final] Bloque 4 de Bonita: carga operativa y tiempos promedio por usuario/actor de Bonita */
public record ActividadUsuariosBonitaDto(boolean disponible, String fuente, String mensaje, int totalTareasCompletadas,
                                         List<ActividadUsuarioDto> usuarios) {
}
