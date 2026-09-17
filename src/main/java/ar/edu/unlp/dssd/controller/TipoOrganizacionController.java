package ar.edu.unlp.dssd.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ar.edu.unlp.dssd.model.TipoOrganizacion;
import ar.edu.unlp.dssd.service.TipoOrganizacionService;

@RestController
@RequestMapping("/api/tipos-organizacion")
public class TipoOrganizacionController {

    @Autowired
    private TipoOrganizacionService tipoOrganizacionService;

    @PostMapping
    public TipoOrganizacion crear(@RequestBody TipoOrganizacion tipo) {
        return tipoOrganizacionService.guardar(tipo);
    }

    @GetMapping
    public List<TipoOrganizacion> listar() {
        return tipoOrganizacionService.obtenerTodos();
    }

    @GetMapping("/{id}")
    public ResponseEntity<TipoOrganizacion> obtenerPorId(@PathVariable Long id) {
        return tipoOrganizacionService.obtenerPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<TipoOrganizacion> actualizar(@PathVariable Long id, @RequestBody TipoOrganizacion detalles) {
        return tipoOrganizacionService.obtenerPorId(id).map(existente -> {
            existente.setNombre(detalles.getNombre());
            TipoOrganizacion actualizado = tipoOrganizacionService.guardar(existente);
            return ResponseEntity.ok(actualizado);
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        if (tipoOrganizacionService.obtenerPorId(id).isPresent()) {
            tipoOrganizacionService.eliminar(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}