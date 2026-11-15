package ar.edu.unlp.dssd.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ar.edu.unlp.dssd.model.EstadoOferta;
import ar.edu.unlp.dssd.model.Oferta;
import jakarta.persistence.LockModeType;

@Repository
public interface OfertaRepository extends JpaRepository<Oferta, Long> {

    List<Oferta> findByConvocatoriaId(Long convocatoriaId);

    // [Entrega final] Ofertas de una organización (perfil ONG)
    List<Oferta> findByOrganizacionId(Long organizacionId);

    // [Entrega final] Lotes propios, del más reciente al más viejo
    List<Oferta> findByOrganizacionIdOrderByIdDesc(Long organizacionId);

    // [Entrega final] Lotes cargados en una oferta conjunta
    List<Oferta> findByOfertaConjuntaId(Long ofertaConjuntaId);

    // [Entrega final] Lotes de una organización en ciertos estados (inventario)
    List<Oferta> findByOrganizacionIdAndEstadoIn(Long organizacionId, Collection<EstadoOferta> estados);

    // [Entrega final] Se bloquea el lote para que dos ediciones simultáneas no pisen la misma versión
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT o FROM Oferta o WHERE o.id = :id")
    Optional<Oferta> buscarParaActualizar(@Param("id") Long id);
}
