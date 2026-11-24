package ar.edu.unlp.dssd.dto;

/** [Entrega final] Cuello de botella actual: tareas humanas pendientes en Bonita por etapa */
public record TareaPendienteEtapaDto(String etapa, int cantidadPendientes, double esperaPromedioHoras,
                                     double esperaMaximaHoras, String fuente) {
}
