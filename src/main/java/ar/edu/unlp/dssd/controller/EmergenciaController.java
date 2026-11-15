package ar.edu.unlp.dssd.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import ar.edu.unlp.dssd.dto.ConfirmacionAdjudicacionResponse;
import ar.edu.unlp.dssd.dto.ConvocatoriaRequest;
import ar.edu.unlp.dssd.dto.OfertasConsolidadasDto;
import ar.edu.unlp.dssd.dto.RegistroValidacionesResponse;
import ar.edu.unlp.dssd.dto.ValidacionesRequest;
import ar.edu.unlp.dssd.model.Convocatoria;
import ar.edu.unlp.dssd.model.Emergencia;
import ar.edu.unlp.dssd.model.EstadoOferta;
import ar.edu.unlp.dssd.seguridad.AccesoIntegracion;
import ar.edu.unlp.dssd.seguridad.Rol;
import ar.edu.unlp.dssd.seguridad.RolesPermitidos;
import ar.edu.unlp.dssd.seguridad.SesionUsuario;
import ar.edu.unlp.dssd.service.ConvocatoriaService;
import ar.edu.unlp.dssd.service.EmergenciaService;
import ar.edu.unlp.dssd.service.IntegracionService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/emergencias")
@Tag(name = "Emergencias", description = "Gestión de emergencias y consulta de sus convocatorias.")
@RolesPermitidos({Rol.OPERADOR_MUNICIPAL, Rol.COORDINADOR}) // [Entrega final] Perfiles por defecto
public class EmergenciaController {

    @Autowired
    private EmergenciaService emergenciaService;

    @Autowired
    private ConvocatoriaService convocatoriaService;

    @Autowired
    private IntegracionService integracionService;

    @PostMapping
    @Operation(summary = "Crear emergencia",
            description = "Registra una emergencia e inicia el proceso correspondiente en Bonita. Si la integración falla, la operación no se considera exitosa.")
    public ResponseEntity<Emergencia> crear(@RequestBody Emergencia emergencia, SesionUsuario sesion) {
        // crear() guarda la emergencia y crea su caso en Bonita (todo o nada)
        Emergencia nueva = emergenciaService.crear(emergencia, sesion);
        return ResponseEntity.status(HttpStatus.CREATED).body(nueva);
    }

    @GetMapping
    @Operation(summary = "Listar emergencias",
            description = "Devuelve las emergencias registradas. El operador municipal solo ve las que creó.")
    public List<Emergencia> listar(SesionUsuario sesion) {
        return emergenciaService.obtenerVisibles(sesion);
    }

    // Path literal: Spring lo prefiere sobre "/{id}", asi que no choca con obtenerPorId
    @GetMapping("/pendientes")
    @RolesPermitidos(Rol.COORDINADOR)
    @Operation(summary = "Listar emergencias pendientes",
            description = "Devuelve las emergencias pendientes de gestión según su estado de proceso.")
    public List<Emergencia> listarPendientes() {
        return emergenciaService.obtenerPendientes();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener emergencia",
            description = "Busca una emergencia por su identificador. El operador municipal solo accede a las que creó.")
    public ResponseEntity<Emergencia> obtenerPorId(@PathVariable Long id, SesionUsuario sesion) {
        return emergenciaService.obtenerVisible(id, sesion)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/convocatorias")
    @RolesPermitidos(Rol.COORDINADOR)
    @Operation(summary = "Listar convocatorias de una emergencia",
            description = "Devuelve las convocatorias asociadas a la emergencia. Si no tiene convocatorias, devuelve una lista vacía.")
    public List<Convocatoria> listarConvocatorias(@PathVariable Long id) {
        return convocatoriaService.obtenerPorEmergencia(id);
    }

    @PostMapping("/{id}/convocatorias")
    @RolesPermitidos(Rol.COORDINADOR)
    @Operation(summary = "Crear relevamiento de emergencia",
            description = "Crea una convocatoria en BORRADOR junto con sus lotes y completa la tarea de relevamiento en Bonita. La operación es transaccional: ante un error no queda guardada parcialmente.")
    public ResponseEntity<Convocatoria> crearConvocatoria(@PathVariable Long id,
                                                          @RequestBody ConvocatoriaRequest request) {
        // Relevamiento: crea la convocatoria con sus lotes y completa "Desglosar Lotes" en Bonita
        Convocatoria nueva = convocatoriaService.crearRelevamiento(id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(nueva);
    }

    @GetMapping("/{id}/ofertas")
    @AccesoIntegracion
    @RolesPermitidos(Rol.COORDINADOR)
    @Operation(summary = "Listar ofertas consolidadas de una emergencia",
            description = "Devuelve los lotes y las ofertas de la convocatoria más reciente de la emergencia, con los códigos de tipo de recurso y de ONG del Sistema Nacional. El filtro opcional 'estado' solo aplica al listado de ofertas. Acepta el header X-Api-Key (Bonita).")
    public OfertasConsolidadasDto listarOfertasConsolidadas(@PathVariable Long id,
                                                           @RequestParam(required = false) EstadoOferta estado) {
        return integracionService.obtenerConsolidado(id, estado);
    }

    @PostMapping("/{id}/ofertas/validaciones")
    @AccesoIntegracion
    @RolesPermitidos(Rol.COORDINADOR)
    @Operation(summary = "Registrar validaciones de ofertas",
            description = "Registra el resultado binario del Sistema Nacional: cada oferta queda VALIDADA (habilitada) o NO_HABILITADA. Todas las ofertas deben pertenecer a la emergencia. Acepta el header X-Api-Key (Bonita).")
    public RegistroValidacionesResponse registrarValidaciones(@PathVariable Long id,
                                                              @Valid @RequestBody ValidacionesRequest request) {
        return new RegistroValidacionesResponse(integracionService.registrarValidaciones(id, request));
    }

    @PostMapping("/{id}/adjudicacion/notificaciones")
    @AccesoIntegracion
    @RolesPermitidos(Rol.COORDINADOR)
    @Operation(summary = "Confirmar adjudicación",
            description = "Notificación formal de Bonita: los lotes SELECCIONADA pasan a ADJUDICADA, se notifica a cada ONG su resultado y se crea el proyecto EN_CURSO de la emergencia. Es idempotente. Acepta el header X-Api-Key (Bonita).")
    public ConfirmacionAdjudicacionResponse confirmarAdjudicacion(@PathVariable Long id) {
        return integracionService.confirmarAdjudicacion(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar emergencia",
            description = "Actualiza los datos descriptivos de una emergencia existente. Conserva la fecha de creación, el identificador de caso y el usuario que la creó.")
    public ResponseEntity<Emergencia> actualizar(@PathVariable Long id, @RequestBody Emergencia detalles,
                                                 SesionUsuario sesion) {
        return emergenciaService.obtenerVisible(id, sesion).map(existente -> {
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
    @RolesPermitidos(Rol.COORDINADOR)
    @Operation(summary = "Eliminar emergencia", description = "Elimina la emergencia indicada si existe.")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        if (emergenciaService.obtenerPorId(id).isPresent()) {
            emergenciaService.eliminar(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
