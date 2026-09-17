package ar.edu.unlp.dssd.service;

import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ar.edu.unlp.dssd.model.Oferta;
import ar.edu.unlp.dssd.repository.OfertaRepository;

@Service
public class OfertaService {

    @Autowired
    private OfertaRepository ofertaRepository;

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