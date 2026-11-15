package ar.edu.unlp.dssd.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/** [Entrega final] Indicadores del tablero del auditor */
public record IndicadoresDto(LocalDateTime generadoEn, LocalDate desde, LocalDate hasta, TotalesDto totales,
                             List<CantidadPorEtiquetaDto> suministrosPorTipo,
                             List<CantidadPorEtiquetaDto> emergenciasPorEtapa,
                             List<CantidadPorEtiquetaDto> emergenciasPorGravedad,
                             List<CantidadPorEtiquetaDto> ofertasPorEstado,
                             OfertasConjuntasIndicadorDto ofertasConjuntas, TiemposEtapasDto tiemposPorEtapa) {
}
