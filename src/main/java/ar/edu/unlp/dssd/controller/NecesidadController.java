package ar.edu.unlp.dssd.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import ar.edu.unlp.dssd.model.Necesidad;
import ar.edu.unlp.dssd.service.NecesidadService;

@RestController
@RequestMapping("/api/necesidades")
@Tag(name = "Necesidades", description = "Administración de lotes de recursos asociados a convocatorias.")
public class NecesidadController {

    @Autowired
    private NecesidadService necesidadService;

    @PostMapping
    @Operation(summary = "Crear necesidad",
            description = "Crea un lote asociado a una convocatoria existente en estado BORRADOR. La convocatoria debe indicarse en el cuerpo.")
    public ResponseEntity<Necesidad> crear(@RequestBody Necesidad necesidad) {
        return ResponseEntity.status(HttpStatus.CREATED).body(necesidadService.guardar(necesidad));
    }

    @GetMapping
    @Operation(summary = "Listar necesidades", description = "Devuelve todas las necesidades registradas.")
    public List<Necesidad> listar() {
        return necesidadService.obtenerTodos();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener necesidad",
            description = "Busca una necesidad por su identificador.")
    public ResponseEntity<Necesidad> obtenerPorId(@PathVariable Long id) {
        return necesidadService.obtenerPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar necesidad",
            description = "Actualiza un lote. La convocatoria actual y la de destino, si cambia, deben estar en estado BORRADOR.")
    public ResponseEntity<Necesidad> actualizar(@PathVariable Long id, @RequestBody Necesidad detalles) {
        // Las reglas (convocatoria en BORRADOR, 404 si no existe) viven en el servicio
        return ResponseEntity.ok(necesidadService.actualizar(id, detalles));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar necesidad",
            description = "Elimina un lote únicamente si su convocatoria está en estado BORRADOR.")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        necesidadService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}