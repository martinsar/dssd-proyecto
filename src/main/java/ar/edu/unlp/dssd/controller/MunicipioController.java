package ar.edu.unlp.dssd.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import ar.edu.unlp.dssd.dto.AdjudicacionRequest;
import ar.edu.unlp.dssd.dto.AdjudicacionResponse;
import ar.edu.unlp.dssd.dto.EmergenciaDetalleDto;
import ar.edu.unlp.dssd.dto.EmergenciaResumenDto;
import ar.edu.unlp.dssd.dto.OfertaValidadaDto;
import ar.edu.unlp.dssd.seguridad.Rol;
import ar.edu.unlp.dssd.seguridad.RolesPermitidos;
import ar.edu.unlp.dssd.seguridad.SesionUsuario;
import ar.edu.unlp.dssd.service.MunicipioService;

/** [Entrega final] Emergencias del operador municipal y selección de ofertas */
@RestController
@RequestMapping("/api/municipio")
@Tag(name = "Municipio", description = "Seguimiento de las emergencias creadas por el operador municipal y selección de los lotes validados.")
@RolesPermitidos(Rol.OPERADOR_MUNICIPAL)
public class MunicipioController {

    @Autowired
    private MunicipioService municipioService;

    @GetMapping("/emergencias")
    @Operation(summary = "Listar mis emergencias",
            description = "Devuelve las emergencias creadas por el usuario con su etapa calculada, el cierre de la convocatoria y la cantidad de lotes validados.")
    public List<EmergenciaResumenDto> emergencias(SesionUsuario sesion) {
        return municipioService.misEmergencias(sesion);
    }

    @GetMapping("/emergencias/{id}")
    @Operation(summary = "Detalle de mi emergencia",
            description = "Devuelve la etapa, la convocatoria vigente, la cobertura validada y adjudicada de cada lote (sin lo ofertado) y el proyecto. Responde 404 si la emergencia no la creó el usuario.")
    public EmergenciaDetalleDto detalle(@PathVariable Long id, SesionUsuario sesion) {
        return municipioService.detalle(id, sesion);
    }

    @GetMapping("/emergencias/{id}/ofertas-validadas")
    @Operation(summary = "Listar ofertas validadas",
            description = "Devuelve los lotes de ofertas válidas en estado VALIDADA, SELECCIONADA, ADJUDICADA, NO_SELECCIONADA o FINALIZADA.")
    public List<OfertaValidadaDto> ofertasValidadas(@PathVariable Long id, SesionUsuario sesion) {
        return municipioService.ofertasValidadas(id, sesion);
    }

    @PostMapping("/emergencias/{id}/adjudicacion")
    @Operation(summary = "Seleccionar ofertas",
            description = "Los lotes elegidos pasan a SELECCIONADA y el resto de los VALIDADA a NO_SELECCIONADA. Si la integración completa con Bonita está desactivada, se adjudican en el momento, se notifica a las ONGs y se crea el proyecto EN_CURSO; si está activada, se completa 'Seleccionar ofertas' y se espera la notificación de Bonita. Responde 409 si la emergencia no está en EN_SELECCION y 400 si algún lote no está VALIDADA.")
    public AdjudicacionResponse adjudicar(@PathVariable Long id, @RequestBody AdjudicacionRequest request,
                                          SesionUsuario sesion) {
        return municipioService.adjudicar(id, request, sesion);
    }
}
