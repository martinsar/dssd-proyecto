package ar.edu.unlp.dssd.controller;

import java.util.List;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import ar.edu.unlp.dssd.dto.OfertaConjuntaOngDto;
import ar.edu.unlp.dssd.dto.OfertaConjuntaRequest;
import ar.edu.unlp.dssd.seguridad.Rol;
import ar.edu.unlp.dssd.seguridad.RolesPermitidos;
import ar.edu.unlp.dssd.seguridad.SesionUsuario;
import ar.edu.unlp.dssd.service.OfertaConjuntaService;

/** [Entrega final] Ofertas conjuntas entre ONGs */
@RestController
@RequestMapping("/api/ong/ofertas-conjuntas")
@Tag(name = "Ofertas conjuntas", description = "Ofertas armadas entre varias ONGs para una convocatoria. Es válida solo si todas las ONGs asociadas cargaron al menos un lote no retirado.")
@RolesPermitidos(Rol.ONG)
public class OfertaConjuntaController {

    @Autowired
    private OfertaConjuntaService ofertaConjuntaService;

    @PostMapping
    @Operation(summary = "Crear oferta conjunta",
            description = "La organización del usuario queda como dueña y las participantes quedan fijas (no se pueden quitar ni cambiar). Responde 400 si no hay participantes y 409 si la ventana cerró, si se repite una organización o si se incluye a la propia.")
    public ResponseEntity<OfertaConjuntaOngDto> crear(@Valid @RequestBody OfertaConjuntaRequest request,
                                                      SesionUsuario sesion) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ofertaConjuntaService.crear(request, sesion));
    }

    @GetMapping
    @Operation(summary = "Listar mis ofertas conjuntas",
            description = "Devuelve las ofertas conjuntas donde la organización es dueña o participante, con su validez, las ONGs que faltan y los lotes cargados.")
    public List<OfertaConjuntaOngDto> listar(SesionUsuario sesion) {
        return ofertaConjuntaService.listar(sesion);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener oferta conjunta",
            description = "Devuelve la oferta conjunta si la organización es dueña o participante; si no, responde 404.")
    public OfertaConjuntaOngDto obtener(@PathVariable Long id, SesionUsuario sesion) {
        return ofertaConjuntaService.obtener(id, sesion);
    }
}
