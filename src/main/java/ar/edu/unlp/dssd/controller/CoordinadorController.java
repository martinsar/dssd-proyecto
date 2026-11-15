package ar.edu.unlp.dssd.controller;

import java.util.List;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import ar.edu.unlp.dssd.dto.ConvocatoriaResumenDto;
import ar.edu.unlp.dssd.dto.DecisionConvocatoriaRequest;
import ar.edu.unlp.dssd.dto.MonitoreoFilaDto;
import ar.edu.unlp.dssd.dto.SeguimientoEmergenciaDto;
import ar.edu.unlp.dssd.model.EtapaEmergencia;
import ar.edu.unlp.dssd.seguridad.Rol;
import ar.edu.unlp.dssd.seguridad.RolesPermitidos;
import ar.edu.unlp.dssd.service.ConvocatoriaService;
import ar.edu.unlp.dssd.service.MonitoreoService;

/** [Entrega final] Monitoreo y decisiones del Centro Coordinador */
@RestController
@RequestMapping("/api/coordinador")
@Tag(name = "Coordinador", description = "Monitoreo de las emergencias, seguimiento de sus lotes y decisiones ante cobertura insuficiente.")
@RolesPermitidos(Rol.COORDINADOR)
public class CoordinadorController {

    @Autowired
    private MonitoreoService monitoreoService;

    @Autowired
    private ConvocatoriaService convocatoriaService;

    @GetMapping("/monitoreo")
    @Operation(summary = "Monitorear emergencias",
            description = "Devuelve una fila por emergencia con su etapa, cobertura y avance de ejecución. Primero las que requieren decisión y después por id descendente. El filtro 'etapa' es opcional.")
    public List<MonitoreoFilaDto> monitoreo(@RequestParam(required = false) EtapaEmergencia etapa) {
        return monitoreoService.monitoreo(etapa);
    }

    @GetMapping("/emergencias/{id}")
    @Operation(summary = "Seguimiento de una emergencia",
            description = "Devuelve el detalle de la emergencia (con lo ofertado por lote) y todos sus lotes con su estado, incluidos los retirados y los de ofertas conjuntas todavía no válidas.")
    public SeguimientoEmergenciaDto seguimiento(@PathVariable Long id) {
        return monitoreoService.seguimiento(id);
    }

    @PostMapping("/convocatorias/{id}/decision")
    @Operation(summary = "Decidir sobre una convocatoria",
            description = "Solo si la etapa es REQUIERE_DECISION (si no, 409). REABRIR: nueva fecha de cierre futura; los lotes evaluados vuelven a PENDIENTE y se notifica a las ONGs. REFORMULAR: igual que REABRIR y además reemplaza los lotes pedidos por 'necesidades'. CONTINUAR_PARCIAL: acepta la cobertura parcial y habilita la selección.")
    public ConvocatoriaResumenDto decidir(@PathVariable Long id, @Valid @RequestBody DecisionConvocatoriaRequest request) {
        return convocatoriaService.decidir(id, request);
    }
}
