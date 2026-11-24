package ar.edu.unlp.dssd.dto;

/** [Entrega final] Tiempo promedio, mínimo y máximo de una etapa del proceso (fuente: BONITA_API) */
public record TiempoEtapaDto(String etapa, double promedioHoras, double minHoras, double maxHoras, int casos,
                             String fuente) {
}
