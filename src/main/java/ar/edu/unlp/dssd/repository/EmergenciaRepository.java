package ar.edu.unlp.dssd.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ar.edu.unlp.dssd.model.Emergencia;

@Repository
public interface EmergenciaRepository extends JpaRepository<Emergencia, Long> {

    // "Pendientes" = emergencias que todavia no tienen ninguna convocatoria PUBLICADA.
    // Incluye las que tienen una convocatoria en BORRADOR, para poder seguir cargando lotes.
    @Query("""
            SELECT e FROM Emergencia e
            WHERE NOT EXISTS (
                SELECT c.id FROM Convocatoria c
                WHERE c.emergencia = e
                  AND c.estado = ar.edu.unlp.dssd.model.EstadoConvocatoria.PUBLICADA
            )
            ORDER BY e.id DESC
            """)
    List<Emergencia> findPendientes();
}
