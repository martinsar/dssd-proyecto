package ar.edu.unlp.dssd.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ar.edu.unlp.dssd.model.Necesidad;
import ar.edu.unlp.dssd.service.NecesidadService;

@RestController
@RequestMapping("/api/necesidades")
public class NecesidadController {

    @Autowired
    private NecesidadService necesidadService;

    @PostMapping
    public Necesidad crear(@RequestBody Necesidad necesidad) {
        return necesidadService.guardar(necesidad);
    }

    @GetMapping
    public List<Necesidad> listar() {
        return necesidadService.obtenerTodos();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Necesidad> obtenerPorId(@PathVariable Long id) {
        return necesidadService.obtenerPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Necesidad> actualizar(@PathVariable Long id, @RequestBody Necesidad detalles) {
        return necesidadService.obtenerPorId(id).map(existente -> {
            existente.setFechaCreacion(detalles.getFechaCreacion());
            existente.setDescripcion(detalles.getDescripcion());
            existente.setCantidad(detalles.getCantidad());
            existente.setConvocatoria(detalles.getConvocatoria());
            existente.setTipoRecurso(detalles.getTipoRecurso());
            
            Necesidad actualizado = necesidadService.guardar(existente);
            return ResponseEntity.ok(actualizado);
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        if (necesidadService.obtenerPorId(id).isPresent()) {
            necesidadService.eliminar(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}