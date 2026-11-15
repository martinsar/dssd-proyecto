package ar.edu.unlp.dssd.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ar.edu.unlp.dssd.model.Convocatoria;
import ar.edu.unlp.dssd.model.EstadoConvocatoria;

@Repository
public interface ConvocatoriaRepository extends JpaRepository<Convocatoria, Long> {

    List<Convocatoria> findByEmergenciaIdOrderByIdDesc(Long emergenciaId);

    boolean existsByEmergenciaIdAndEstadoIn(Long emergenciaId, Collection<EstadoConvocatoria> estados);

    // [Entrega final] Convocatoria vigente (la más reciente) de una emergencia
    Optional<Convocatoria> findFirstByEmergenciaIdOrderByIdDesc(Long emergenciaId);

    // [Entrega final] Convocatorias publicadas, de la que cierra antes a la que cierra después
    List<Convocatoria> findByEstadoOrderByFechaCierreAsc(EstadoConvocatoria estado);
}
