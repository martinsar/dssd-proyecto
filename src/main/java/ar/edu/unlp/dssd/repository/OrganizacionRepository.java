package ar.edu.unlp.dssd.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ar.edu.unlp.dssd.model.Organizacion;

@Repository
public interface OrganizacionRepository extends JpaRepository<Organizacion, Long> {

    // [Entrega final] Las demás organizaciones (para armar una oferta conjunta)
    List<Organizacion> findByIdNotOrderByNombreAsc(Long id);
}
