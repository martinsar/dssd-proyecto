package ar.edu.unlp.dssd.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ar.edu.unlp.dssd.model.Convocatoria;
import ar.edu.unlp.dssd.model.EstadoConvocatoria;

@Repository
public interface ConvocatoriaRepository extends JpaRepository<Convocatoria, Long> {

    List<Convocatoria> findByEmergenciaIdOrderByIdDesc(Long emergenciaId);

    boolean existsByEmergenciaIdAndEstadoIn(Long emergenciaId, Collection<EstadoConvocatoria> estados);
}
