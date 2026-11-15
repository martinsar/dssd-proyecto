package ar.edu.unlp.dssd.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ar.edu.unlp.dssd.model.OfertaVersion;

/** [Entrega final] Historial de versiones de los lotes */
@Repository
public interface OfertaVersionRepository extends JpaRepository<OfertaVersion, Long> {

    List<OfertaVersion> findByOfertaIdOrderByNumeroVersionDesc(Long ofertaId);
}
