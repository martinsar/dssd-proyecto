package ar.edu.unlp.dssd.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import ar.edu.unlp.dssd.model.Usuario; // ¡Importación corregida a model!
import ar.edu.unlp.dssd.service.UsuarioService;

@RestController
@RequestMapping("/api/usuarios")
@Tag(name = "Usuarios", description = "Administración de usuarios.")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    // 1. CREAR (Método POST)
    @PostMapping
    @Operation(summary = "Crear usuario", description = "Registra un usuario.")
    public ResponseEntity<Usuario> crearUsuario(@RequestBody Usuario usuario) {
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioService.guardarUsuario(usuario));
    }

    // 2. LEER TODOS (Método GET)
    @GetMapping
    @Operation(summary = "Listar usuarios", description = "Devuelve todos los usuarios registrados.")
    public List<Usuario> listarUsuarios() {
        return usuarioService.obtenerTodos();
    }

    // 3. LEER UNO (Método GET)
    @GetMapping("/{id}")
    @Operation(summary = "Obtener usuario", description = "Busca un usuario por su identificador.")
    public ResponseEntity<Usuario> obtenerUsuario(@PathVariable Long id) {
        return usuarioService.obtenerPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // 4. ACTUALIZAR (Método PUT)
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar usuario", description = "Actualiza el nombre, apellido y CUIL de un usuario existente.")
    public ResponseEntity<Usuario> actualizarUsuario(@PathVariable Long id, @RequestBody Usuario detalles) {
        return usuarioService.obtenerPorId(id).map(usuarioExistente -> {
            usuarioExistente.setNombre(detalles.getNombre());
            usuarioExistente.setApellido(detalles.getApellido());
            usuarioExistente.setCuil(detalles.getCuil());
            
            Usuario actualizado = usuarioService.guardarUsuario(usuarioExistente);
            return ResponseEntity.ok(actualizado);
        }).orElse(ResponseEntity.notFound().build());
    }

    // 5. BORRAR (Método DELETE)
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar usuario", description = "Elimina el usuario indicado si existe.")
    public ResponseEntity<Void> eliminarUsuario(@PathVariable Long id) {
        if (usuarioService.obtenerPorId(id).isPresent()) {
            usuarioService.eliminarUsuario(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}