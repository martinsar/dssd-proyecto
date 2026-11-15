package ar.edu.unlp.dssd.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import ar.edu.unlp.dssd.model.Convocatoria;
import ar.edu.unlp.dssd.model.Emergencia;
import ar.edu.unlp.dssd.model.EstadoOferta;
import ar.edu.unlp.dssd.model.EtapaEmergencia;
import ar.edu.unlp.dssd.service.EtapaEmergenciaService.EstadoEmergencia;

/** [Entrega final] Fila del tablero de monitoreo del coordinador (cuenta solo lotes válidos no retirados) */
public record MonitoreoFilaDto(Long emergenciaId, String tipoDesastre, String nivelGravedad, String zonaAfectada,
                               LocalDate fechaCreacion, EtapaEmergencia etapa, Long convocatoriaId,
                               LocalDateTime fechaCierre, Integer ronda, int lotesTotales, int lotesCubiertos,
                               int ofertasActivas, int ofertasAdjudicadas, int ofertasFinalizadas,
                               int progresoEjecucion, boolean requiereDecision) {

    public static MonitoreoFilaDto desde(Emergencia e, EstadoEmergencia estado) {
        Convocatoria c = estado.convocatoria();
        int lotesCubiertos = (int) estado.cobertura().stream()
                .filter(l -> l.cantidadOfertada() != null && l.cantidadRequerida() != null
                        && l.cantidadOfertada() >= l.cantidadRequerida())
                .count();
        int finalizadas = (int) estado.validas().stream()
                .filter(o -> o.getEstado() == EstadoOferta.FINALIZADA).count();
        int adjudicadas = (int) estado.validas().stream()
                .filter(o -> o.getEstado() == EstadoOferta.ADJUDICADA).count() + finalizadas;
        int progreso = adjudicadas == 0 ? 0 : (int) Math.round(100.0 * finalizadas / adjudicadas);
        return new MonitoreoFilaDto(e.getId(), e.getTipoDesastre(), e.getNivelGravedad(), e.getZonaAfectada(),
                e.getFechaCreacion(), estado.etapa(),
                c == null ? null : c.getId(),
                c == null ? null : c.getFechaCierre(),
                c == null ? null : (c.getRonda() == null ? 1 : c.getRonda()),
                estado.cobertura().size(), lotesCubiertos, estado.validas().size(), adjudicadas, finalizadas,
                progreso, estado.etapa() == EtapaEmergencia.REQUIERE_DECISION);
    }
}
