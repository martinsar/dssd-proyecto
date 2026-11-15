package ar.edu.unlp.dssd.controller;

import java.util.List;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import ar.edu.unlp.dssd.dto.ConvocatoriaAbiertaDto;
import ar.edu.unlp.dssd.dto.EspecialidadDto;
import ar.edu.unlp.dssd.dto.InventarioItemDto;
import ar.edu.unlp.dssd.dto.InventarioRequest;
import ar.edu.unlp.dssd.dto.OrganizacionResumenDto;
import ar.edu.unlp.dssd.seguridad.Rol;
import ar.edu.unlp.dssd.seguridad.RolesPermitidos;
import ar.edu.unlp.dssd.seguridad.SesionUsuario;
import ar.edu.unlp.dssd.service.InventarioService;
import ar.edu.unlp.dssd.service.OngService;

/** [Entrega final] Consultas e inventario del perfil ONG */
@RestController
@RequestMapping("/api/ong")
@Tag(name = "ONG", description = "Convocatorias publicadas, especialidades, organizaciones e inventario de la ONG del usuario.")
@RolesPermitidos(Rol.ONG)
public class OngController {

    @Autowired
    private OngService ongService;

    @Autowired
    private InventarioService inventarioService;

    @GetMapping("/convocatorias")
    @Operation(summary = "Listar convocatorias publicadas",
            description = "Devuelve las convocatorias PUBLICADAS con sus lotes, ordenadas por fecha de cierre. 'abierta' indica si todavía reciben lotes.")
    public List<ConvocatoriaAbiertaDto> convocatorias() {
        return ongService.convocatorias();
    }

    @GetMapping("/especialidades")
    @Operation(summary = "Listar mis especialidades",
            description = "Devuelve los tipos de recurso en los que se especializa la organización del usuario.")
    public List<EspecialidadDto> especialidades(SesionUsuario sesion) {
        return ongService.especialidades(sesion);
    }

    @GetMapping("/organizaciones")
    @Operation(summary = "Listar otras organizaciones",
            description = "Devuelve las demás organizaciones (sin la propia) para elegir participantes de una oferta conjunta.")
    public List<OrganizacionResumenDto> organizaciones(SesionUsuario sesion) {
        return ongService.organizaciones(sesion);
    }

    @GetMapping("/inventario")
    @Operation(summary = "Listar inventario",
            description = "Devuelve los recursos del inventario con lo ofertado (lotes PENDIENTE y VALIDADA) y lo comprometido (SELECCIONADA y ADJUDICADA).")
    public List<InventarioItemDto> inventario(SesionUsuario sesion) {
        return inventarioService.listar(sesion);
    }

    @PutMapping("/inventario/{tipoRecursoId}")
    @Operation(summary = "Guardar recurso del inventario",
            description = "Crea o actualiza la cantidad disponible y la descripción del tipo de recurso en el inventario de la organización.")
    public InventarioItemDto guardarInventario(@PathVariable Long tipoRecursoId,
                                               @Valid @RequestBody InventarioRequest request,
                                               SesionUsuario sesion) {
        return inventarioService.guardar(tipoRecursoId, request, sesion);
    }

    @DeleteMapping("/inventario/{tipoRecursoId}")
    @Operation(summary = "Quitar recurso del inventario",
            description = "Elimina el tipo de recurso del inventario de la organización. Responde 404 si no estaba cargado.")
    public ResponseEntity<Void> eliminarInventario(@PathVariable Long tipoRecursoId, SesionUsuario sesion) {
        inventarioService.eliminar(tipoRecursoId, sesion);
        return ResponseEntity.noContent().build();
    }
}
