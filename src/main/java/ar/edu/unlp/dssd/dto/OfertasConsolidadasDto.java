package ar.edu.unlp.dssd.dto;

import java.time.LocalDateTime;
import java.util.List;

/** [Entrega final] Listado consolidado de la convocatoria más reciente de una emergencia (lo consulta Bonita). */
public record OfertasConsolidadasDto(Long emergenciaId, Long convocatoriaId, LocalDateTime fechaCierre,
                                     boolean coberturaCompleta, List<LoteCoberturaDto> lotes,
                                     List<OfertaConsolidadaDto> ofertas) {
}
