package ar.edu.unlp.dssd.dto;

/** [Entrega final] Resumen de una versión del proceso desplegada en Bonita (fuente: BONITA_API) */
public record ProcesoBonitaResumenDto(String id, String nombre, String version, String estadoActivacion,
                                      String fuente) {
}
