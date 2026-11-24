package ar.edu.unlp.dssd.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import ar.edu.unlp.dssd.model.EtapaEmergencia;

/** [Entrega final] Fila de emergencias del tablero del auditor (fuente: DSSD_DB) */
public record AuditoriaEmergenciaDto(String fuente, Long id, Long caseId, String tipoDesastre, String nivelGravedad,
                                     String zonaAfectada, LocalDate fechaCreacion, EtapaEmergencia etapa,
                                     LocalDateTime fechaCierreConvocatoria, long ofertas, long ofertasAdjudicadas,
                                     long unidadesMovilizadas, long ofertasEnConjunta, boolean resolucionConConsorcio,
                                     String proyectoEstado, LocalDateTime proyectoFechaInicio,
                                     LocalDateTime proyectoFechaFin, Double proyectoDuracionHoras) {
}
