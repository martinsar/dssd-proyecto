package ar.edu.unlp.dssd.service;

import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ar.edu.unlp.dssd.model.TipoOrganizacion;
import ar.edu.unlp.dssd.repository.TipoOrganizacionRepository;

@Service
public class TipoOrganizacionService {

    @Autowired
    private TipoOrganizacionRepository tipoOrganizacionRepository;

    public TipoOrganizacion guardar(TipoOrganizacion tipo) {
        return tipoOrganizacionRepository.save(tipo);
    }

    public List<TipoOrganizacion> obtenerTodos() {
        return tipoOrganizacionRepository.findAll();
    }

    public Optional<TipoOrganizacion> obtenerPorId(Long id) {
        return tipoOrganizacionRepository.findById(id);
    }

    public void eliminar(Long id) {
        tipoOrganizacionRepository.deleteById(id);
    }
}