package ar.edu.unlp.dssd.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ar.edu.unlp.dssd.dto.LoteDetalleDto;
import ar.edu.unlp.dssd.model.Convocatoria;
import ar.edu.unlp.dssd.model.Emergencia;
import ar.edu.unlp.dssd.model.EstadoConvocatoria;
import ar.edu.unlp.dssd.model.EstadoOferta;
import ar.edu.unlp.dssd.model.EtapaEmergencia;
import ar.edu.unlp.dssd.model.Necesidad;
import ar.edu.unlp.dssd.model.Oferta;
import ar.edu.unlp.dssd.repository.ConvocatoriaRepository;
import ar.edu.unlp.dssd.repository.NecesidadRepository;
import ar.edu.unlp.dssd.repository.OfertaRepository;

/** [Entrega final] Se calcula la etapa de la emergencia y la cobertura de sus lotes */
@Service
public class EtapaEmergenciaService {

    /** Estados que cuentan como validados por el Sistema Nacional (los que ve el municipio) */
    public static final Set<EstadoOferta> ESTADOS_VALIDADOS = EnumSet.of(EstadoOferta.VALIDADA,
            EstadoOferta.SELECCIONADA, EstadoOferta.ADJUDICADA, EstadoOferta.NO_SELECCIONADA,
            EstadoOferta.FINALIZADA);

    /** Estados que cuentan como adjudicados */
    public static final Set<EstadoOferta> ESTADOS_ADJUDICADOS = EnumSet.of(EstadoOferta.SELECCIONADA,
            EstadoOferta.ADJUDICADA, EstadoOferta.FINALIZADA);

    /**
     * Estado de una emergencia. ofertas: todos los lotes de la convocatoria vigente;
     * validas: los que cuentan (no retirados y de ofertas conjuntas válidas).
     */
    public record EstadoEmergencia(EtapaEmergencia etapa, Convocatoria convocatoria, List<Necesidad> lotes,
                                   List<Oferta> ofertas, List<Oferta> validas, List<LoteDetalleDto> cobertura) {
    }

    @Autowired
    private ConvocatoriaRepository convocatoriaRepository;

    @Autowired
    private NecesidadRepository necesidadRepository;

    @Autowired
    private OfertaRepository ofertaRepository;

    @Autowired
    private IntegracionService integracionService;

    /** Ventana abierta: convocatoria PUBLICADA, apertura alcanzada y cierre no vencido */
    public static boolean ventanaAbierta(Convocatoria c, LocalDateTime ahora) {
        return c != null
                && c.getEstado() == EstadoConvocatoria.PUBLICADA
                && c.getFechaCierre() != null
                && (c.getFechaApertura() == null || !c.getFechaApertura().isAfter(ahora))
                && ahora.isBefore(c.getFechaCierre());
    }

    /**
     * [Entrega final] Etapa a partir de la convocatoria vigente y de los lotes válidos (regla 10). Orden:
     * sin convocatoria o BORRADOR: EN_RELEVAMIENTO. PUBLICADA y antes del cierre: CONVOCATORIA_ABIERTA.
     * Con la ventana vencida: solo FINALIZADA (sin ADJUDICADA ni SELECCIONADA): FINALIZADA; alguna FINALIZADA
     * y quedan ADJUDICADA: EN_EJECUCION; alguna SELECCIONADA o ADJUDICADA (ninguna finalizada): ADJUDICADA;
     * sin lotes: REQUIERE_DECISION; alguna PENDIENTE: EN_VALIDACION; lo validado no cubre lo pedido y no se
     * aceptó cobertura parcial: REQUIERE_DECISION; alguna VALIDADA: EN_SELECCION; si no, REQUIERE_DECISION.
     */
    public EtapaEmergencia calcularEtapa(Convocatoria c, List<Oferta> validas, List<Necesidad> lotes,
                                         LocalDateTime ahora) {
        if (c == null || c.getEstado() == null || c.getEstado() == EstadoConvocatoria.BORRADOR) {
            return EtapaEmergencia.EN_RELEVAMIENTO;
        }
        if (c.getEstado() == EstadoConvocatoria.PUBLICADA
                && (c.getFechaCierre() == null || ahora.isBefore(c.getFechaCierre()))) {
            return EtapaEmergencia.CONVOCATORIA_ABIERTA;
        }

        List<Oferta> activas = nulo(validas).stream().filter(o -> o.getEstado() != EstadoOferta.RETIRADA).toList();
        boolean haySeleccionada = hay(activas, EstadoOferta.SELECCIONADA);
        boolean hayAdjudicada = hay(activas, EstadoOferta.ADJUDICADA);
        boolean hayFinalizada = hay(activas, EstadoOferta.FINALIZADA);
        if (hayFinalizada && !hayAdjudicada && !haySeleccionada) {
            return EtapaEmergencia.FINALIZADA;
        }
        if (hayFinalizada) {
            return EtapaEmergencia.EN_EJECUCION;
        }
        if (haySeleccionada || hayAdjudicada) {
            return EtapaEmergencia.ADJUDICADA;
        }
        if (activas.isEmpty()) {
            return EtapaEmergencia.REQUIERE_DECISION;
        }
        if (hay(activas, EstadoOferta.PENDIENTE)) {
            return EtapaEmergencia.EN_VALIDACION;
        }
        if (!coberturaCompleta(lotes, activas, ESTADOS_VALIDADOS)
                && !Boolean.TRUE.equals(c.getCoberturaParcialAceptada())) {
            return EtapaEmergencia.REQUIERE_DECISION;
        }
        if (hay(activas, EstadoOferta.VALIDADA)) {
            return EtapaEmergencia.EN_SELECCION;
        }
        return EtapaEmergencia.REQUIERE_DECISION;
    }

