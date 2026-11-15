package ar.edu.unlp.dssd.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import ar.edu.unlp.dssd.model.Organizacion;
import ar.edu.unlp.dssd.seguridad.Rol;
import ar.edu.unlp.dssd.seguridad.RolesPermitidos;
import ar.edu.unlp.dssd.service.OrganizacionService;

@RestController
@RequestMapping("/api/organizaciones")
@Tag(name = "Organizaciones", description = "Administración de organizaciones participantes.")
public class OrganizacionController {

    @Autowired
    private OrganizacionService organizacionService;

    // 1. CREAR (POST)
    @PostMapping
    @RolesPermitidos(Rol.COORDINADOR) // [Entrega final] Escritura solo del coordinador
    @Operation(summary = "Crear organización", description = "Registra una organización.")
    public ResponseEntity<Organizacion> crear(@RequestBody Organizacion organizacion) {
        return ResponseEntity.status(HttpStatus.CREATED).body(organizacionService.guardar(organizacion));
    }

    // 2. LISTAR TODOS (GET)
    @GetMapping
    @Operation(summary = "Listar organizaciones", description = "Devuelve todas las organizaciones registradas.")
    public List<Organizacion> listar() {
        return organizacionService.obtenerTodos();
    }

    // 3. OBTENER POR ID (GET)
    @GetMapping("/{id}")
    @Operation(summary = "Obtener organización", description = "Busca una organización por su identificador.")
    public ResponseEntity<Organizacion> obtenerPorId(@PathVariable Long id) {
        return organizacionService.obtenerPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // 4. ACTUALIZAR (PUT)
    @PutMapping("/{id}")
    @RolesPermitidos(Rol.COORDINADOR)
    @Operation(summary = "Actualizar organización",
            description = "Actualiza los datos de una organización existente.")
    public ResponseEntity<Organizacion> actualizar(@PathVariable Long id, @RequestBody Organizacion detalles) {
        return organizacionService.obtenerPorId(id).map(existente -> {
            existente.setNombre(detalles.getNombre());
            existente.setCodigoOng(detalles.getCodigoOng());
            existente.setTipo(detalles.getTipo());
            existente.setObjetoSocial(detalles.getObjetoSocial());
            existente.setFechaCreacion(detalles.getFechaCreacion());
            existente.setRegistradaPor(detalles.getRegistradaPor());
            existente.setValidadoPor(detalles.getValidadoPor());
            
            Organizacion actualizado = organizacionService.guardar(existente);
            return ResponseEntity.ok(actualizado);
        }).orElse(ResponseEntity.notFound().build());
    }

    // 5. ELIMINAR (DELETE)
    @DeleteMapping("/{id}")
    @RolesPermitidos(Rol.COORDINADOR)
    @Operation(summary = "Eliminar organización", description = "Elimina la organización indicada si existe.")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        if (organizacionService.obtenerPorId(id).isPresent()) {
            organizacionService.eliminar(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}