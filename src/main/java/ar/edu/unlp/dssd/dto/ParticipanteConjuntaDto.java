package ar.edu.unlp.dssd.dto;

/** [Entrega final] Organización asociada a una oferta conjunta y cuántos lotes no retirados cargó */
public record ParticipanteConjuntaDto(Long organizacionId, String codigoOng, String nombre, long lotesCargados) {
}
