package ar.edu.unlp.dssd.dto;

import ar.edu.unlp.dssd.model.EtapaEmergencia;

/** [Entrega final] Resultado de la selección del municipio y etapa en la que queda la emergencia */
public record AdjudicacionResponse(int seleccionadas, int noSeleccionadas, EtapaEmergencia etapa) {
}
