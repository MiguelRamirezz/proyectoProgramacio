package com.example.proyectoProgramacion.controller.api;

import com.example.proyectoProgramacion.service.interfaces.OrdenService;
import com.example.proyectoProgramacion.exception.BusinessException;
import com.example.proyectoProgramacion.model.dto.orden.OrdenDTO;
import com.example.proyectoProgramacion.model.dto.orden.OrdenRequestDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import com.example.proyectoProgramacion.model.enums.EstadoOrden;

/**
 * Controlador para la gestión de órdenes de compra
 */
@RestController
@RequestMapping("/api/ordenes")
@Tag(name = "Órdenes", description = "API para gestión de órdenes de compra")
@SecurityRequirement(name = "bearerAuth")
@ApiResponses(value = {
    @ApiResponse(responseCode = "401", description = "No autorizado - Se requiere autenticación"),
    @ApiResponse(responseCode = "403", description = "Acceso denegado - No tiene permisos suficientes"),
    @ApiResponse(responseCode = "500", description = "Error interno del servidor")
})
public class OrdenController {

    private final OrdenService ordenService;

    public OrdenController(OrdenService ordenService) {
        this.ordenService = ordenService;
    }

    @Operation(summary = "Listar órdenes del usuario", 
               description = "Obtiene todas las órdenes del usuario autenticado ordenadas por fecha de creación")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de órdenes obtenida exitosamente",
                   content = @Content(mediaType = "application/json",
                   schema = @Schema(implementation = Page.class)))
    })
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<OrdenDTO>> listarOrdenes(
            @Parameter(hidden = true) Authentication authentication,
            @Parameter(description = "Configuración de paginación", example = "{\"page\":0,\"size\":10,\"sort\":\"fechaCreacion,desc\"}")
            @PageableDefault(size = 10, sort = "fechaCreacion", direction = Sort.Direction.DESC) Pageable pageable) {
        String username = authentication.getName();
        return ResponseEntity.ok(ordenService.obtenerOrdenesPorUsuario(username, pageable));
    }

    @Operation(summary = "Obtener orden por ID", 
               description = "Obtiene los detalles de una orden específica por su ID. Solo el propietario o un administrador pueden ver la orden.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Orden encontrada",
                   content = @Content(mediaType = "application/json",
                   schema = @Schema(implementation = OrdenDTO.class))),
        @ApiResponse(responseCode = "403", description = "No autorizado para ver esta orden"),
        @ApiResponse(responseCode = "404", description = "Orden no encontrada")
    })
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<OrdenDTO> obtenerOrden(
            @Parameter(description = "ID de la orden a consultar", required = true, example = "1")
            @PathVariable Long id,
            @Parameter(hidden = true) Authentication authentication) {

        String username = authentication.getName();
        OrdenDTO orden = ordenService.obtenerOrdenPorId(id);

        // Verificar que la orden pertenece al usuario o es admin
        if (!orden.getUsuario().equals(username) && authentication.getAuthorities().stream()
                .noneMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            throw new BusinessException("No tiene permiso para ver esta orden");
        }

        return ResponseEntity.ok(orden);
    }

    @Operation(summary = "Crear nueva orden", 
               description = "Crea una nueva orden a partir del carrito de compras del usuario autenticado")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Orden creada exitosamente",
                   content = @Content(mediaType = "application/json",
                   schema = @Schema(implementation = OrdenDTO.class))),
        @ApiResponse(responseCode = "400", description = "Error al crear la orden - Carrito vacío o datos inválidos"),
        @ApiResponse(responseCode = "404", description = "Producto no encontrado en el carrito")
    })
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("isAuthenticated()")
    @Transactional
    public ResponseEntity<OrdenDTO> crearOrden(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Datos necesarios para crear la orden",
                required = true,
                content = @Content(schema = @Schema(implementation = OrdenRequestDTO.class))
            )
            @Valid @RequestBody OrdenRequestDTO ordenRequest,
            @Parameter(hidden = true) Authentication authentication) {

        String username = authentication.getName();

        // Validar y crear la orden
        // Nota: Se asume que la validación del carrito se hace dentro de crearOrden
        OrdenDTO nuevaOrden = ordenService.crearOrden(username, ordenRequest.getDireccionEnvio());
        return new ResponseEntity<>(nuevaOrden, HttpStatus.CREATED);
    }

    @Operation(summary = "Cancelar orden", 
               description = "Cancela una orden existente. Solo el propietario o un administrador pueden cancelar la orden. " +
                           "No se pueden cancelar órdenes completadas o ya canceladas.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Orden cancelada exitosamente",
                   content = @Content(mediaType = "application/json",
                   schema = @Schema(implementation = OrdenDTO.class))),
        @ApiResponse(responseCode = "400", description = "La orden no puede ser cancelada en su estado actual"),
        @ApiResponse(responseCode = "403", description = "No autorizado para cancelar esta orden"),
        @ApiResponse(responseCode = "404", description = "Orden no encontrada")
    })
    @PutMapping(value = "/{id}/cancelar", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("isAuthenticated()")
    @Transactional
    public ResponseEntity<OrdenDTO> cancelarOrden(
            @Parameter(description = "ID de la orden a cancelar", required = true, example = "1")
            @PathVariable Long id,
            @Parameter(hidden = true) Authentication authentication) {

        String username = authentication.getName();

        // Verificar que la orden existe
        OrdenDTO orden = ordenService.obtenerOrdenPorId(id);

        // Verificar que la orden pertenece al usuario
        if (!orden.getUsuario().equals(username) && authentication.getAuthorities().stream()
                .noneMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            throw new BusinessException("No tiene permiso para cancelar esta orden");
        }

        // Verificar que la orden puede ser cancelada (según su estado)
        String estado = orden.getEstado();
        if (EstadoOrden.COMPLETADA.name().equals(estado) || EstadoOrden.CANCELADA.name().equals(estado)) {
            throw new BusinessException("La orden no puede ser cancelada en su estado actual");
        }

        return ResponseEntity.ok(ordenService.cancelarOrden(id, username));
    }
}
