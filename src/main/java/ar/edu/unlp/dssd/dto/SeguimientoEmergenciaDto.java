package ar.edu.unlp.dssd.dto;

import java.util.List;

/** [Entrega final] Detalle de una emergencia para el coordinador con todos sus lotes */
public record SeguimientoEmergenciaDto(EmergenciaDetalleDto emergencia, List<OfertaSeguimientoDto> ofertas) {
}
