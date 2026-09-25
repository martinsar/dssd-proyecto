package ar.edu.unlp.dssd.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import ar.edu.unlp.dssd.model.Emergencia;
import ar.edu.unlp.dssd.service.BonitaIntegrationService;
import ar.edu.unlp.dssd.service.EmergenciaService;
// Importá el servicio de Bonita que hayas creado
// import ar.edu.unlp.dssd.service.BonitaIntegrationService; 

@RestController
@RequestMapping("/api/emergencias")
public class EmergenciaController {

    @Autowired
    private EmergenciaService emergenciaService;

    // 1. Inyectamos el servicio que se comunica con la API de Bonita
    @Autowired
    private BonitaIntegrationService bonitaIntegrationService;

    @PostMapping
    public ResponseEntity<?> crear(@RequestBody Emergencia emergencia) {
        // 2. Guardamos la emergencia en la base de datos (PostgreSQL)
        Emergencia nuevaEmergencia = emergenciaService.guardar(emergencia);

        // 3. Disparamos la instancia en Bonita enviando el contrato
        try {
            bonitaIntegrationService.iniciarProcesoEmergencia(
                nuevaEmergencia.getId(), // Asegurate de que el getter se llame así
                nuevaEmergencia.getTipoDesastre()
            );
        } catch (Exception e) {
            // Si Bonita falla, la emergencia ya quedó guardada en BD, 
            // pero le avisamos al frontend que hubo un problema con el proceso.
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Emergencia guardada, pero falló la conexión con Bonita: " + e.getMessage());
        }

        return ResponseEntity.ok(nuevaEmergencia);
    }

    @GetMapping
    public List<Emergencia> listar() {
        return emergenciaService.obtenerTodos();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Emergencia> obtenerPorId(@PathVariable Long id) {
        return emergenciaService.obtenerPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Emergencia> actualizar(@PathVariable Long id, @RequestBody Emergencia detalles) {
        return emergenciaService.obtenerPorId(id).map(existente -> {
            existente.setTipoDesastre(detalles.getTipoDesastre());
            existente.setNivelGravedad(detalles.getNivelGravedad());
            existente.setZonaAfectada(detalles.getZonaAfectada());
            existente.setFechaCreacion(detalles.getFechaCreacion());
            existente.setDescripcion(detalles.getDescripcion());
            existente.setProyecto(detalles.getProyecto());
            existente.setCreadoPor(detalles.getCreadoPor());
            
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