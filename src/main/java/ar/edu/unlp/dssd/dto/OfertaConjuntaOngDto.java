package ar.edu.unlp.dssd.dto;

import java.time.LocalDateTime;
import java.util.List;

/** [Entrega final] Oferta conjunta vista por una ONG asociada; participantes incluye a la dueña */
public record OfertaConjuntaOngDto(Long id, Long convocatoriaId, Long emergenciaId, String tipoDesastre,
                                   LocalDateTime fechaCierre, boolean ventanaAbierta,
                                   OrganizacionReferenciaDto organizacionDuena, boolean esDuena,
                                   List<ParticipanteConjuntaDto> participantes, boolean valida,
                                   List<String> faltan, List<OfertaOngDto> lotes) {
}
