package ar.edu.unlp.dssd.dto;

import java.time.LocalDateTime;
import java.util.List;

import ar.edu.unlp.dssd.model.AccionConvocatoria;
import jakarta.validation.constraints.NotNull;

/** [Entrega final] Decisión del coordinador ante cobertura insuficiente */
public record DecisionConvocatoriaRequest(
        @NotNull(message = "Elegí una acción")
        AccionConvocatoria accion,

        LocalDateTime fechaCierre,

        List<NecesidadRequest> necesidades) {
}
