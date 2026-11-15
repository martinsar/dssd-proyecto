package ar.edu.unlp.dssd.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import ar.edu.unlp.dssd.dto.LoginRequest;
import ar.edu.unlp.dssd.dto.LoginResponse;
import ar.edu.unlp.dssd.dto.SesionActualDto;
import ar.edu.unlp.dssd.seguridad.Publico;
import ar.edu.unlp.dssd.seguridad.SesionUsuario;
import ar.edu.unlp.dssd.service.AuthService;

/** [Entrega final] Ingreso, sesión vigente y cierre de sesión */
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticación", description = "Ingreso con el usuario de Bonita, consulta y cierre de la sesión.")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Publico
    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión",
            description = "Autentica al usuario contra Bonita, resuelve su perfil y devuelve un token para enviar como 'Authorization: Bearer <token>'.")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @GetMapping("/sesion")
    @Operation(summary = "Consultar sesión",
            description = "Devuelve los datos del usuario logueado y el vencimiento de la sesión.")
    public SesionActualDto sesion(SesionUsuario sesion) {
        return authService.sesionActual(sesion);
    }

    @PostMapping("/logout")
    @Operation(summary = "Cerrar sesión",
            description = "Invalida el token de la app y cierra la sesión del usuario en Bonita.")
    public ResponseEntity<Void> logout(SesionUsuario sesion) {
        authService.logout(sesion);
        return ResponseEntity.noContent().build();
    }
}
