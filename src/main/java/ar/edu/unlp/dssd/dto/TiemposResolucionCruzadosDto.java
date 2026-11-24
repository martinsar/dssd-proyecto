package ar.edu.unlp.dssd.dto;

import java.util.List;

/** [Entrega final] Bloque 3 combinado: tiempos promedio de resolución en Bonita cruzados por gravedad y tipo de desastre de DSSD */
public record TiemposResolucionCruzadosDto(boolean disponible, String fuente, String mensaje,
                                           List<TiempoGrupoDto> porGravedad, List<TiempoGrupoDto> porTipoDesastre) {
}
