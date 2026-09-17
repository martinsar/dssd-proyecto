package ar.edu.unlp.dssd.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import ar.edu.unlp.dssd.model.Emergencia;
import ar.edu.unlp.dssd.service.EmergenciaService;

@RestController
@RequestMapping("/api/emergencias")
public class EmergenciaController {

    @Autowired
    private EmergenciaService emergenciaService;

    @PostMapping
    public Emergencia crear(@RequestBody Emergencia emergencia) {
        return emergenciaService.guardar(emergencia);
    }

    @GetMapping
    public List<Emergencia> listar() {
        return emergenciaService.obtenerTodos();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Emergencia> obtenerPorId(@PathVariable Long id) {
        return emergenciaService.obtenerPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Emergencia> actualizar(@PathVariable Long id, @RequestBody Emergencia detalles) {
        return emergenciaService.obtenerPorId(id).map(existente -> {
            existente.setTipoDesastre(detalles.getTipoDesastre());
            existente.setNivelGravedad(detalles.getNivelGravedad());
            existente.setZonaAfectada(detalles.getZonaAfectada());
            existente.setFechaCreacion(detalles.getFechaCreacion());
            existente.setDescripcion(detalles.getDescripcion());
            existente.setProyecto(detalles.getProyecto());
            existente.setCreadoPor(detalles.getCreadoPor());
            
            Emergencia actualizado = emergenciaService.guardar(existente);
            return ResponseEntity.ok(actualizado);
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        if (emergenciaService.obtenerPorId(id).isPresent()) {
            emergenciaService.eliminar(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}