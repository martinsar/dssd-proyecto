package ar.edu.unlp.dssd.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ar.edu.unlp.dssd.dto.OfertaRequest;
import ar.edu.unlp.dssd.exception.RecursoNoEncontradoException;
import ar.edu.unlp.dssd.model.Convocatoria;
import ar.edu.unlp.dssd.model.Organizacion;
import ar.edu.unlp.dssd.model.TipoRecurso;
import ar.edu.unlp.dssd.model.Oferta;
import ar.edu.unlp.dssd.repository.OfertaRepository;
import ar.edu.unlp.dssd.repository.ConvocatoriaRepository;
import ar.edu.unlp.dssd.repository.OrganizacionRepository;
import ar.edu.unlp.dssd.repository.TipoRecursoRepository;

@Service
public class OfertaService {

    @Autowired
    private OfertaRepository ofertaRepository;

    @Autowired
    private ConvocatoriaRepository convocatoriaRepository;

    @Autowired
    private OrganizacionRepository organizacionRepository;

    @Autowired
    private TipoRecursoRepository tipoRecursoRepository;

    @Transactional
    public Oferta crear(OfertaRequest request) {
        Convocatoria convocatoria = convocatoriaRepository.findById(request.convocatoriaId())
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Convocatoria no encontrada: " + request.convocatoriaId()));
        Organizacion organizacion = organizacionRepository.findById(request.organizacionId())
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Organizacion no encontrada: " + request.organizacionId()));
        TipoRecurso tipoRecurso = tipoRecursoRepository.findById(request.tipoRecursoId())
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Tipo de recurso no encontrado: " + request.tipoRecursoId()));

        Oferta oferta = new Oferta();
        oferta.setCantidad(request.cantidad());
        oferta.setDescripcion(request.descripcion());
        oferta.setConvocatoria(convocatoria);
        oferta.setOrganizacion(organizacion);
        oferta.setTipoRecurso(tipoRecurso);
        oferta.setFechaCreacion(LocalDate.now());
        return ofertaRepository.save(oferta);
    }

    public Oferta guardar(Oferta oferta) {
        return ofertaRepository.save(oferta);
    }

    public List<Oferta> obtenerTodos() {
        return ofertaRepository.findAll();
    }

    public Optional<Oferta> obtenerPorId(Long id) {
        return ofertaRepository.findById(id);
    }

    public void eliminar(Long id) {
        ofertaRepository.deleteById(id);
    }
}