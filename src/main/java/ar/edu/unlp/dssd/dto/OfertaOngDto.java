package ar.edu.unlp.dssd.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import ar.edu.unlp.dssd.model.Convocatoria;
import ar.edu.unlp.dssd.model.Emergencia;
import ar.edu.unlp.dssd.model.EstadoOferta;
import ar.edu.unlp.dssd.model.Oferta;
import ar.edu.unlp.dssd.model.TipoRecurso;

/** [Entrega final] Lote de una ONG con lo que puede hacer sobre él */
public record OfertaOngDto(Long id, Long convocatoriaId, Long emergenciaId, String tipoDesastre, String zonaAfectada,
                           LocalDateTime fechaCierre, boolean ventanaAbierta, Long tipoRecursoId,
                           String codigoTipoRecurso, String tipoRecurso, Integer cantidad, String descripcion,
                           EstadoOferta estado, String organizacion, Long ofertaConjuntaId, Integer numeroVersion,
                           LocalDate fechaCreacion, LocalDateTime fechaModificacion,
                           LocalDateTime fechaFinalizacion, String informeFinalizacion,
                           boolean fueraDeEspecialidad, boolean conjuntaValida, boolean puedeEditar,
                           boolean puedeFinalizar) {

    /** propia: el lote es de la organización de la sesión (solo así se puede editar o finalizar) */
    public static OfertaOngDto desde(Oferta o, boolean ventanaAbierta, boolean conjuntaValida,
                                     boolean fueraDeEspecialidad, boolean propia) {
        Convocatoria c = o.getConvocatoria();
        Emergencia e = c == null ? null : c.getEmergencia();
        TipoRecurso t = o.getTipoRecurso();
        return new OfertaOngDto(o.getId(),
                c == null ? null : c.getId(),
                e == null ? null : e.getId(),
                e == null ? null : e.getTipoDesastre(),
                e == null ? null : e.getZonaAfectada(),
                c == null ? null : c.getFechaCierre(),
                ventanaAbierta,
                t == null ? null : t.getId(),
                t == null ? null : t.getCodigo(),
                t == null ? null : t.getNombre(),
                o.getCantidad(), o.getDescripcion(), o.getEstado(),
                o.getOrganizacion() == null ? null : o.getOrganizacion().getNombre(),
                o.getOfertaConjunta() == null ? null : o.getOfertaConjunta().getId(),
                o.getNumeroVersion() == null ? 1 : o.getNumeroVersion(),
                o.getFechaCreacion(), o.getFechaModificacion(), o.getFechaFinalizacion(),
                o.getInformeFinalizacion(), fueraDeEspecialidad, conjuntaValida,
                propia && ventanaAbierta && o.getEstado() == EstadoOferta.PENDIENTE,
                propia && o.getEstado() == EstadoOferta.ADJUDICADA);
    }
}
