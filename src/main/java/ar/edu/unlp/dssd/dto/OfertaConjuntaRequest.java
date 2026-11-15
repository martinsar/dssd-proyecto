package ar.edu.unlp.dssd.dto;

import java.util.List;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/** [Entrega final] Alta de una oferta conjunta: convocatoria y ONGs participantes (sin la dueña) */
public record OfertaConjuntaRequest(
        @NotNull(message = "La convocatoria es obligatoria")
        @Positive(message = "El id de la convocatoria debe ser mayor que cero")
        Long convocatoriaId,

        List<Long> participantesIds) {
}
