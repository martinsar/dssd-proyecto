package ar.edu.unlp.dssd.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ar.edu.unlp.dssd.model.Oferta;

@Repository
public interface OfertaRepository extends JpaRepository<Oferta, Long> {

    List<Oferta> findByConvocatoriaId(Long convocatoriaId);
}