package ar.edu.unlp.dssd.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import ar.edu.unlp.dssd.model.TipoOrganizacion;
import ar.edu.unlp.dssd.seguridad.Rol;
import ar.edu.unlp.dssd.seguridad.RolesPermitidos;
import ar.edu.unlp.dssd.service.TipoOrganizacionService;

@RestController
@RequestMapping("/api/tipos-organizacion")
@Tag(name = "Tipos de organización", description = "Catálogo de tipos de organización.")
public class TipoOrganizacionController {

    @Autowired
    private TipoOrganizacionService tipoOrganizacionService;

    @PostMapping
    @RolesPermitidos(Rol.COORDINADOR) // [Entrega final] Escritura solo del coordinador
    @Operation(summary = "Crear tipo de organización", description = "Agrega un tipo de organización al catálogo.")
    public ResponseEntity<TipoOrganizacion> crear(@RequestBody TipoOrganizacion tipo) {
        return ResponseEntity.status(HttpStatus.CREATED).body(tipoOrganizacionService.guardar(tipo));
    }

    @GetMapping
    @Operation(summary = "Listar tipos de organización", description = "Devuelve los tipos de organización disponibles.")
    public List<TipoOrganizacion> listar() {
        return tipoOrganizacionService.obtenerTodos();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener tipo de organización", description = "Busca un tipo de organización por su identificador.")
    public ResponseEntity<TipoOrganizacion> obtenerPorId(@PathVariable Long id) {
        return tipoOrganizacionService.obtenerPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @RolesPermitidos(Rol.COORDINADOR)
    @Operation(summary = "Actualizar tipo de organización", description = "Actualiza el nombre del tipo de organización indicado.")
    public ResponseEntity<TipoOrganizacion> actualizar(@PathVariable Long id, @RequestBody TipoOrganizacion detalles) {
        return tipoOrganizacionService.obtenerPorId(id).map(existente -> {
            existente.setNombre(detalles.getNombre());
            TipoOrganizacion actualizado = tipoOrganizacionService.guardar(existente);
            return ResponseEntity.ok(actualizado);
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @RolesPermitidos(Rol.COORDINADOR)
    @Operation(summary = "Eliminar tipo de organización", description = "Elimina el tipo de organización indicado si existe.")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        if (tipoOrganizacionService.obtenerPorId(id).isPresent()) {
            tipoOrganizacionService.eliminar(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}