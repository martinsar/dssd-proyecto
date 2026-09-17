package ar.edu.unlp.dssd.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ar.edu.unlp.dssd.model.Convocatoria;

@Repository
public interface ConvocatoriaRepository extends JpaRepository<Convocatoria, Long> {
}