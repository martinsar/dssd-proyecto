package ar.edu.unlp.dssd.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import ar.edu.unlp.dssd.dto.CantidadDto;
import ar.edu.unlp.dssd.dto.NotificacionDto;
import ar.edu.unlp.dssd.seguridad.Rol;
import ar.edu.unlp.dssd.seguridad.RolesPermitidos;
import ar.edu.unlp.dssd.seguridad.SesionUsuario;
import ar.edu.unlp.dssd.service.NotificacionService;

/** [Entrega final] Notificaciones de la ONG */
@RestController
@RequestMapping("/api/ong/notificaciones")
@Tag(name = "Notificaciones", description = "Avisos de nuevas convocatorias, reaperturas y resultados de los lotes de la organización.")
@RolesPermitidos(Rol.ONG)
public class NotificacionController {

    @Autowired
    private NotificacionService notificacionService;

    @GetMapping
    @Operation(summary = "Listar notificaciones",
            description = "Devuelve las últimas 100 notificaciones de la organización, de la más nueva a la más vieja.")
    public List<NotificacionDto> listar(SesionUsuario sesion) {
        return notificacionService.listar(organizacion(sesion));
    }

    @GetMapping("/no-leidas")
    @Operation(summary = "Contar notificaciones no leídas",
            description = "Devuelve la cantidad de notificaciones de la organización que todavía no se leyeron.")
    public CantidadDto noLeidas(SesionUsuario sesion) {
        return new CantidadDto(notificacionService.contarNoLeidas(organizacion(sesion)));
    }

    @PostMapping("/{id}/lectura")
    @Operation(summary = "Marcar notificación como leída",
            description = "Marca como leída una notificación de la organización. Responde 404 si es de otra organización.")
    public ResponseEntity<Void> marcarLeida(@PathVariable Long id, SesionUsuario sesion) {
        notificacionService.marcarLeida(organizacion(sesion), id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/lectura")
    @Operation(summary = "Marcar todas como leídas",
            description = "Marca como leídas todas las notificaciones de la organización.")
    public ResponseEntity<Void> marcarTodas(SesionUsuario sesion) {
        notificacionService.marcarTodas(organizacion(sesion));
        return ResponseEntity.noContent().build();
    }

    private Long organizacion(SesionUsuario sesion) {
        if (sesion.organizacionId() == null) {
            throw new ar.edu.unlp.dssd.exception.AccesoDenegadoException(
                    "Tu usuario no está asociado a ninguna organización.");
        }
        return sesion.organizacionId();
    }
}
