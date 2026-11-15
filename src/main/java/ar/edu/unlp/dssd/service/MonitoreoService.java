package ar.edu.unlp.dssd.service;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ar.edu.unlp.dssd.dto.EmergenciaDetalleDto;
import ar.edu.unlp.dssd.dto.MonitoreoFilaDto;
import ar.edu.unlp.dssd.dto.OfertaSeguimientoDto;
import ar.edu.unlp.dssd.dto.SeguimientoEmergenciaDto;
import ar.edu.unlp.dssd.exception.RecursoNoEncontradoException;
import ar.edu.unlp.dssd.model.Emergencia;
import ar.edu.unlp.dssd.model.EtapaEmergencia;
import ar.edu.unlp.dssd.model.Oferta;
import ar.edu.unlp.dssd.repository.EmergenciaRepository;
import ar.edu.unlp.dssd.service.EtapaEmergenciaService.EstadoEmergencia;

/** [Entrega final] Tablero de monitoreo y seguimiento del Centro Coordinador */
@Service
public class MonitoreoService {

    @Autowired
    private EmergenciaRepository emergenciaRepository;

    @Autowired
    private EtapaEmergenciaService etapaService;

    @Autowired
    private IntegracionService integracionService;

    /** Primero las que requieren decisión y después por id descendente; el filtro de etapa es opcional */
    @Transactional(readOnly = true)
    public List<MonitoreoFilaDto> monitoreo(EtapaEmergencia filtro) {
        return emergenciaRepository.findAllByOrderByIdDesc().stream()
                .map(emergencia -> MonitoreoFilaDto.desde(emergencia, etapaService.evaluar(emergencia)))
                .filter(fila -> filtro == null || fila.etapa() == filtro)
                .sorted(Comparator.comparing(MonitoreoFilaDto::requiereDecision).reversed()
                        .thenComparing(MonitoreoFilaDto::emergenciaId,
                                Comparator.nullsLast(Comparator.<Long>reverseOrder())))
                .toList();
    }

    /** Detalle con lo ofertado por lote y todos los lotes (incluidos retirados y de ofertas conjuntas no válidas) */
    @Transactional(readOnly = true)
    public SeguimientoEmergenciaDto seguimiento(Long emergenciaId) {
        Emergencia emergencia = emergenciaRepository.findById(emergenciaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Emergencia no encontrada."));
        EstadoEmergencia estado = etapaService.evaluar(emergencia);
        Map<Long, Boolean> validez = new HashMap<>();
        List<OfertaSeguimientoDto> ofertas = estado.ofertas().stream()
                .map(o -> OfertaSeguimientoDto.desde(o, conjuntaValida(o, validez)))
                .toList();
        return new SeguimientoEmergenciaDto(EmergenciaDetalleDto.desde(emergencia, estado, true), ofertas);
    }

    private boolean conjuntaValida(Oferta oferta, Map<Long, Boolean> validez) {
        if (oferta.getOfertaConjunta() == null) {
            return true;
        }
        return validez.computeIfAbsent(oferta.getOfertaConjunta().getId(),
                id -> integracionService.conjuntaEsValida(oferta.getOfertaConjunta()));
    }
}
