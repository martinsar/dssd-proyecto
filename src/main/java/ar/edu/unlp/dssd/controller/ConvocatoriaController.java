package ar.edu.unlp.dssd.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ar.edu.unlp.dssd.model.Convocatoria;
import ar.edu.unlp.dssd.service.ConvocatoriaService;

@RestController
@RequestMapping("/api/convocatorias")
public class ConvocatoriaController {

    @Autowired
    private ConvocatoriaService convocatoriaService;

    @PostMapping
    public Convocatoria crear(@RequestBody Convocatoria convocatoria) {
        return convocatoriaService.guardar(convocatoria);
    }

    @GetMapping
    public List<Convocatoria> listar() {
        return convocatoriaService.obtenerTodos();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Convocatoria> obtenerPorId(@PathVariable Long id) {
        return convocatoriaService.obtenerPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Convocatoria> actualizar(@PathVariable Long id, @RequestBody Convocatoria detalles) {
        return convocatoriaService.obtenerPorId(id).map(existente -> {
            existente.setEstado(detalles.getEstado());
            existente.setFechaCreacion(detalles.getFechaCreacion());
            existente.setFechaApertura(detalles.getFechaApertura());
            existente.setFechaCierre(detalles.getFechaCierre());
            existente.setEmergencia(detalles.getEmergencia());
            
            Convocatoria actualizado = convocatoriaService.guardar(existente);
            return ResponseEntity.ok(actualizado);
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        if (convocatoriaService.obtenerPorId(id).isPresent()) {
            convocatoriaService.eliminar(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}