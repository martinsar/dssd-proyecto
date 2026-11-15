package ar.edu.unlp.dssd.dto;

import java.time.LocalDate;
import java.util.List;

import ar.edu.unlp.dssd.model.Emergencia;
import ar.edu.unlp.dssd.model.EtapaEmergencia;
import ar.edu.unlp.dssd.service.EtapaEmergenciaService.EstadoEmergencia;

/** [Entrega final] Detalle de la emergencia con etapa, convocatoria, lotes y proyecto */
public record EmergenciaDetalleDto(Long id, String tipoDesastre, String nivelGravedad, String zonaAfectada,
                                   String descripcion, LocalDate fechaCreacion, EtapaEmergencia etapa,
                                   boolean puedeSeleccionar, boolean puedeDecidir,
                                   ConvocatoriaResumenDto convocatoria, List<LoteDetalleDto> lotes,
                                   ProyectoResumenDto proyecto) {

    /** Sin incluirOfertada los lotes llevan cantidadOfertada en null (vista del municipio) */
    public static EmergenciaDetalleDto desde(Emergencia e, EstadoEmergencia estado, boolean incluirOfertada) {
        List<LoteDetalleDto> lotes = estado.cobertura().stream()
                .map(lote -> incluirOfertada ? lote : lote.sinOfertado())
                .toList();
        return new EmergenciaDetalleDto(e.getId(), e.getTipoDesastre(), e.getNivelGravedad(), e.getZonaAfectada(),
                e.getDescripcion(), e.getFechaCreacion(), estado.etapa(),
                estado.etapa() == EtapaEmergencia.EN_SELECCION,
                estado.etapa() == EtapaEmergencia.REQUIERE_DECISION,
                ConvocatoriaResumenDto.desde(estado.convocatoria()), lotes,
                ProyectoResumenDto.desde(e.getProyecto()));
    }
}
