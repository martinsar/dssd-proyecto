
package ar.edu.unlp.dssd.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ar.edu.unlp.dssd.model.TipoRecurso;

@Repository
public interface TipoRecursoRepository extends JpaRepository<TipoRecurso, Long> {

}