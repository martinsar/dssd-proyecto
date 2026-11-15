package ar.edu.unlp.dssd.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import ar.edu.unlp.dssd.model.Proyecto;
import ar.edu.unlp.dssd.seguridad.Rol;
import ar.edu.unlp.dssd.seguridad.RolesPermitidos;
import ar.edu.unlp.dssd.service.ProyectoService;

@RestController
@RequestMapping("/api/proyectos")
@Tag(name = "Proyectos", description = "Administración de proyectos asociados a emergencias.")
public class ProyectoController {

    @Autowired
    private ProyectoService proyectoService;

    @PostMapping
    @RolesPermitidos(Rol.COORDINADOR) // [Entrega final] Escritura solo del coordinador
    @Operation(summary = "Crear proyecto", description = "Registra un proyecto.")
    public ResponseEntity<Proyecto> crear(@RequestBody Proyecto proyecto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(proyectoService.guardar(proyecto));
    }

    @GetMapping
    @Operation(summary = "Listar proyectos", description = "Devuelve todos los proyectos registrados.")
    public List<Proyecto> listar() {
        return proyectoService.obtenerTodos();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener proyecto", description = "Busca un proyecto por su identificador.")
    public ResponseEntity<Proyecto> obtenerPorId(@PathVariable Long id) {
        return proyectoService.obtenerPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @RolesPermitidos(Rol.COORDINADOR)
    @Operation(summary = "Actualizar proyecto", description = "Actualiza el nombre y estado de un proyecto existente.")
    public ResponseEntity<Proyecto> actualizar(@PathVariable Long id, @RequestBody Proyecto detalles) {
        return proyectoService.obtenerPorId(id).map(existente -> {
            existente.setNombre(detalles.getNombre());
            existente.setEstado(detalles.getEstado());
            
            Proyecto actualizado = proyectoService.guardar(existente);
            return ResponseEntity.ok(actualizado);
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @RolesPermitidos(Rol.COORDINADOR)
    @Operation(summary = "Eliminar proyecto", description = "Elimina el proyecto indicado si existe.")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        if (proyectoService.obtenerPorId(id).isPresent()) {
            proyectoService.eliminar(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}