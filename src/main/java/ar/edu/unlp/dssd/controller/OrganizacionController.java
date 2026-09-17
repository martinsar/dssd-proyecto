package ar.edu.unlp.dssd.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ar.edu.unlp.dssd.model.Organizacion;
import ar.edu.unlp.dssd.service.OrganizacionService;

@RestController
@RequestMapping("/api/organizaciones")
public class OrganizacionController {

    @Autowired
    private OrganizacionService organizacionService;

    // 1. CREAR (POST)
    @PostMapping
    public Organizacion crear(@RequestBody Organizacion organizacion) {
        return organizacionService.guardar(organizacion);
    }

    // 2. LISTAR TODOS (GET)
    @GetMapping
    public List<Organizacion> listar() {
        return organizacionService.obtenerTodos();
    }

    // 3. OBTENER POR ID (GET)
    @GetMapping("/{id}")
    public ResponseEntity<Organizacion> obtenerPorId(@PathVariable Long id) {
        return organizacionService.obtenerPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // 4. ACTUALIZAR (PUT)
    @PutMapping("/{id}")
    public ResponseEntity<Organizacion> actualizar(@PathVariable Long id, @RequestBody Organizacion detalles) {
        return organizacionService.obtenerPorId(id).map(existente -> {
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
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        if (organizacionService.obtenerPorId(id).isPresent()) {
            organizacionService.eliminar(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}