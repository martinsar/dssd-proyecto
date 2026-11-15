package ar.edu.unlp.dssd.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ar.edu.unlp.dssd.dto.AuditoriaEmergenciaDto;
import ar.edu.unlp.dssd.dto.CantidadPorEtiquetaDto;
import ar.edu.unlp.dssd.dto.IndicadoresDto;
import ar.edu.unlp.dssd.dto.OfertasConjuntasIndicadorDto;
import ar.edu.unlp.dssd.dto.TiemposEtapasDto;
import ar.edu.unlp.dssd.dto.TotalesDto;
import ar.edu.unlp.dssd.model.Emergencia;
import ar.edu.unlp.dssd.model.EstadoConvocatoria;
import ar.edu.unlp.dssd.model.EstadoOferta;
import ar.edu.unlp.dssd.model.EtapaEmergencia;
import ar.edu.unlp.dssd.model.Oferta;
import ar.edu.unlp.dssd.model.Proyecto;
import ar.edu.unlp.dssd.repository.EmergenciaRepository;
import ar.edu.unlp.dssd.service.EtapaEmergenciaService.EstadoEmergencia;

/** [Entrega final] Indicadores del auditor con datos locales, filtrados por fecha de creación de la emergencia */
@Service
public class IndicadoresService {

    private static final Set<EstadoOferta> ESTADOS_MOVILIZADOS = EnumSet.of(EstadoOferta.ADJUDICADA,
            EstadoOferta.FINALIZADA);
    private static final LocalDate DESDE_MINIMO = LocalDate.of(1900, 1, 1);
    private static final LocalDate HASTA_MAXIMO = LocalDate.of(9999, 12, 31);

    @Autowired
    private EmergenciaRepository emergenciaRepository;

    @Autowired
    private EtapaEmergenciaService etapaService;

    @Transactional(readOnly = true)
    public IndicadoresDto indicadores(LocalDate desde, LocalDate hasta) {
        List<Evaluada> evaluadas = evaluar(desde, hasta);

        long convocatoriasPublicadas = 0;
        long proyectosEnCurso = 0;
        long proyectosFinalizados = 0;
        Map<EtapaEmergencia, Long> porEtapa = new EnumMap<>(EtapaEmergencia.class);
        Map<String, Long> porGravedad = new LinkedHashMap<>();
        Map<EstadoOferta, Long> porEstado = new EnumMap<>(EstadoOferta.class);
        Map<String, Long> suministrosPorTipo = new LinkedHashMap<>();

        long ofertasRecibidas = 0;
        long ofertasAdjudicadas = 0;
        long adjudicadasEnConjunta = 0;
        long unidadesAdjudicadas = 0;
        long unidadesEnConjunta = 0;

        for (Evaluada evaluada : evaluadas) {
            Emergencia emergencia = evaluada.emergencia();
            EstadoEmergencia estado = evaluada.estado();

            if (estado.convocatoria() != null && estado.convocatoria().getEstado() != EstadoConvocatoria.BORRADOR) {
                convocatoriasPublicadas++;
            }
            Proyecto proyecto = emergencia.getProyecto();
            if (proyecto != null && Proyecto.EN_CURSO.equals(proyecto.getEstado())) {
                proyectosEnCurso++;
            } else if (proyecto != null && Proyecto.TERMINADO.equals(proyecto.getEstado())) {
                proyectosFinalizados++;
            }
            porEtapa.merge(estado.etapa(), 1L, Long::sum);
            porGravedad.merge(etiqueta(emergencia.getNivelGravedad()), 1L, Long::sum);

            for (Oferta oferta : estado.ofertas()) {
                if (oferta.getEstado() != null) {
                    porEstado.merge(oferta.getEstado(), 1L, Long::sum);
                }
                if (oferta.getEstado() != EstadoOferta.RETIRADA) {
                    ofertasRecibidas++;
                }
            }
            for (Oferta oferta : estado.validas()) {
                if (ESTADOS_MOVILIZADOS.contains(oferta.getEstado())) {
                    long cantidad = cantidad(oferta);
                    ofertasAdjudicadas++;
                    unidadesAdjudicadas += cantidad;
                    String recurso = oferta.getTipoRecurso() == null ? "Sin dato" : oferta.getTipoRecurso().getNombre();
                    suministrosPorTipo.merge(recurso, cantidad, Long::sum);
                    if (oferta.getOfertaConjunta() != null) {
                        adjudicadasEnConjunta++;
                        unidadesEnConjunta += cantidad;
                    }
                }
            }
        }

        TotalesDto totales = new TotalesDto(evaluadas.size(), convocatoriasPublicadas, ofertasRecibidas,
                ofertasAdjudicadas, proyectosEnCurso, proyectosFinalizados, unidadesAdjudicadas);
        OfertasConjuntasIndicadorDto conjuntas = new OfertasConjuntasIndicadorDto(ofertasAdjudicadas,
                adjudicadasEnConjunta, porcentaje(adjudicadasEnConjunta, ofertasAdjudicadas), unidadesAdjudicadas,
                unidadesEnConjunta, porcentaje(unidadesEnConjunta, unidadesAdjudicadas));

        List<CantidadPorEtiquetaDto> etapas = new ArrayList<>();
        porEtapa.forEach((etapa, cantidad) -> etapas.add(new CantidadPorEtiquetaDto(etapa.name(), cantidad)));
        List<CantidadPorEtiquetaDto> estados = new ArrayList<>();
        porEstado.forEach((estadoOferta, cantidad) -> estados.add(new CantidadPorEtiquetaDto(estadoOferta.name(), cantidad)));

        return new IndicadoresDto(LocalDateTime.now().withNano(0), desde, hasta, totales,
                ordenarPorCantidad(suministrosPorTipo), etapas, ordenarPorCantidad(porGravedad), estados,
                conjuntas, tiemposPorEtapa());
    }

