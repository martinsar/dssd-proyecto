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
import ar.edu.unlp.dssd.dto.SerieMensualDto;
import ar.edu.unlp.dssd.dto.TiemposEmergenciaDto;
import ar.edu.unlp.dssd.model.EtapaEmergencia;
import ar.edu.unlp.dssd.seguridad.Rol;
import ar.edu.unlp.dssd.seguridad.RolesPermitidos;
import ar.edu.unlp.dssd.seguridad.SesionUsuario;
import ar.edu.unlp.dssd.service.IndicadoresService;

/** [Entrega final] Tablero del auditor (reportes combinando datos locales con la API REST de Bonita) */
@RestController
@RequestMapping("/api/auditor")
@Tag(name = "Auditor", description = "Indicadores, series mensuales y auditoría de emergencias combinando datos locales de RescueSync con métricas de la API REST de Bonita.")
@RolesPermitidos(Rol.AUDITOR)
public class AuditorController {

    @Autowired
    private IndicadoresService indicadoresService;

    @GetMapping("/indicadores")
    @Operation(summary = "Indicadores del período",
            description = "Totales, estado de proyectos, suministros movilizados por tipo, emergencias por etapa y gravedad, peso de consorcios y tiempos promedio por etapa obtenidos de la API de Bonita. 'desde' y 'hasta' (yyyy-MM-dd) son opcionales.")
    public IndicadoresDto indicadores(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                                      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
                                      SesionUsuario sesion) {
        return indicadoresService.indicadores(desde, hasta, sesion == null ? null : sesion.bonita());
    }

    @GetMapping("/series-mensuales")
    @Operation(summary = "Evolución mensual de indicadores",
            description = "Devuelve por mes (yyyy-MM) la cantidad de suministros movilizados (total y por tipo), emergencias registradas, proyectos finalizados y porcentaje de resolución mediante consorcios.")
    public List<SerieMensualDto> seriesMensuales(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                                                 @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return indicadoresService.seriesMensuales(desde, hasta);
    }

    @GetMapping("/emergencias")
    @Operation(summary = "Auditoría de emergencias",
            description = "Devuelve una fila por emergencia del período con su etapa, lotes recibidos y adjudicados, unidades movilizadas, participación de consorcios y estado/fechas del proyecto. Permite filtrar por 'etapa' y 'estadoProyecto' (EN_CURSO, TERMINADO, SIN_PROYECTO).")
    public List<AuditoriaEmergenciaDto> emergencias(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                                                    @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
                                                    @RequestParam(required = false) EtapaEmergencia etapa,
                                                    @RequestParam(required = false) String estadoProyecto) {
        return indicadoresService.emergencias(desde, hasta, etapa, estadoProyecto);
    }

    @GetMapping("/emergencias/{id}/tiempos")
    @Operation(summary = "Tiempos por etapa de una emergencia en Bonita",
            description = "Devuelve la línea de tiempo de etapas completadas y pendientes obtenidas de la API REST de Bonita para el caso asociado a la emergencia.")
    public TiemposEmergenciaDto tiemposEmergencia(@PathVariable Long id, SesionUsuario sesion) {
        return indicadoresService.tiemposEmergencia(id, sesion == null ? null : sesion.bonita());
    }
}
