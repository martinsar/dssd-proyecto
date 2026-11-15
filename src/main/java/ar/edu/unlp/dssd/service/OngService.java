package ar.edu.unlp.dssd.service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ar.edu.unlp.dssd.dto.ConvocatoriaAbiertaDto;
import ar.edu.unlp.dssd.dto.EspecialidadDto;
import ar.edu.unlp.dssd.dto.LoteConvocatoriaDto;
import ar.edu.unlp.dssd.dto.OrganizacionResumenDto;
import ar.edu.unlp.dssd.exception.RecursoNoEncontradoException;
import ar.edu.unlp.dssd.model.EstadoConvocatoria;
import ar.edu.unlp.dssd.model.Necesidad;
import ar.edu.unlp.dssd.model.TipoRecurso;
import ar.edu.unlp.dssd.repository.ConvocatoriaRepository;
import ar.edu.unlp.dssd.repository.NecesidadRepository;
import ar.edu.unlp.dssd.repository.OrganizacionRepository;
import ar.edu.unlp.dssd.seguridad.SesionUsuario;

/** [Entrega final] Consultas del perfil ONG: convocatorias publicadas, especialidades y demás organizaciones */
@Service
public class OngService {

    @Autowired
    private ConvocatoriaRepository convocatoriaRepository;

    @Autowired
    private NecesidadRepository necesidadRepository;

    @Autowired
    private OrganizacionRepository organizacionRepository;

    /** Solo PUBLICADAS, de la que cierra antes a la que cierra después; abierta indica si reciben lotes */
    @Transactional(readOnly = true)
    public List<ConvocatoriaAbiertaDto> convocatorias() {
        LocalDateTime ahora = LocalDateTime.now();
        return convocatoriaRepository.findByEstadoOrderByFechaCierreAsc(EstadoConvocatoria.PUBLICADA).stream()
                .map(c -> ConvocatoriaAbiertaDto.desde(c, EtapaEmergenciaService.ventanaAbierta(c, ahora),
                        necesidadRepository.findByConvocatoriaId(c.getId()).stream()
                                .sorted(Comparator.comparing(Necesidad::getId,
                                        Comparator.nullsLast(Comparator.naturalOrder())))
                                .map(LoteConvocatoriaDto::desde)
                                .toList()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<EspecialidadDto> especialidades(SesionUsuario sesion) {
        Long organizacionId = OfertaService.exigirOrganizacion(sesion);
        return organizacionRepository.findById(organizacionId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Organización no encontrada."))
                .getEspecialidades().stream()
                .sorted(Comparator.comparing(TipoRecurso::getNombre, Comparator.nullsLast(Comparator.naturalOrder())))
                .map(EspecialidadDto::desde)
                .toList();
    }

    /** Las demás organizaciones (sin la propia) para armar una oferta conjunta */
    @Transactional(readOnly = true)
    public List<OrganizacionResumenDto> organizaciones(SesionUsuario sesion) {
        Long organizacionId = OfertaService.exigirOrganizacion(sesion);
        return organizacionRepository.findByIdNotOrderByNombreAsc(organizacionId).stream()
                .map(OrganizacionResumenDto::desde)
                .toList();
    }
}
