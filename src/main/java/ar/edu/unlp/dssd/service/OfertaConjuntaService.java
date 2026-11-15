package ar.edu.unlp.dssd.service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ar.edu.unlp.dssd.dto.OfertaConjuntaOngDto;
import ar.edu.unlp.dssd.dto.OfertaConjuntaRequest;
import ar.edu.unlp.dssd.dto.OfertaOngDto;
import ar.edu.unlp.dssd.dto.OrganizacionReferenciaDto;
import ar.edu.unlp.dssd.dto.ParticipanteConjuntaDto;
import ar.edu.unlp.dssd.exception.RecursoNoEncontradoException;
import ar.edu.unlp.dssd.exception.ReglaNegocioException;
import ar.edu.unlp.dssd.model.Convocatoria;
import ar.edu.unlp.dssd.model.Emergencia;
import ar.edu.unlp.dssd.model.EstadoOferta;
import ar.edu.unlp.dssd.model.Oferta;
import ar.edu.unlp.dssd.model.OfertaConjunta;
import ar.edu.unlp.dssd.model.Organizacion;
import ar.edu.unlp.dssd.repository.ConvocatoriaRepository;
import ar.edu.unlp.dssd.repository.OfertaConjuntaRepository;
import ar.edu.unlp.dssd.repository.OfertaRepository;
import ar.edu.unlp.dssd.repository.OrganizacionRepository;
import ar.edu.unlp.dssd.seguridad.SesionUsuario;

/** [Entrega final] Ofertas conjuntas: la dueña elige a las participantes y cada una carga sus lotes */
@Service
public class OfertaConjuntaService {

    @Autowired
    private OfertaConjuntaRepository ofertaConjuntaRepository;

    @Autowired
    private ConvocatoriaRepository convocatoriaRepository;

    @Autowired
    private OrganizacionRepository organizacionRepository;

    @Autowired
    private OfertaRepository ofertaRepository;

    @Autowired
    private OfertaService ofertaService;

    /** Sin invitaciones: los participantes quedan fijos desde el alta */
    @Transactional
    public OfertaConjuntaOngDto crear(OfertaConjuntaRequest request, SesionUsuario sesion) {
        Long organizacionId = OfertaService.exigirOrganizacion(sesion);
        if (request.participantesIds() == null || request.participantesIds().isEmpty()) {
            throw new IllegalArgumentException("Elegí al menos una organización participante.");
        }
        Convocatoria convocatoria = convocatoriaRepository.findById(request.convocatoriaId())
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Convocatoria no encontrada: " + request.convocatoriaId()));
        OfertaService.exigirVentanaAbierta(convocatoria);

        Set<Long> vistos = new HashSet<>();
        for (Long participanteId : request.participantesIds()) {
            if (participanteId == null) {
                throw new IllegalArgumentException("Hay una organización participante sin id.");
            }
            if (participanteId.equals(organizacionId)) {
                throw new ReglaNegocioException("Tu organización ya es la dueña de la oferta conjunta; no la incluyas como participante.");
            }
            if (!vistos.add(participanteId)) {
                throw new ReglaNegocioException("No se puede repetir una organización participante.");
            }
        }

        Organizacion duena = organizacionRepository.findById(organizacionId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Organizacion no encontrada: " + organizacionId));
        Set<Organizacion> participantes = new LinkedHashSet<>();
        for (Long participanteId : request.participantesIds()) {
            participantes.add(organizacionRepository.findById(participanteId)
                    .orElseThrow(() -> new RecursoNoEncontradoException(
                            "Organizacion no encontrada: " + participanteId)));
        }

        OfertaConjunta conjunta = new OfertaConjunta();
        conjunta.setConvocatoria(convocatoria);
        conjunta.setOrganizacionDuena(duena);
        conjunta.setParticipantes(participantes);
        conjunta.setFechaCreacion(LocalDateTime.now());
        return aDto(ofertaConjuntaRepository.save(conjunta), organizacionId);
    }

    /** Ofertas conjuntas donde la organización es dueña o participante */
    @Transactional(readOnly = true)
    public List<OfertaConjuntaOngDto> listar(SesionUsuario sesion) {
        Long organizacionId = OfertaService.exigirOrganizacion(sesion);
        return ofertaConjuntaRepository.buscarDeOrganizacion(organizacionId).stream()
                .map(c -> aDto(c, organizacionId))
                .toList();
    }

    /** Si la organización no es dueña ni participante responde 404 */
    @Transactional(readOnly = true)
    public OfertaConjuntaOngDto obtener(Long id, SesionUsuario sesion) {
        Long organizacionId = OfertaService.exigirOrganizacion(sesion);
        OfertaConjunta conjunta = ofertaConjuntaRepository.findById(id)
                .filter(c -> OfertaService.esAsociada(c, organizacionId))
                .orElseThrow(() -> new RecursoNoEncontradoException("Oferta conjunta no encontrada."));
        return aDto(conjunta, organizacionId);
    }

    private OfertaConjuntaOngDto aDto(OfertaConjunta conjunta, Long organizacionId) {
        List<Oferta> lotes = conjunta.getId() == null ? List.of() : ofertaRepository.findByOfertaConjuntaId(conjunta.getId());
        List<Organizacion> faltantes = IntegracionService.faltantes(conjunta, lotes);
        boolean valida = faltantes.isEmpty();

        List<ParticipanteConjuntaDto> participantes = IntegracionService.asociadas(conjunta).stream()
                .map(org -> new ParticipanteConjuntaDto(org.getId(), org.getCodigoOng(), org.getNombre(),
                        lotes.stream().filter(o -> o.getEstado() != EstadoOferta.RETIRADA
                                && o.getOrganizacion() != null
                                && org.getId().equals(o.getOrganizacion().getId())).count()))
                .toList();

        Map<Long, Boolean> validez = new HashMap<>();
        validez.put(conjunta.getId(), valida);
        List<OfertaOngDto> lotesDto = lotes.stream()
                .sorted((a, b) -> Long.compare(a.getId() == null ? 0 : a.getId(), b.getId() == null ? 0 : b.getId()))
                .map(o -> ofertaService.aDto(o, organizacionId, validez))
                .toList();

        Convocatoria c = conjunta.getConvocatoria();
        Emergencia e = c == null ? null : c.getEmergencia();
        Organizacion duena = conjunta.getOrganizacionDuena();
        return new OfertaConjuntaOngDto(conjunta.getId(),
                c == null ? null : c.getId(),
                e == null ? null : e.getId(),
                e == null ? null : e.getTipoDesastre(),
                c == null ? null : c.getFechaCierre(),
                EtapaEmergenciaService.ventanaAbierta(c, LocalDateTime.now()),
                OrganizacionReferenciaDto.desde(duena),
                duena != null && organizacionId.equals(duena.getId()),
                participantes, valida, faltantes.stream().map(Organizacion::getNombre).toList(), lotesDto);
    }
}
