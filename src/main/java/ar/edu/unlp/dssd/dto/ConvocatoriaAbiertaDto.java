package ar.edu.unlp.dssd.dto;

import java.time.LocalDateTime;
import java.util.List;

import ar.edu.unlp.dssd.model.Convocatoria;
import ar.edu.unlp.dssd.model.Emergencia;

/** [Entrega final] Convocatoria publicada vista por una ONG */
public record ConvocatoriaAbiertaDto(Long convocatoriaId, Long emergenciaId, String tipoDesastre,
                                     String nivelGravedad, String zonaAfectada, String descripcion,
                                     LocalDateTime fechaApertura, LocalDateTime fechaCierre, boolean abierta,
                                     List<LoteConvocatoriaDto> lotes) {

    public static ConvocatoriaAbiertaDto desde(Convocatoria c, boolean abierta, List<LoteConvocatoriaDto> lotes) {
        Emergencia e = c.getEmergencia();
        return new ConvocatoriaAbiertaDto(c.getId(),
                e == null ? null : e.getId(),
                e == null ? null : e.getTipoDesastre(),
                e == null ? null : e.getNivelGravedad(),
                e == null ? null : e.getZonaAfectada(),
                e == null ? null : e.getDescripcion(),
                c.getFechaApertura(), c.getFechaCierre(), abierta, lotes);
    }
}
