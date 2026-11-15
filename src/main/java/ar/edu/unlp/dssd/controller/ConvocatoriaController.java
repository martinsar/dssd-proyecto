package ar.edu.unlp.dssd.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import ar.edu.unlp.dssd.dto.NecesidadRequest;
import ar.edu.unlp.dssd.dto.PublicacionRequest;
import ar.edu.unlp.dssd.model.Convocatoria;
import ar.edu.unlp.dssd.model.Necesidad;
import ar.edu.unlp.dssd.seguridad.AccesoIntegracion;
import ar.edu.unlp.dssd.seguridad.Rol;
import ar.edu.unlp.dssd.seguridad.RolesPermitidos;
import ar.edu.unlp.dssd.service.ConvocatoriaService;
import ar.edu.unlp.dssd.service.NecesidadService;

@RestController
@RequestMapping("/api/convocatorias")
@Tag(name = "Convocatorias", description = "Consulta, edición y publicación de convocatorias y sus lotes.")
@RolesPermitidos(Rol.COORDINADOR) // [Entrega final] Perfil por defecto
public class ConvocatoriaController {

    @Autowired
    private ConvocatoriaService convocatoriaService;

    @Autowired
    private NecesidadService necesidadService;

    @GetMapping("/{id}/necesidades")
    @RolesPermitidos({Rol.COORDINADOR, Rol.ONG})
    @Operation(summary = "Listar lotes de una convocatoria",
            description = "Devuelve las necesidades asociadas a la convocatoria indicada.")
    public List<Necesidad> listarNecesidades(@PathVariable Long id) {
        return necesidadService.obtenerPorConvocatoria(id);
    }

    @PostMapping("/{id}/necesidades")
    @Operation(summary = "Agregar lote a una convocatoria",
            description = "Agrega una necesidad a una convocatoria que todavía está en estado BORRADOR.")
    public ResponseEntity<Necesidad> agregarNecesidad(@PathVariable Long id,
                                                      @RequestBody NecesidadRequest request) {
        Necesidad nueva = necesidadService.agregar(id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(nueva);
    }

    @DeleteMapping("/{id}/necesidades/{necesidadId}")
    @Operation(summary = "Quitar lote de una convocatoria",
            description = "Elimina el lote indicado si pertenece a la convocatoria y esta sigue en estado BORRADOR.")
    public ResponseEntity<Void> quitarNecesidad(@PathVariable Long id, @PathVariable Long necesidadId) {
        necesidadService.quitar(id, necesidadId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/ofertas/validacion")
    @AccesoIntegracion
    @Operation(summary = "Validar ofertas de una convocatoria",
            description = "Indica si las ofertas registradas para la convocatoria cumplen las necesidades asociadas.")
    public ResponseEntity<Boolean> validarOfertas(@PathVariable Long id) {
        return ResponseEntity.ok(convocatoriaService.validarOfertas(id));
    }

    @PostMapping("/{id}/publicar")
    @Operation(summary = "Publicar convocatoria",
            description = "Publica una convocatoria en BORRADOR. Requiere al menos un lote y una fecha de cierre futura; la fecha de apertura es opcional y, si se omite, se usa la fecha y hora actuales. La publicación también avanza el proceso asociado en Bonita.")
    public Convocatoria publicar(@PathVariable Long id, @RequestBody PublicacionRequest request) {
        return convocatoriaService.publicar(id, request);
    }

    @GetMapping
    @RolesPermitidos({Rol.COORDINADOR, Rol.ONG})
    @Operation(summary = "Listar convocatorias", description = "Devuelve todas las convocatorias registradas.")
    public List<Convocatoria> listar() {
        return convocatoriaService.obtenerTodos();
    }

    @GetMapping("/{id}")
    @RolesPermitidos({Rol.COORDINADOR, Rol.ONG})
    @Operation(summary = "Obtener convocatoria", description = "Busca una convocatoria por su identificador.")
    public ResponseEntity<Convocatoria> obtenerPorId(@PathVariable Long id) {
        return convocatoriaService.obtenerPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar fechas de convocatoria",
            description = "Actualiza únicamente las fechas de apertura y cierre de una convocatoria en estado BORRADOR. El estado y la emergencia asociada no se modifican.")
    public ResponseEntity<Convocatoria> actualizar(@PathVariable Long id, @RequestBody Convocatoria detalles) {
        // Del body solo se toman las fechas; estado y emergencia no se pueden cambiar por aca
        return ResponseEntity.ok(convocatoriaService.actualizarFechas(id,
                detalles.getFechaApertura(), detalles.getFechaCierre()));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar convocatoria", description = "Elimina la convocatoria indicada si existe.")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        if (convocatoriaService.obtenerPorId(id).isPresent()) {
            convocatoriaService.eliminar(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}