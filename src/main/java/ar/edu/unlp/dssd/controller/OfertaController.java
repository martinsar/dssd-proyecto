package ar.edu.unlp.dssd.controller;

import java.util.List;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import ar.edu.unlp.dssd.dto.OfertaRequest;
import ar.edu.unlp.dssd.model.Oferta;
import ar.edu.unlp.dssd.service.OfertaService;

@RestController
@RequestMapping("/api/ofertas")
@Tag(name = "Ofertas", description = "Administración de ofertas de recursos realizadas por organizaciones.")
public class OfertaController {

    @Autowired
    private OfertaService ofertaService;

    @PostMapping
    @Operation(summary = "Crear oferta",
            description = "Registra una oferta vinculada a una convocatoria, organización y tipo de recurso existentes.")
    public ResponseEntity<Oferta> crear(@Valid @RequestBody OfertaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ofertaService.crear(request));
    }

    @GetMapping
    @Operation(summary = "Listar ofertas", description = "Devuelve todas las ofertas registradas.")
    public List<Oferta> listar() {
        return ofertaService.obtenerTodos();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener oferta", description = "Busca una oferta por su identificador.")
    public ResponseEntity<Oferta> obtenerPorId(@PathVariable Long id) {
        return ofertaService.obtenerPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar oferta",
            description = "Actualiza los datos de una oferta existente manteniendo su identificador.")
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

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar oferta", description = "Elimina la oferta indicada si existe.")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        if (ofertaService.obtenerPorId(id).isPresent()) {
            ofertaService.eliminar(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
