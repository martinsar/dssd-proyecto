package ar.edu.unlp.dssd.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import ar.edu.unlp.dssd.model.TipoRecurso;
import ar.edu.unlp.dssd.service.TipoRecursoService;

@RestController
@RequestMapping("/api/tipos-recurso")
@Tag(name = "Tipos de recurso", description = "Catálogo de recursos que pueden solicitarse u ofrecerse.")
public class TipoRecursoController {

    @Autowired
    private TipoRecursoService tipoRecursoService;

    // 1. CREAR (Método POST)
    @PostMapping
    @Operation(summary = "Crear tipo de recurso", description = "Agrega un tipo de recurso al catálogo.")
    public ResponseEntity<TipoRecurso> crearTipoRecurso(@RequestBody TipoRecurso tipoRecurso) {
        return ResponseEntity.status(HttpStatus.CREATED).body(tipoRecursoService.guardar(tipoRecurso));
    }

    // 2. LEER TODOS (Método GET)
    @GetMapping
    @Operation(summary = "Listar tipos de recurso", description = "Devuelve los tipos de recurso disponibles.")
    public List<TipoRecurso> listarTiposRecurso() {
        return tipoRecursoService.obtenerTodos();
    }

    // 3. LEER UNO (Método GET)
    @GetMapping("/{id}")
    @Operation(summary = "Obtener tipo de recurso", description = "Busca un tipo de recurso por su identificador.")
    public ResponseEntity<TipoRecurso> obtenerTipoRecurso(@PathVariable Long id) {
        return tipoRecursoService.obtenerPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // 4. ACTUALIZAR (Método PUT)
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar tipo de recurso", description = "Actualiza el nombre del tipo de recurso indicado.")
    public ResponseEntity<TipoRecurso> actualizarTipoRecurso(@PathVariable Long id, @RequestBody TipoRecurso detalles) {
        return tipoRecursoService.obtenerPorId(id).map(tipoExistente -> {
            tipoExistente.setNombre(detalles.getNombre());
            
            TipoRecurso actualizado = tipoRecursoService.guardar(tipoExistente);
            return ResponseEntity.ok(actualizado);
        }).orElse(ResponseEntity.notFound().build());
    }

    // 5. BORRAR (Método DELETE)
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar tipo de recurso", description = "Elimina el tipo de recurso indicado si existe.")
    public ResponseEntity<Void> eliminarTipoRecurso(@PathVariable Long id) {
        if (tipoRecursoService.obtenerPorId(id).isPresent()) {
            tipoRecursoService.eliminar(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}