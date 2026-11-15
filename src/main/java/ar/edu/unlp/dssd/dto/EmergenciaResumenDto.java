package ar.edu.unlp.dssd.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import ar.edu.unlp.dssd.model.Emergencia;
import ar.edu.unlp.dssd.model.EtapaEmergencia;
import ar.edu.unlp.dssd.service.EtapaEmergenciaService;
import ar.edu.unlp.dssd.service.EtapaEmergenciaService.EstadoEmergencia;

/** [Entrega final] Fila de "Mis emergencias" del municipio */
public record EmergenciaResumenDto(Long id, String tipoDesastre, String nivelGravedad, String zonaAfectada,
                                   String descripcion, LocalDate fechaCreacion, EtapaEmergencia etapa,
                                   LocalDateTime fechaCierre, int ofertasValidadas) {

    public static EmergenciaResumenDto desde(Emergencia e, EstadoEmergencia estado) {
        int validadas = (int) estado.validas().stream()
                .filter(o -> EtapaEmergenciaService.ESTADOS_VALIDADOS.contains(o.getEstado()))
                .count();
        return new EmergenciaResumenDto(e.getId(), e.getTipoDesastre(), e.getNivelGravedad(), e.getZonaAfectada(),
                e.getDescripcion(), e.getFechaCreacion(), estado.etapa(),
                estado.convocatoria() == null ? null : estado.convocatoria().getFechaCierre(), validadas);
    }
}
