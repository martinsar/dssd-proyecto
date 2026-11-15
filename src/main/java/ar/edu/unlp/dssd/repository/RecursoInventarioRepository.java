package ar.edu.unlp.dssd.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ar.edu.unlp.dssd.model.RecursoInventario;

/** [Entrega final] Inventario de recursos de las ONGs */
@Repository
public interface RecursoInventarioRepository extends JpaRepository<RecursoInventario, Long> {

    List<RecursoInventario> findByOrganizacionIdOrderByTipoRecursoNombreAsc(Long organizacionId);

    Optional<RecursoInventario> findByOrganizacionIdAndTipoRecursoId(Long organizacionId, Long tipoRecursoId);
}
