package ar.edu.unlp.dssd.dto;

import java.util.List;

/** Body del relevamiento: la convocatoria se crea junto con sus lotes (necesidades). Sin fechas. */
public record ConvocatoriaRequest(List<NecesidadRequest> necesidades) {
}
