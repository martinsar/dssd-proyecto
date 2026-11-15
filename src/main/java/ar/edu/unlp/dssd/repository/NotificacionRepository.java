package ar.edu.unlp.dssd.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ar.edu.unlp.dssd.model.Notificacion;
import ar.edu.unlp.dssd.model.TipoNotificacion;

/** [Entrega final] Notificaciones de las ONGs */
@Repository
public interface NotificacionRepository extends JpaRepository<Notificacion, Long> {

    List<Notificacion> findTop100ByOrganizacionIdOrderByFechaHoraDescIdDesc(Long organizacionId);

    long countByOrganizacionIdAndLeidaFalse(Long organizacionId);

    Optional<Notificacion> findByIdAndOrganizacionId(Long id, Long organizacionId);

    boolean existsByOfertaIdAndOrganizacionIdAndTipo(Long ofertaId, Long organizacionId, TipoNotificacion tipo);

    @Modifying
    @Query("UPDATE Notificacion n SET n.leida = true WHERE n.organizacion.id = :organizacionId AND n.leida = false")
    int marcarTodasLeidas(@Param("organizacionId") Long organizacionId);
}
