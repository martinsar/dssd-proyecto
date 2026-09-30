package ar.edu.unlp.dssd.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import ar.edu.unlp.dssd.dto.ConvocatoriaRequest;
import ar.edu.unlp.dssd.model.Convocatoria;
import ar.edu.unlp.dssd.model.Emergencia;
import ar.edu.unlp.dssd.service.ConvocatoriaService;
import ar.edu.unlp.dssd.service.EmergenciaService;

@RestController
@RequestMapping("/api/emergencias")
public class EmergenciaController {

    @Autowired
    private EmergenciaService emergenciaService;

    @Autowired
    private ConvocatoriaService convocatoriaService;

    @PostMapping
    public ResponseEntity<Emergencia> crear(@RequestBody Emergencia emergencia) {
        // crear() guarda la emergencia y crea su caso en Bonita (todo o nada)
        Emergencia nueva = emergenciaService.crear(emergencia);
        return ResponseEntity.status(HttpStatus.CREATED).body(nueva);
    }

    @GetMapping
    public List<Emergencia> listar() {
        return emergenciaService.obtenerTodos();
    }

    // Path literal: Spring lo prefiere sobre "/{id}", asi que no choca con obtenerPorId
    @GetMapping("/pendientes")
    public List<Emergencia> listarPendientes() {
        return emergenciaService.obtenerPendientes();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Emergencia> obtenerPorId(@PathVariable Long id) {
        return emergenciaService.obtenerPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/convocatorias")
    public List<Convocatoria> listarConvocatorias(@PathVariable Long id) {
        return convocatoriaService.obtenerPorEmergencia(id);
    }

    @PostMapping("/{id}/convocatorias")
    public ResponseEntity<Convocatoria> crearConvocatoria(@PathVariable Long id,
                                                          @RequestBody ConvocatoriaRequest request) {
        // Relevamiento: crea la convocatoria con sus lotes y completa "Desglosar Lotes" en Bonita
        Convocatoria nueva = convocatoriaService.crearRelevamiento(id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(nueva);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Emergencia> actualizar(@PathVariable Long id, @RequestBody Emergencia detalles) {
        return emergenciaService.obtenerPorId(id).map(existente -> {
            // fechaCreacion, caseId y creadoPor NO se pisan con el body: los conserva el registro existente
            existente.setTipoDesastre(detalles.getTipoDesastre());
            existente.setNivelGravedad(detalles.getNivelGravedad());
            existente.setZonaAfectada(detalles.getZonaAfectada());
            existente.setDescripcion(detalles.getDescripcion());
            existente.setProyecto(detalles.getProyecto());

            Emergencia actualizado = emergenciaService.guardar(existente);
            return ResponseEntity.ok(actualizado);
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        if (emergenciaService.obtenerPorId(id).isPresent()) {
            emergenciaService.eliminar(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
