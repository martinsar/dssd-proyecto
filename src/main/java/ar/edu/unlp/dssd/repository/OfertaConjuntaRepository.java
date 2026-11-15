package ar.edu.unlp.dssd.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ar.edu.unlp.dssd.model.OfertaConjunta;

/** [Entrega final] Ofertas conjuntas entre ONGs */
@Repository
public interface OfertaConjuntaRepository extends JpaRepository<OfertaConjunta, Long> {

    // Ofertas conjuntas donde la organización es dueña o participante
    @Query("""
            SELECT DISTINCT oc FROM OfertaConjunta oc LEFT JOIN oc.participantes p
            WHERE oc.organizacionDuena.id = :organizacionId OR p.id = :organizacionId
            ORDER BY oc.id DESC
            """)
    List<OfertaConjunta> buscarDeOrganizacion(@Param("organizacionId") Long organizacionId);
}