    /** Se verifica que lo de los estados indicados (null = todos) cubra lo requerido por tipo de recurso */
    public boolean coberturaCompleta(List<Necesidad> lotes, List<Oferta> ofertas, Set<EstadoOferta> estados) {
        Map<Long, Long> requerido = new HashMap<>();
        for (Necesidad lote : nulo(lotes)) {
            if (lote.getTipoRecurso() != null) {
                requerido.merge(lote.getTipoRecurso().getId(), cantidad(lote.getCantidad()), Long::sum);
            }
        }
        Map<Long, Long> cubierto = sumarPorTipo(nulo(ofertas), estados);
        return requerido.entrySet().stream()
                .allMatch(r -> cubierto.getOrDefault(r.getKey(), 0L) >= r.getValue());
    }

    /** Se arma la cobertura de cada lote; lo de un tipo se reparte entre sus lotes en orden de id */
    public List<LoteDetalleDto> cobertura(List<Necesidad> lotes, List<Oferta> validas) {
        List<Necesidad> ordenados = new ArrayList<>(nulo(lotes));
        ordenados.sort(Comparator.comparing(Necesidad::getId, Comparator.nullsLast(Comparator.naturalOrder())));

        long[] ofertada = repartir(ordenados, sumarPorTipo(nulo(validas), null));
        long[] validada = repartir(ordenados, sumarPorTipo(nulo(validas), ESTADOS_VALIDADOS));
        long[] adjudicada = repartir(ordenados, sumarPorTipo(nulo(validas), ESTADOS_ADJUDICADOS));

        List<LoteDetalleDto> resultado = new ArrayList<>();
        for (int i = 0; i < ordenados.size(); i++) {
            Necesidad lote = ordenados.get(i);
            resultado.add(new LoteDetalleDto(lote.getId(),
                    lote.getTipoRecurso() == null ? null : lote.getTipoRecurso().getId(),
                    lote.getTipoRecurso() == null ? null : lote.getTipoRecurso().getCodigo(),
                    lote.getTipoRecurso() == null ? null : lote.getTipoRecurso().getNombre(),
                    lote.getDescripcion(), lote.getCantidad(), ofertada[i], validada[i], adjudicada[i]));
        }
        return resultado;
    }

    /** Se carga la convocatoria vigente con sus lotes y ofertas y se calcula la etapa */
    @Transactional(readOnly = true)
    public EstadoEmergencia evaluar(Emergencia emergencia) {
        Convocatoria convocatoria = convocatoriaRepository.findFirstByEmergenciaIdOrderByIdDesc(emergencia.getId())
                .orElse(null);
        if (convocatoria == null) {
            return new EstadoEmergencia(EtapaEmergencia.EN_RELEVAMIENTO, null, List.of(), List.of(), List.of(),
                    List.of());
        }
        List<Necesidad> lotes = necesidadRepository.findByConvocatoriaId(convocatoria.getId());
        List<Oferta> ofertas = new ArrayList<>(ofertaRepository.findByConvocatoriaId(convocatoria.getId()));
        ofertas.sort(Comparator.comparing(Oferta::getId, Comparator.nullsLast(Comparator.naturalOrder())));
        List<Oferta> validas = integracionService.filtrarValidas(ofertas);

        EtapaEmergencia etapa = calcularEtapa(convocatoria, validas, lotes, LocalDateTime.now());
        return new EstadoEmergencia(etapa, convocatoria, lotes, ofertas, validas, cobertura(lotes, validas));
    }

    // Utilidades internas

    private static boolean hay(List<Oferta> ofertas, EstadoOferta estado) {
        return ofertas.stream().anyMatch(o -> o.getEstado() == estado);
    }

    private static Map<Long, Long> sumarPorTipo(List<Oferta> ofertas, Set<EstadoOferta> estados) {
        Map<Long, Long> suma = new HashMap<>();
        for (Oferta oferta : ofertas) {
            if (oferta.getTipoRecurso() == null || oferta.getEstado() == EstadoOferta.RETIRADA
                    || (estados != null && !estados.contains(oferta.getEstado()))) {
                continue;
            }
            suma.merge(oferta.getTipoRecurso().getId(), cantidad(oferta.getCantidad()), Long::sum);
        }
        return suma;
    }

    /** Se reparte lo disponible de cada tipo entre sus lotes; el último lote del tipo recibe el resto */
    private static long[] repartir(List<Necesidad> lotes, Map<Long, Long> disponiblePorTipo) {
        Map<Long, Integer> lotesRestantes = new HashMap<>();
        for (Necesidad lote : lotes) {
            if (lote.getTipoRecurso() != null) {
                lotesRestantes.merge(lote.getTipoRecurso().getId(), 1, Integer::sum);
            }
        }
        Map<Long, Long> pool = new HashMap<>(disponiblePorTipo);
        long[] asignado = new long[lotes.size()];
        for (int i = 0; i < lotes.size(); i++) {
            Necesidad lote = lotes.get(i);
            if (lote.getTipoRecurso() == null) {
                continue;
            }
            Long tipo = lote.getTipoRecurso().getId();
            int restantes = lotesRestantes.merge(tipo, -1, Integer::sum);
            long disponible = pool.getOrDefault(tipo, 0L);
            long parte = restantes == 0 ? disponible : Math.min(disponible, cantidad(lote.getCantidad()));
            asignado[i] = parte;
            pool.put(tipo, disponible - parte);
        }
        return asignado;
    }

    private static long cantidad(Integer valor) {
        return valor == null ? 0L : valor;
    }

    private static <T> List<T> nulo(List<T> lista) {
        return lista == null ? List.of() : lista;
    }
}
