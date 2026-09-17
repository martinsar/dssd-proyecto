package ar.edu.unlp.dssd.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ar.edu.unlp.dssd.model.Proyecto; // Asegúrate de que el paquete coincida con el tuyo

@Repository
public interface ProyectoRepository extends JpaRepository<Proyecto, Long> {
    
    
}