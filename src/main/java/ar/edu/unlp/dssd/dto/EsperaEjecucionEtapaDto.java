package ar.edu.unlp.dssd.dto;

/** [Entrega final] Desglose de tiempo de espera (cola) vs. ejecución efectiva por etapa en Bonita (fuente: BONITA_API) */
public record EsperaEjecucionEtapaDto(String etapa, double esperaPromedioHoras, double ejecucionPromedioHoras,
                                      double porcentajeEspera, int casos, String fuente) {
}
