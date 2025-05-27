package com.example.proyectoProgramacion.controller.api;

import com.example.proyectoProgramacion.model.dto.pago.PagoDTO;
import com.example.proyectoProgramacion.model.dto.pago.PagoRequestDTO;
import com.example.proyectoProgramacion.model.dto.pago.PagoResponseDTO;
import com.example.proyectoProgramacion.exception.BusinessException;
import com.example.proyectoProgramacion.exception.ResourceNotFoundException;
import com.example.proyectoProgramacion.service.interfaces.PagoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/pagos")
@Tag(name = "Pagos", description = "API para gestión de pagos")
@SecurityRequirement(name = "bearerAuth")
public class PagoController {

    private final PagoService pagoService;

    @Autowired
    public PagoController(PagoService pagoService) {
        this.pagoService = pagoService;
    }
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Transactional
    @Operation(summary = "Procesar pago", description = "Procesa el pago de una orden")
    @ApiResponse(responseCode = "200", description = "Pago procesado exitosamente")
    @ApiResponse(responseCode = "400", description = "Error al procesar el pago")
    @ApiResponse(responseCode = "404", description = "Orden no encontrada")
    public ResponseEntity<PagoResponseDTO> procesarPago(
            @Valid @RequestBody PagoRequestDTO pagoRequest,
            Authentication authentication) {

        String username = authentication.getName();

        try {
            // Procesar el pago utilizando el gateway configurado
            PagoResponseDTO response = pagoService.procesarPago(pagoRequest, username);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            throw new BusinessException("Error al procesar el pago: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Obtener información de pago", description = "Obtiene la información de un pago específico")
    @ApiResponse(responseCode = "200", description = "Información de pago obtenida")
    @ApiResponse(responseCode = "404", description = "Pago no encontrado")
    public ResponseEntity<PagoDTO> obtenerPago(
            @PathVariable Long id,
            Authentication authentication) {

        String username = authentication.getName();

        // Obtener información del pago
        PagoDTO pago = pagoService.obtenerPagoPorId(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pago no encontrado con ID: " + id));

        // Verificar que el pago pertenece al usuario o es admin
        if (!pagoService.verificarPropiedadPago(id, username) && authentication.getAuthorities().stream()
                .noneMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            throw new BusinessException("No tiene permiso para ver este pago");
        }

        return ResponseEntity.ok(pago);
    }

    @PostMapping("/{id}/reembolso")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Transactional
    @Operation(summary = "Procesar reembolso", description = "Procesa el reembolso de un pago")
    @ApiResponse(responseCode = "200", description = "Reembolso procesado exitosamente")
    @ApiResponse(responseCode = "400", description = "Error al procesar el reembolso")
    @ApiResponse(responseCode = "404", description = "Pago no encontrado")
    public ResponseEntity<PagoDTO> procesarReembolso(
            @PathVariable Long id,
            Authentication authentication) {

        try {
            // Procesar el reembolso (el servicio maneja la lógica de validación)
            PagoDTO pagoReembolsado = pagoService.reembolsarPago(id, authentication.getName());
            return ResponseEntity.ok(pagoReembolsado);
        } catch (Exception e) {
            throw new BusinessException("Error al procesar el reembolso: " + e.getMessage());
        }
    }
}





