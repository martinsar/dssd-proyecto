package ar.edu.unlp.dssd.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ar.edu.unlp.dssd.model.Oferta;
import ar.edu.unlp.dssd.service.OfertaService;

@RestController
@RequestMapping("/api/ofertas")
public class OfertaController {

    @Autowired
    private OfertaService ofertaService;

    // 1. CREAR (POST)
    @PostMapping
    public Oferta crear(@RequestBody Oferta oferta) {
        return ofertaService.guardar(oferta);
    }

    // 2. LISTAR TODOS (GET)
    @GetMapping
    public List<Oferta> listar() {
        return ofertaService.obtenerTodos();
    }

    // 3. OBTENER POR ID (GET)
    @GetMapping("/{id}")
    public ResponseEntity<Oferta> obtenerPorId(@PathVariable Long id) {
        return ofertaService.obtenerPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // 4. ACTUALIZAR (PUT)
    @PutMapping("/{id}")
    public ResponseEntity<Oferta> actualizar(@PathVariable Long id, @RequestBody Oferta detalles) {
        return ofertaService.obtenerPorId(id).map(existente -> {
            existente.setCantidad(detalles.getCantidad());
            existente.setDescripcion(detalles.getDescripcion());
            existente.setEstado(detalles.getEstado());
            existente.setTipo(detalles.getTipo());
            existente.setFechaCreacion(detalles.getFechaCreacion());
            existente.setFechaValidacion(detalles.getFechaValidacion());
            existente.setNivelAprobacion(detalles.getNivelAprobacion());
            existente.setPerfilCompetencia(detalles.getPerfilCompetencia());
            existente.setOrganizacion(detalles.getOrganizacion());
            existente.setTipoRecurso(detalles.getTipoRecurso());
            
            Oferta actualizado = ofertaService.guardar(existente);
            return ResponseEntity.ok(actualizado);
        }).orElse(ResponseEntity.notFound().build());
    }

    // 5. ELIMINAR (DELETE)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        if (ofertaService.obtenerPorId(id).isPresent()) {
            ofertaService.eliminar(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}