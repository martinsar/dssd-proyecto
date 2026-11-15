package ar.edu.unlp.dssd.dto;

import java.time.LocalDateTime;

import ar.edu.unlp.dssd.model.EstadoOferta;
import ar.edu.unlp.dssd.model.OfertaVersion;
import ar.edu.unlp.dssd.model.Usuario;

/** [Entrega final] Entrada del historial de un lote; usuario es null si el cambio lo hizo el sistema */
public record OfertaVersionDto(Integer numeroVersion, LocalDateTime fechaHora, String usuario, String motivo,
                               Long tipoRecursoId, String tipoRecurso, Integer cantidad, String descripcion,
                               EstadoOferta estado) {

    public static OfertaVersionDto desde(OfertaVersion v) {
        Usuario u = v.getUsuario();
        String usuario = u == null ? null
                : ((u.getNombre() == null ? "" : u.getNombre()) + " "
                        + (u.getApellido() == null ? "" : u.getApellido())).trim();
        return new OfertaVersionDto(v.getNumeroVersion(), v.getFechaHora(), usuario, v.getMotivo(),
                v.getTipoRecurso() == null ? null : v.getTipoRecurso().getId(),
                v.getTipoRecurso() == null ? null : v.getTipoRecurso().getNombre(),
                v.getCantidad(), v.getDescripcion(), v.getEstado());
    }
}
