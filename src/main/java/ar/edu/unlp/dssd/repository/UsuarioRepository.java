package ar.edu.unlp.dssd.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ar.edu.unlp.dssd.model.Usuario; // Asegúrate de que el paquete coincida con el tuyo

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    // [Entrega final] Se busca el usuario local vinculado al usuario de Bonita
    Optional<Usuario> findByUsuarioBonita(String usuarioBonita);
}