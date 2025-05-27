package com.example.proyectoProgramacion.controller.api;

import com.example.proyectoProgramacion.model.dto.auth.JwtAuthResponseDTO;
import com.example.proyectoProgramacion.model.dto.auth.LoginRequestDTO;
import com.example.proyectoProgramacion.model.dto.auth.RegistroUsuarioDTO;
import com.example.proyectoProgramacion.service.interfaces.AdminRegistrationService;
import com.example.proyectoProgramacion.service.interfaces.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador para la autenticación y registro de usuarios y administradores
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticación", description = "Operaciones de autenticación y registro de usuarios")
public class AuthApiController {

    private final AuthService authService;
    private final AdminRegistrationService adminRegistrationService;

    @Operation(summary = "Iniciar sesión", description = "Autentica un usuario y devuelve un token JWT")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Autenticación exitosa", 
                    content = @Content(mediaType = "application/json", 
                    schema = @Schema(implementation = JwtAuthResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Solicitud inválida"),
        @ApiResponse(responseCode = "401", description = "Credenciales inválidas"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @PostMapping(value = "/login", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<JwtAuthResponseDTO> login(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Credenciales de inicio de sesión", required = true)
            @Valid @RequestBody LoginRequestDTO loginRequest,
            HttpServletRequest request) {
        String ipAddress = request.getRemoteAddr();
        JwtAuthResponseDTO response = authService.autenticarUsuario(loginRequest, ipAddress);
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Registrar nuevo usuario", 
        description = "Crea una nueva cuenta de usuario. Los usuarios normales no pueden registrarse como administradores."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Usuario registrado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos de registro inválidos", 
                    content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "409", description = "El nombre de usuario o correo ya existe",
                    content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor",
                    content = @Content(mediaType = "application/json"))
    })
    @PostMapping(value = "/register", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> registrarUsuario(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Datos de registro del usuario", 
                required = true,
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = RegistroUsuarioDTO.class),
                    examples = @ExampleObject(
                        name = "Ejemplo de registro de usuario",
                        value = """
                        {
                            "username": "usuario123",
                            "email": "usuario@ejemplo.com",
                            "password": "Contraseña123!",
                            "confirmPassword": "Contraseña123!",
                            "nombre": "Juan",
                            "apellido": "Pérez",
                            "telefono": "912345678",
                            "terminosAceptados": true
                        }
                        """
                    )
                )
            )
            @Valid @RequestBody RegistroUsuarioDTO registroDTO) {
        // Asegurarse de que no se pueda registrar un administrador a través de este endpoint
        if (registroDTO.isEsAdmin()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("No tiene permiso para registrar administradores");
        }
        
        authService.registrarUsuario(registroDTO);
        return ResponseEntity.ok("Usuario registrado exitosamente");
    }
    
    @Operation(
        summary = "Registrar nuevo administrador", 
        description = "Crea una nueva cuenta de administrador. Requiere permisos de administrador.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Administrador registrado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos de registro inválidos",
                   content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "401", description = "No autorizado",
                   content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "403", description = "No tiene permisos para realizar esta acción",
                   content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "409", description = "El nombre de usuario o correo ya existe",
                   content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor",
                   content = @Content(mediaType = "application/json"))
    })
    @PostMapping(value = "/register/admin", produces = MediaType.TEXT_PLAIN_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> registrarAdmin(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Datos de registro del administrador", 
                required = true,
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = RegistroUsuarioDTO.class),
                    examples = @ExampleObject(
                        name = "Ejemplo de registro de administrador",
                        value = """
                        {
                            "username": "admin123",
                            "email": "admin@ejemplo.com",
                            "password": "Admin123!",
                            "confirmPassword": "Admin123!",
                            "nombre": "Admin",
                            "apellido": "Sistema",
                            "telefono": "987654321",
                            "esAdmin": true,
                            "terminosAceptados": true
                        }
                        """
                    )
                )
            )
            @Valid @RequestBody RegistroUsuarioDTO registroDTO) {
        // Asegurarse de que se esté registrando un administrador
        registroDTO.setEsAdmin(true);
        adminRegistrationService.registrarAdmin(registroDTO);
        return ResponseEntity.ok("Administrador registrado exitosamente");
    }
}
