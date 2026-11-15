package ar.edu.unlp.dssd.controller;

import java.util.List;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import ar.edu.unlp.dssd.dto.FinalizacionRequest;
import ar.edu.unlp.dssd.dto.OfertaEdicionRequest;
import ar.edu.unlp.dssd.dto.OfertaOngDto;
import ar.edu.unlp.dssd.dto.OfertaRequest;
import ar.edu.unlp.dssd.dto.OfertaVersionDto;
import ar.edu.unlp.dssd.seguridad.Rol;
import ar.edu.unlp.dssd.seguridad.RolesPermitidos;
import ar.edu.unlp.dssd.seguridad.SesionUsuario;
import ar.edu.unlp.dssd.service.OfertaService;

/** [Entrega final] Lotes ofertados por la ONG de la sesión */
@RestController
@RequestMapping("/api")
@Tag(name = "Ofertas de la ONG", description = "Alta, consulta, modificación con versiones, retiro y finalización de los lotes de la organización del usuario ONG.")
@RolesPermitidos(Rol.ONG)
public class OfertaController {

    @Autowired
    private OfertaService ofertaService;

    // [Entrega final] Se mantiene POST /api/ofertas como alias del alta
    @PostMapping({"/ong/ofertas", "/ofertas"})
    @Operation(summary = "Crear lote",
            description = "Registra un lote PENDIENTE (versión 1) en una convocatoria PUBLICADA con la ventana abierta. La organización se toma de la sesión; el organizacionId del cuerpo se ignora. Con ofertaConjuntaId el lote se suma a esa oferta conjunta (la organización tiene que ser dueña o participante). fueraDeEspecialidad indica que el tipo de recurso no está entre las especialidades de la ONG. Responde 409 si la ventana cerró y 400 si la convocatoria no pide ese tipo de recurso.")
    public ResponseEntity<OfertaOngDto> crear(@Valid @RequestBody OfertaRequest request, SesionUsuario sesion) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ofertaService.crear(request, sesion));
    }

    @GetMapping("/ong/ofertas")
    @Operation(summary = "Listar mis lotes",
            description = "Devuelve los lotes cargados por la organización del usuario, incluidos los que forman parte de sus ofertas conjuntas, del más reciente al más viejo.")
    public List<OfertaOngDto> listar(SesionUsuario sesion) {
        return ofertaService.listarPropias(sesion);
    }

    @GetMapping("/ong/ofertas/{id}")
    @Operation(summary = "Obtener lote",
            description = "Devuelve un lote propio o de una oferta conjunta de la que participa la organización. Responde 404 si es de otra organización.")
    public OfertaOngDto obtener(@PathVariable Long id, SesionUsuario sesion) {
        return ofertaService.obtener(id, sesion);
    }

    @PutMapping("/ong/ofertas/{id}")
    @Operation(summary = "Modificar lote",
            description = "Modifica tipo de recurso, cantidad y descripción de un lote propio PENDIENTE con la ventana abierta y guarda una versión nueva. Responde 409 si versionEsperada no coincide con la versión vigente o si la ventana cerró, y 403 si el lote lo cargó otra organización.")
    public OfertaOngDto editar(@PathVariable Long id, @Valid @RequestBody OfertaEdicionRequest request,
                               SesionUsuario sesion) {
        return ofertaService.editar(id, request, sesion);
    }

    @PostMapping("/ong/ofertas/{id}/retiro")
    @Operation(summary = "Retirar lote",
            description = "Marca un lote propio PENDIENTE como RETIRADA (no se borra) mientras la ventana está abierta y guarda una versión nueva. Un lote retirado deja de contar para su oferta conjunta.")
    public OfertaOngDto retirar(@PathVariable Long id, SesionUsuario sesion) {
        return ofertaService.retirar(id, sesion);
    }

    @GetMapping("/ong/ofertas/{id}/versiones")
    @Operation(summary = "Historial del lote",
            description = "Devuelve las versiones del lote, de la más nueva a la más vieja. El usuario es null si el cambio lo hizo el sistema.")
    public List<OfertaVersionDto> versiones(@PathVariable Long id, SesionUsuario sesion) {
        return ofertaService.versiones(id, sesion);
    }

    @PostMapping("/ong/ofertas/{id}/finalizacion")
    @Operation(summary = "Finalizar actividad del lote",
            description = "Marca un lote propio ADJUDICADA como FINALIZADA con un informe opcional. Cuando todos los lotes adjudicados de la emergencia quedan finalizados, el proyecto pasa a TERMINADO y se intenta completar la tarea de Bonita 'Finalizar actividades'.")
    public OfertaOngDto finalizar(@PathVariable Long id,
                                  @Valid @RequestBody(required = false) FinalizacionRequest request,
                                  SesionUsuario sesion) {
        return ofertaService.finalizar(id, request, sesion);
    }
}
