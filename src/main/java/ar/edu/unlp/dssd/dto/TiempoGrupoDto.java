package ar.edu.unlp.dssd.dto;

/** [Entrega final] Tiempo promedio de resolución de emergencias agrupado por gravedad o tipo de desastre (cruce DSSD + Bonita) */
public record TiempoGrupoDto(String etiqueta, double promedioHoras, double minHoras, double maxHoras, int casos,
                             String fuente) {
}
