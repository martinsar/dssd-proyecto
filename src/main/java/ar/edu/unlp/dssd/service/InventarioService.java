package ar.edu.unlp.dssd.service;

import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ar.edu.unlp.dssd.dto.InventarioItemDto;
import ar.edu.unlp.dssd.dto.InventarioRequest;
import ar.edu.unlp.dssd.exception.RecursoNoEncontradoException;
import ar.edu.unlp.dssd.model.EstadoOferta;
import ar.edu.unlp.dssd.model.Oferta;
import ar.edu.unlp.dssd.model.RecursoInventario;
import ar.edu.unlp.dssd.repository.OfertaRepository;
import ar.edu.unlp.dssd.repository.OrganizacionRepository;
import ar.edu.unlp.dssd.repository.RecursoInventarioRepository;
import ar.edu.unlp.dssd.repository.TipoRecursoRepository;
import ar.edu.unlp.dssd.seguridad.SesionUsuario;

/** [Entrega final] Inventario de recursos de la ONG con lo ofertado y lo comprometido */
@Service
public class InventarioService {

    private static final Set<EstadoOferta> OFERTADOS = EnumSet.of(EstadoOferta.PENDIENTE, EstadoOferta.VALIDADA);
    private static final Set<EstadoOferta> COMPROMETIDOS = EnumSet.of(EstadoOferta.SELECCIONADA, EstadoOferta.ADJUDICADA);

    @Autowired
    private RecursoInventarioRepository inventarioRepository;

    @Autowired
    private OfertaRepository ofertaRepository;

    @Autowired
    private OrganizacionRepository organizacionRepository;

    @Autowired
    private TipoRecursoRepository tipoRecursoRepository;

    @Transactional(readOnly = true)
    public List<InventarioItemDto> listar(SesionUsuario sesion) {
        Long organizacionId = OfertaService.exigirOrganizacion(sesion);
        Map<Long, Long> ofertado = sumarPorTipo(organizacionId, OFERTADOS);
        Map<Long, Long> comprometido = sumarPorTipo(organizacionId, COMPROMETIDOS);
        return inventarioRepository.findByOrganizacionIdOrderByTipoRecursoNombreAsc(organizacionId).stream()
                .map(r -> aDto(r, ofertado, comprometido))
                .toList();
    }

    /** Se crea o actualiza el recurso del tipo indicado */
    @Transactional
    public InventarioItemDto guardar(Long tipoRecursoId, InventarioRequest request, SesionUsuario sesion) {
        Long organizacionId = OfertaService.exigirOrganizacion(sesion);
        RecursoInventario recurso = inventarioRepository
                .findByOrganizacionIdAndTipoRecursoId(organizacionId, tipoRecursoId)
                .orElseGet(() -> {
                    RecursoInventario nuevo = new RecursoInventario();
                    nuevo.setOrganizacion(organizacionRepository.findById(organizacionId)
                            .orElseThrow(() -> new RecursoNoEncontradoException("Organización no encontrada.")));
                    nuevo.setTipoRecurso(tipoRecursoRepository.findById(tipoRecursoId)
                            .orElseThrow(() -> new RecursoNoEncontradoException(
                                    "Tipo de recurso no encontrado: " + tipoRecursoId)));
                    return nuevo;
                });
        recurso.setCantidadDisponible(request.cantidadDisponible());
        recurso.setDescripcion(request.descripcion());
        recurso.setFechaActualizacion(LocalDateTime.now());
        RecursoInventario guardado = inventarioRepository.save(recurso);
        return aDto(guardado, sumarPorTipo(organizacionId, OFERTADOS), sumarPorTipo(organizacionId, COMPROMETIDOS));
    }

    @Transactional
    public void eliminar(Long tipoRecursoId, SesionUsuario sesion) {
        Long organizacionId = OfertaService.exigirOrganizacion(sesion);
        RecursoInventario recurso = inventarioRepository
                .findByOrganizacionIdAndTipoRecursoId(organizacionId, tipoRecursoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("El recurso no está en tu inventario."));
        inventarioRepository.delete(recurso);
    }

    private InventarioItemDto aDto(RecursoInventario r, Map<Long, Long> ofertado, Map<Long, Long> comprometido) {
        Long tipo = r.getTipoRecurso() == null ? null : r.getTipoRecurso().getId();
        return InventarioItemDto.desde(r, ofertado.getOrDefault(tipo, 0L), comprometido.getOrDefault(tipo, 0L));
    }

    private Map<Long, Long> sumarPorTipo(Long organizacionId, Set<EstadoOferta> estados) {
        Map<Long, Long> suma = new HashMap<>();
        for (Oferta oferta : ofertaRepository.findByOrganizacionIdAndEstadoIn(organizacionId, estados)) {
            if (oferta.getTipoRecurso() != null) {
                suma.merge(oferta.getTipoRecurso().getId(),
                        oferta.getCantidad() == null ? 0L : oferta.getCantidad(), Long::sum);
            }
        }
        return suma;
    }
}
