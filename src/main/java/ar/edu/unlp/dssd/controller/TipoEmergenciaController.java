package ar.edu.unlp.dssd.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import ar.edu.unlp.dssd.model.TipoEmergencia;
import ar.edu.unlp.dssd.service.TipoEmergenciaService;

@RestController
@RequestMapping("/api/tipos-emergencia")
public class TipoEmergenciaController {

    @Autowired
    private TipoEmergenciaService tipoEmergenciaService;

    @PostMapping
    public TipoEmergencia crear(@RequestBody TipoEmergencia tipoEmergencia) {
        return tipoEmergenciaService.guardar(tipoEmergencia);
    }

    @GetMapping
    public List<TipoEmergencia> listar() {
        return tipoEmergenciaService.obtenerTodos();
    }

    @GetMapping("/{id}")
    public ResponseEntity<TipoEmergencia> obtenerPorId(@PathVariable Long id) {
        return tipoEmergenciaService.obtenerPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<TipoEmergencia> actualizar(@PathVariable Long id, @RequestBody TipoEmergencia detalles) {
        return tipoEmergenciaService.obtenerPorId(id).map(existente -> {
            existente.setTipoDesastre(detalles.getTipoDesastre());
            TipoEmergencia actualizado = tipoEmergenciaService.guardar(existente);
            return ResponseEntity.ok(actualizado);
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        if (tipoEmergenciaService.obtenerPorId(id).isPresent()) {
            tipoEmergenciaService.eliminar(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}