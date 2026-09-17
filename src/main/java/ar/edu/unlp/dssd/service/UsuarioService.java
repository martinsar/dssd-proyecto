package ar.edu.unlp.dssd.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ar.edu.unlp.dssd.model.Usuario;
import ar.edu.unlp.dssd.repository.UsuarioRepository;

@Service // Esta anotación le dice a Spring que esta clase maneja lógica de negocio
public class UsuarioService {

    @Autowired // Inyectamos el repositorio que creaste antes
    private UsuarioRepository usuarioRepository;

    // CREATE / UPDATE: save() sirve para guardar uno nuevo o actualizar uno existente
    public Usuario guardarUsuario(Usuario usuario) {
        return usuarioRepository.save(usuario);
    }

    // READ (Todos): Busca todos los usuarios en la tabla
    public List<Usuario> obtenerTodos() {
        return usuarioRepository.findAll();
    }

    // READ (Uno solo): Busca por ID. Usamos Optional por si el ID no existe
    public Optional<Usuario> obtenerPorId(Long id) {
        return usuarioRepository.findById(id);
    }

    // DELETE: Borra el usuario por su ID
    public void eliminarUsuario(Long id) {
        usuarioRepository.deleteById(id);
    }
}