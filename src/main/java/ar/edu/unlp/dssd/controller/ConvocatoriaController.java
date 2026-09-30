package ar.edu.unlp.dssd.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ar.edu.unlp.dssd.dto.NecesidadRequest;
import ar.edu.unlp.dssd.dto.PublicacionRequest;
import ar.edu.unlp.dssd.model.Convocatoria;
import ar.edu.unlp.dssd.model.Necesidad;
import ar.edu.unlp.dssd.service.ConvocatoriaService;
import ar.edu.unlp.dssd.service.NecesidadService;

@RestController
@RequestMapping("/api/convocatorias")
public class ConvocatoriaController {

    @Autowired
    private ConvocatoriaService convocatoriaService;

    @Autowired
    private NecesidadService necesidadService;

    @GetMapping("/{id}/necesidades")
    public List<Necesidad> listarNecesidades(@PathVariable Long id) {
        return necesidadService.obtenerPorConvocatoria(id);
    }

    @PostMapping("/{id}/necesidades")
    public ResponseEntity<Necesidad> agregarNecesidad(@PathVariable Long id,
                                                      @RequestBody NecesidadRequest request) {
        Necesidad nueva = necesidadService.agregar(id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(nueva);
    }

    @DeleteMapping("/{id}/necesidades/{necesidadId}")
    public ResponseEntity<Void> quitarNecesidad(@PathVariable Long id, @PathVariable Long necesidadId) {
        necesidadService.quitar(id, necesidadId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/publicar")
    public Convocatoria publicar(@PathVariable Long id, @RequestBody PublicacionRequest request) {
        return convocatoriaService.publicar(id, request);
    }

    @GetMapping
    public List<Convocatoria> listar() {
        return convocatoriaService.obtenerTodos();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Convocatoria> obtenerPorId(@PathVariable Long id) {
        return convocatoriaService.obtenerPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Convocatoria> actualizar(@PathVariable Long id, @RequestBody Convocatoria detalles) {
        // Del body solo se toman las fechas; estado y emergencia no se pueden cambiar por aca
        return ResponseEntity.ok(convocatoriaService.actualizarFechas(id,
                detalles.getFechaApertura(), detalles.getFechaCierre()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        if (convocatoriaService.obtenerPorId(id).isPresent()) {
            convocatoriaService.eliminar(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}