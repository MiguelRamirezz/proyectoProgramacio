package com.example.proyectoProgramacion.controller.api;

import com.example.proyectoProgramacion.model.dto.usuario.UsuarioDTO;
import com.example.proyectoProgramacion.service.interfaces.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador para la gestión de usuarios
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Usuarios", description = "Operaciones para la gestión de usuarios")
@SecurityRequirement(name = "bearerAuth")
public class UserApiController {

    private final UsuarioService usuarioService;

    @Operation(summary = "Listar usuarios", description = "Obtiene una lista paginada de usuarios")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de usuarios obtenida exitosamente",
                   content = @Content(mediaType = "application/json",
                   schema = @Schema(implementation = Page.class))),
        @ApiResponse(responseCode = "401", description = "No autorizado"),
        @ApiResponse(responseCode = "403", description = "Acceso denegado"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Page<UsuarioDTO>> listarUsuarios(
            @Parameter(description = "Configuración de paginación", example = "{\"page\":0,\"size\":10,\"sort\":\"username,asc\"}")
            @PageableDefault(sort = "username", direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.ok(usuarioService.obtenerTodosLosUsuarios(pageable));
    }

    @Operation(summary = "Obtener usuario por nombre de usuario", description = "Obtiene los detalles de un usuario específico por su nombre de usuario")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Usuario encontrado",
                   content = @Content(mediaType = "application/json",
                   schema = @Schema(implementation = UsuarioDTO.class))),
        @ApiResponse(responseCode = "401", description = "No autorizado"),
        @ApiResponse(responseCode = "403", description = "Acceso denegado"),
        @ApiResponse(responseCode = "404", description = "Usuario no encontrado"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping(value = "/{username}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UsuarioDTO> obtenerUsuarioPorUsername(
            @Parameter(description = "Nombre de usuario a buscar", required = true, example = "usuario1")
            @PathVariable String username) {
        return ResponseEntity.ok(usuarioService.obtenerUsuarioPorUsername(username));
    }

    @Operation(summary = "Actualizar usuario", description = "Actualiza la información de un usuario existente")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Usuario actualizado exitosamente",
                   content = @Content(mediaType = "application/json",
                   schema = @Schema(implementation = UsuarioDTO.class))),
        @ApiResponse(responseCode = "400", description = "Datos de usuario inválidos"),
        @ApiResponse(responseCode = "401", description = "No autorizado"),
        @ApiResponse(responseCode = "403", description = "Acceso denegado"),
        @ApiResponse(responseCode = "404", description = "Usuario no encontrado"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @PutMapping(value = "/{username}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UsuarioDTO> actualizarUsuario(
            @Parameter(description = "Nombre de usuario del usuario a actualizar", required = true, example = "usuario1")
            @PathVariable String username,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Datos actualizados del usuario", required = true)
            @Valid @RequestBody UsuarioDTO usuarioDTO) {
        return ResponseEntity.ok(usuarioService.actualizarUsuario(username, usuarioDTO));
    }

    @Operation(summary = "Eliminar usuario", description = "Elimina un usuario por su nombre de usuario")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Usuario eliminado exitosamente"),
        @ApiResponse(responseCode = "401", description = "No autorizado"),
        @ApiResponse(responseCode = "403", description = "Acceso denegado"),
        @ApiResponse(responseCode = "404", description = "Usuario no encontrado"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @DeleteMapping("/{username}")
    public ResponseEntity<Void> eliminarUsuario(
            @Parameter(description = "Nombre de usuario del usuario a eliminar", required = true, example = "usuario1")
            @PathVariable String username) {
        // First get the user to delete by username
        UsuarioDTO usuario = usuarioService.obtenerUsuarioPorUsername(username);
        usuarioService.eliminarUsuario(usuario.getId());
        return ResponseEntity.noContent().build();
    }
}
