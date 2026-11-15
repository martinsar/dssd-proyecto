package ar.edu.unlp.dssd.dto;

import ar.edu.unlp.dssd.model.Necesidad;
import ar.edu.unlp.dssd.model.TipoRecurso;

/** [Entrega final] Lote pedido en una convocatoria, tal como lo ve una ONG */
public record LoteConvocatoriaDto(Long necesidadId, Long tipoRecursoId, String codigoTipoRecurso, String tipoRecurso,
                                  String descripcion, Integer cantidadRequerida) {

    public static LoteConvocatoriaDto desde(Necesidad n) {
        TipoRecurso t = n.getTipoRecurso();
        return new LoteConvocatoriaDto(n.getId(), t == null ? null : t.getId(), t == null ? null : t.getCodigo(),
                t == null ? null : t.getNombre(), n.getDescripcion(), n.getCantidad());
    }
}