    @Transactional(readOnly = true)
    public List<AuditoriaEmergenciaDto> emergencias(LocalDate desde, LocalDate hasta) {
        return evaluar(desde, hasta).stream().map(evaluada -> {
            Emergencia e = evaluada.emergencia();
            EstadoEmergencia estado = evaluada.estado();
            List<Oferta> movilizadas = estado.validas().stream()
                    .filter(o -> ESTADOS_MOVILIZADOS.contains(o.getEstado()))
                    .toList();
            long unidades = movilizadas.stream().mapToLong(IndicadoresService::cantidad).sum();
            long recibidas = estado.ofertas().stream().filter(o -> o.getEstado() != EstadoOferta.RETIRADA).count();
            long enConjunta = estado.validas().stream().filter(o -> o.getOfertaConjunta() != null).count();
            return new AuditoriaEmergenciaDto(e.getId(), e.getTipoDesastre(), e.getNivelGravedad(),
                    e.getZonaAfectada(), e.getFechaCreacion(), estado.etapa(),
                    estado.convocatoria() == null ? null : estado.convocatoria().getFechaCierre(),
                    recibidas, movilizadas.size(), unidades, enConjunta,
                    e.getProyecto() == null ? null : e.getProyecto().getEstado());
        }).toList();
    }

    public TiemposEtapasDto tiemposPorEtapa() {
        // [Entrega final] Se completa con /API/bpm/archivedFlowNode cuando el auditor tenga perfil en Bonita
        return new TiemposEtapasDto(false, "Bonita", List.of());
    }

    // Utilidades internas

    private record Evaluada(Emergencia emergencia, EstadoEmergencia estado) {
    }

    private List<Evaluada> evaluar(LocalDate desde, LocalDate hasta) {
        if (desde != null && hasta != null && desde.isAfter(hasta)) {
            throw new IllegalArgumentException("La fecha desde no puede ser posterior a la fecha hasta.");
        }
        List<Emergencia> emergencias = desde == null && hasta == null
                ? emergenciaRepository.findAllByOrderByIdDesc()
                : emergenciaRepository.findByFechaCreacionBetweenOrderByIdDesc(
                        desde == null ? DESDE_MINIMO : desde, hasta == null ? HASTA_MAXIMO : hasta);
        return emergencias.stream().map(e -> new Evaluada(e, etapaService.evaluar(e))).toList();
    }

    private static String etiqueta(String valor) {
        return valor == null || valor.isBlank() ? "Sin dato" : valor;
    }

    private static List<CantidadPorEtiquetaDto> ordenarPorCantidad(Map<String, Long> valores) {
        return valores.entrySet().stream()
                .map(entrada -> new CantidadPorEtiquetaDto(entrada.getKey(), entrada.getValue()))
                .sorted(Comparator.comparingLong(CantidadPorEtiquetaDto::cantidad).reversed())
                .toList();
    }

    private static double porcentaje(long parte, long total) {
        if (total == 0) {
            return 0.0;
        }
        return Math.round(1000.0 * parte / total) / 10.0;
    }

    private static long cantidad(Oferta oferta) {
        return oferta.getCantidad() == null ? 0L : oferta.getCantidad();
    }
}
