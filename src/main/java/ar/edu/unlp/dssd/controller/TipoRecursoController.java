package ar.edu.unlp.dssd.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import ar.edu.unlp.dssd.model.TipoRecurso;
import ar.edu.unlp.dssd.service.TipoRecursoService;

@RestController
@RequestMapping("/api/tipos-recurso")
public class TipoRecursoController {

    @Autowired
    private TipoRecursoService tipoRecursoService;

    // 1. CREAR (Método POST)
    @PostMapping
    public TipoRecurso crearTipoRecurso(@RequestBody TipoRecurso tipoRecurso) {
        return tipoRecursoService.guardar(tipoRecurso);
    }

    // 2. LEER TODOS (Método GET)
    @GetMapping
    public List<TipoRecurso> listarTiposRecurso() {
        return tipoRecursoService.obtenerTodos();
    }

    // 3. LEER UNO (Método GET)
    @GetMapping("/{id}")
    public ResponseEntity<TipoRecurso> obtenerTipoRecurso(@PathVariable Long id) {
        return tipoRecursoService.obtenerPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // 4. ACTUALIZAR (Método PUT)
    @PutMapping("/{id}")
    public ResponseEntity<TipoRecurso> actualizarTipoRecurso(@PathVariable Long id, @RequestBody TipoRecurso detalles) {
        return tipoRecursoService.obtenerPorId(id).map(tipoExistente -> {
            tipoExistente.setNombre(detalles.getNombre());
            
            TipoRecurso actualizado = tipoRecursoService.guardar(tipoExistente);
            return ResponseEntity.ok(actualizado);
        }).orElse(ResponseEntity.notFound().build());
    }

    // 5. BORRAR (Método DELETE)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarTipoRecurso(@PathVariable Long id) {
        if (tipoRecursoService.obtenerPorId(id).isPresent()) {
            tipoRecursoService.eliminar(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}