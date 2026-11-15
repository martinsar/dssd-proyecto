package ar.edu.unlp.dssd.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import ar.edu.unlp.dssd.model.EtapaEmergencia;

/** [Entrega final] Fila de emergencias del tablero del auditor */
public record AuditoriaEmergenciaDto(Long id, String tipoDesastre, String nivelGravedad, String zonaAfectada,
                                     LocalDate fechaCreacion, EtapaEmergencia etapa,
                                     LocalDateTime fechaCierreConvocatoria, long ofertas, long ofertasAdjudicadas,
                                     long unidadesMovilizadas, long ofertasEnConjunta, String proyectoEstado) {
}
