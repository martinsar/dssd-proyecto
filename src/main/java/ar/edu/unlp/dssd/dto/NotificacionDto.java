package ar.edu.unlp.dssd.dto;

import java.time.LocalDateTime;

import ar.edu.unlp.dssd.model.Notificacion;
import ar.edu.unlp.dssd.model.TipoNotificacion;

/** [Entrega final] Notificación de una organización */
public record NotificacionDto(Long id, TipoNotificacion tipo, String titulo, String mensaje, boolean leida,
                              LocalDateTime fechaHora, Long emergenciaId, Long convocatoriaId, Long ofertaId) {

    public static NotificacionDto desde(Notificacion n) {
        return new NotificacionDto(n.getId(), n.getTipo(), n.getTitulo(), n.getMensaje(), n.isLeida(),
                n.getFechaHora(),
                n.getEmergencia() == null ? null : n.getEmergencia().getId(),
                n.getConvocatoria() == null ? null : n.getConvocatoria().getId(),
                n.getOferta() == null ? null : n.getOferta().getId());
    }
}
