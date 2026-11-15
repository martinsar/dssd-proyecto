package ar.edu.unlp.dssd.controller;

import java.time.LocalDate;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import ar.edu.unlp.dssd.dto.AuditoriaEmergenciaDto;
import ar.edu.unlp.dssd.dto.IndicadoresDto;
import ar.edu.unlp.dssd.seguridad.Rol;
import ar.edu.unlp.dssd.seguridad.RolesPermitidos;
import ar.edu.unlp.dssd.service.IndicadoresService;

/** [Entrega final] Tablero del auditor */
@RestController
@RequestMapping("/api/auditor")
@Tag(name = "Auditor", description = "Indicadores y auditoría de emergencias con los datos locales de RescueSync.")
@RolesPermitidos(Rol.AUDITOR)
public class AuditorController {

    @Autowired
    private IndicadoresService indicadoresService;

    @GetMapping("/indicadores")
    @Operation(summary = "Indicadores del período",
            description = "Totales, suministros por tipo, emergencias por etapa y gravedad, lotes por estado y peso de las ofertas conjuntas. 'desde' y 'hasta' (yyyy-MM-dd) son opcionales y filtran por la fecha de creación de la emergencia.")
    public IndicadoresDto indicadores(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                                      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return indicadoresService.indicadores(desde, hasta);
    }

    @GetMapping("/emergencias")
    @Operation(summary = "Auditoría de emergencias",
            description = "Devuelve una fila por emergencia del período con su etapa, lotes recibidos y adjudicados, unidades movilizadas y estado del proyecto.")
    public List<AuditoriaEmergenciaDto> emergencias(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                                                    @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return indicadoresService.emergencias(desde, hasta);
    }
}
