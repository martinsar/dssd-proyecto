package ar.edu.unlp.dssd.dto;

import java.time.LocalDateTime;

import ar.edu.unlp.dssd.model.Convocatoria;
import ar.edu.unlp.dssd.model.EstadoConvocatoria;

/** [Entrega final] Resumen de la convocatoria vigente */
public record ConvocatoriaResumenDto(Long id, EstadoConvocatoria estado, LocalDateTime fechaApertura,
                                     LocalDateTime fechaCierre, Integer ronda, boolean coberturaParcialAceptada) {

    public static ConvocatoriaResumenDto desde(Convocatoria c) {
        if (c == null) {
            return null;
        }
        return new ConvocatoriaResumenDto(c.getId(), c.getEstado(), c.getFechaApertura(), c.getFechaCierre(),
                c.getRonda() == null ? 1 : c.getRonda(), Boolean.TRUE.equals(c.getCoberturaParcialAceptada()));
    }
}
