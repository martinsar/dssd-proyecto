package ar.edu.unlp.dssd.service;

import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ar.edu.unlp.dssd.model.Organizacion;
import ar.edu.unlp.dssd.repository.OrganizacionRepository;

@Service
public class OrganizacionService {

    @Autowired
    private OrganizacionRepository organizacionRepository;

    public Organizacion guardar(Organizacion organizacion) {
        return organizacionRepository.save(organizacion);
    }

    public List<Organizacion> obtenerTodos() {
        return organizacionRepository.findAll();
    }

    public Optional<Organizacion> obtenerPorId(Long id) {
        return organizacionRepository.findById(id);
    }

    public void eliminar(Long id) {
        organizacionRepository.deleteById(id);
    }
}