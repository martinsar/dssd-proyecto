package ar.edu.unlp.dssd.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import ar.edu.unlp.dssd.model.TipoEmergencia;
import ar.edu.unlp.dssd.seguridad.Rol;
import ar.edu.unlp.dssd.seguridad.RolesPermitidos;
import ar.edu.unlp.dssd.service.TipoEmergenciaService;

@RestController
@RequestMapping("/api/tipos-emergencia")
@Tag(name = "Tipos de emergencia", description = "Catálogo de tipos de desastre.")
public class TipoEmergenciaController {

    @Autowired
    private TipoEmergenciaService tipoEmergenciaService;

    @PostMapping
    @RolesPermitidos(Rol.COORDINADOR) // [Entrega final] Escritura solo del coordinador
    @Operation(summary = "Crear tipo de emergencia", description = "Agrega un tipo de desastre al catálogo.")
    public ResponseEntity<TipoEmergencia> crear(@RequestBody TipoEmergencia tipoEmergencia) {
        return ResponseEntity.status(HttpStatus.CREATED).body(tipoEmergenciaService.guardar(tipoEmergencia));
    }

    @GetMapping
    @Operation(summary = "Listar tipos de emergencia", description = "Devuelve los tipos de desastre disponibles.")
    public List<TipoEmergencia> listar() {
        return tipoEmergenciaService.obtenerTodos();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener tipo de emergencia", description = "Busca un tipo de desastre por su identificador.")
    public ResponseEntity<TipoEmergencia> obtenerPorId(@PathVariable Long id) {
        return tipoEmergenciaService.obtenerPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @RolesPermitidos(Rol.COORDINADOR)
    @Operation(summary = "Actualizar tipo de emergencia", description = "Actualiza el nombre del tipo de desastre indicado.")
    public ResponseEntity<TipoEmergencia> actualizar(@PathVariable Long id, @RequestBody TipoEmergencia detalles) {
        return tipoEmergenciaService.obtenerPorId(id).map(existente -> {
            existente.setTipoDesastre(detalles.getTipoDesastre());
            TipoEmergencia actualizado = tipoEmergenciaService.guardar(existente);
            return ResponseEntity.ok(actualizado);
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @RolesPermitidos(Rol.COORDINADOR)
    @Operation(summary = "Eliminar tipo de emergencia", description = "Elimina el tipo de desastre indicado si existe.")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        if (tipoEmergenciaService.obtenerPorId(id).isPresent()) {
            tipoEmergenciaService.eliminar(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}