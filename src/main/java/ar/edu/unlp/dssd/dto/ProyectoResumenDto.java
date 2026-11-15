package ar.edu.unlp.dssd.dto;

import java.time.LocalDateTime;

import ar.edu.unlp.dssd.model.Proyecto;

/** [Entrega final] Proyecto de ejecución de la emergencia (EN_CURSO o TERMINADO) */
public record ProyectoResumenDto(Long id, String nombre, String estado, LocalDateTime fechaInicio,
                                 LocalDateTime fechaFin) {

    public static ProyectoResumenDto desde(Proyecto p) {
        if (p == null) {
            return null;
        }
        return new ProyectoResumenDto(p.getId(), p.getNombre(), p.getEstado(), p.getFechaInicio(), p.getFechaFin());
    }
}
