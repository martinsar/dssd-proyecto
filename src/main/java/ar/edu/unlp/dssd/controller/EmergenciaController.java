package ar.edu.unlp.dssd.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import ar.edu.unlp.dssd.dto.ConvocatoriaRequest;
import ar.edu.unlp.dssd.model.Convocatoria;
import ar.edu.unlp.dssd.model.Emergencia;
import ar.edu.unlp.dssd.service.ConvocatoriaService;
import ar.edu.unlp.dssd.service.EmergenciaService;

@RestController
@RequestMapping("/api/emergencias")
@Tag(name = "Emergencias", description = "Gestión de emergencias y consulta de sus convocatorias.")
public class EmergenciaController {

    @Autowired
    private EmergenciaService emergenciaService;

    @Autowired
    private ConvocatoriaService convocatoriaService;

    @PostMapping
    @Operation(summary = "Crear emergencia",
            description = "Registra una emergencia e inicia el proceso correspondiente en Bonita. Si la integración falla, la operación no se considera exitosa.")
    public ResponseEntity<Emergencia> crear(@RequestBody Emergencia emergencia) {
        // crear() guarda la emergencia y crea su caso en Bonita (todo o nada)
        Emergencia nueva = emergenciaService.crear(emergencia);
        return ResponseEntity.status(HttpStatus.CREATED).body(nueva);
    }

    @GetMapping
    @Operation(summary = "Listar emergencias", description = "Devuelve todas las emergencias registradas.")
    public List<Emergencia> listar() {
        return emergenciaService.obtenerTodos();
    }

    // Path literal: Spring lo prefiere sobre "/{id}", asi que no choca con obtenerPorId
    @GetMapping("/pendientes")
    @Operation(summary = "Listar emergencias pendientes",
            description = "Devuelve las emergencias pendientes de gestión según su estado de proceso.")
    public List<Emergencia> listarPendientes() {
        return emergenciaService.obtenerPendientes();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener emergencia", description = "Busca una emergencia por su identificador.")
    public ResponseEntity<Emergencia> obtenerPorId(@PathVariable Long id) {
        return emergenciaService.obtenerPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/convocatorias")
    @Operation(summary = "Listar convocatorias de una emergencia",
            description = "Devuelve las convocatorias asociadas a la emergencia. Si no tiene convocatorias, devuelve una lista vacía.")
    public List<Convocatoria> listarConvocatorias(@PathVariable Long id) {
        return convocatoriaService.obtenerPorEmergencia(id);
    }

    @PostMapping("/{id}/convocatorias")
    @Operation(summary = "Crear relevamiento de emergencia",
            description = "Crea una convocatoria en BORRADOR junto con sus lotes y completa la tarea de relevamiento en Bonita. La operación es transaccional: ante un error no queda guardada parcialmente.")
    public ResponseEntity<Convocatoria> crearConvocatoria(@PathVariable Long id,
                                                          @RequestBody ConvocatoriaRequest request) {
        // Relevamiento: crea la convocatoria con sus lotes y completa "Desglosar Lotes" en Bonita
        Convocatoria nueva = convocatoriaService.crearRelevamiento(id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(nueva);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar emergencia",
            description = "Actualiza los datos descriptivos de una emergencia existente. Conserva la fecha de creación, el identificador de caso y el usuario que la creó.")
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
    @Operation(summary = "Eliminar emergencia", description = "Elimina la emergencia indicada si existe.")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        if (emergenciaService.obtenerPorId(id).isPresent()) {
            emergenciaService.eliminar(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
