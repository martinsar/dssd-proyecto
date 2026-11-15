package ar.edu.unlp.dssd.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ar.edu.unlp.dssd.dto.AdjudicacionRequest;
import ar.edu.unlp.dssd.dto.AdjudicacionResponse;
import ar.edu.unlp.dssd.dto.EmergenciaDetalleDto;
import ar.edu.unlp.dssd.dto.EmergenciaResumenDto;
import ar.edu.unlp.dssd.dto.OfertaValidadaDto;
import ar.edu.unlp.dssd.exception.RecursoNoEncontradoException;
import ar.edu.unlp.dssd.exception.ReglaNegocioException;
import ar.edu.unlp.dssd.model.Emergencia;
import ar.edu.unlp.dssd.model.EstadoOferta;
import ar.edu.unlp.dssd.model.EtapaEmergencia;
import ar.edu.unlp.dssd.model.Oferta;
import ar.edu.unlp.dssd.model.OfertaVersion;
import ar.edu.unlp.dssd.repository.EmergenciaRepository;
import ar.edu.unlp.dssd.repository.OfertaRepository;
import ar.edu.unlp.dssd.repository.OfertaVersionRepository;
import ar.edu.unlp.dssd.repository.UsuarioRepository;
import ar.edu.unlp.dssd.seguridad.SesionUsuario;
import ar.edu.unlp.dssd.service.EtapaEmergenciaService.EstadoEmergencia;

/** [Entrega final] Vista del operador municipal: solo sus emergencias y los lotes válidos ya validados */
@Service
public class MunicipioService {

    @Autowired
    private EmergenciaRepository emergenciaRepository;

    @Autowired
    private OfertaRepository ofertaRepository;

    @Autowired
    private OfertaVersionRepository ofertaVersionRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private EtapaEmergenciaService etapaService;

    @Autowired
    private IntegracionService integracionService;

    @Autowired
    private TareasFinalesBonitaService tareasFinalesBonita;

    @Transactional(readOnly = true)
    public List<EmergenciaResumenDto> misEmergencias(SesionUsuario sesion) {
        return emergenciaRepository.findByCreadoPorIdOrderByIdDesc(sesion.usuarioId()).stream()
                .map(e -> EmergenciaResumenDto.desde(e, etapaService.evaluar(e)))
                .toList();
    }

    /** El municipio no ve lo ofertado de cada lote (cantidadOfertada en null) */
    @Transactional(readOnly = true)
    public EmergenciaDetalleDto detalle(Long emergenciaId, SesionUsuario sesion) {
        Emergencia emergencia = propia(emergenciaId, sesion);
        return EmergenciaDetalleDto.desde(emergencia, etapaService.evaluar(emergencia), false);
    }

    /** Regla 7: lotes válidos en VALIDADA, SELECCIONADA, ADJUDICADA, NO_SELECCIONADA o FINALIZADA */
    @Transactional(readOnly = true)
    public List<OfertaValidadaDto> ofertasValidadas(Long emergenciaId, SesionUsuario sesion) {
        Emergencia emergencia = propia(emergenciaId, sesion);
        return etapaService.evaluar(emergencia).validas().stream()
                .filter(o -> EtapaEmergenciaService.ESTADOS_VALIDADOS.contains(o.getEstado()))
                .map(OfertaValidadaDto::desde)
                .toList();
    }

    /**
     * Regla 8: los elegidos pasan a SELECCIONADA y el resto de los VALIDADA a NO_SELECCIONADA.
     * Con integracion-completa=false se adjudica en el momento; con true se espera la notificación de Bonita.
     */
    @Transactional
    public AdjudicacionResponse adjudicar(Long emergenciaId, AdjudicacionRequest request, SesionUsuario sesion) {
        Emergencia emergencia = propia(emergenciaId, sesion);
        EstadoEmergencia estado = etapaService.evaluar(emergencia);
        if (estado.etapa() != EtapaEmergencia.EN_SELECCION) {
            throw new ReglaNegocioException("La emergencia no está en etapa de selección de ofertas.");
        }
        if (request == null || request.ofertaIds() == null || request.ofertaIds().isEmpty()) {
            throw new IllegalArgumentException("Elegí al menos una oferta.");
        }
        Set<Long> elegidas = new HashSet<>(request.ofertaIds());
        List<Oferta> validadas = estado.validas().stream()
                .filter(o -> o.getEstado() == EstadoOferta.VALIDADA)
                .toList();
        Set<Long> idsValidados = new HashSet<>(validadas.stream().map(Oferta::getId).toList());
        if (!idsValidados.containsAll(elegidas)) {
            throw new IllegalArgumentException("Solo se pueden seleccionar ofertas validadas de esta emergencia.");
        }

        int seleccionadas = 0;
        int noSeleccionadas = 0;
        for (Oferta oferta : validadas) {
            boolean elegida = elegidas.contains(oferta.getId());
            oferta.setEstado(elegida ? EstadoOferta.SELECCIONADA : EstadoOferta.NO_SELECCIONADA);
            oferta.avanzarVersion();
            ofertaRepository.save(oferta);
            ofertaVersionRepository.save(OfertaVersion.desde(oferta,
                    sesion.usuarioId() == null ? null : usuarioRepository.getReferenceById(sesion.usuarioId()),
                    elegida ? "Seleccionado por el municipio" : "No seleccionado por el municipio"));
            if (elegida) {
                seleccionadas++;
            } else {
                noSeleccionadas++;
            }
        }

        // Con integracion-completa=true un fallo de Bonita deshace la selección
        tareasFinalesBonita.seleccionarOfertas(emergencia.getCaseId());
        if (!tareasFinalesBonita.integracionCompleta()) {
            integracionService.confirmarAdjudicacion(emergenciaId);
        }
        return new AdjudicacionResponse(seleccionadas, noSeleccionadas, etapaService.evaluar(emergencia).etapa());
    }

    private Emergencia propia(Long emergenciaId, SesionUsuario sesion) {
        return emergenciaRepository.findByIdAndCreadoPorId(emergenciaId, sesion.usuarioId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Emergencia no encontrada."));
    }
}
