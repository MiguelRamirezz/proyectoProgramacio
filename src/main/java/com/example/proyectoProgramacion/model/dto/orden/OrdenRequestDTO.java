package com.example.proyectoProgramacion.model.dto.orden;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * DTO para la solicitud de creación de una nueva orden.
 */
@Data
public class OrdenRequestDTO {
    
    @NotBlank(message = "La dirección de envío es obligatoria")
    private String direccionEnvio;
    
    @NotBlank(message = "El método de pago es obligatorio")
    private String metodoPago;
    
    @NotNull(message = "El ID del carrito es obligatorio")
    private Long carritoId;
    
    /**
     * Valida que la solicitud tenga todos los campos requeridos.
     * @return true si la solicitud es válida, false en caso contrario
     */
    public boolean isValid() {
        return direccionEnvio != null && !direccionEnvio.trim().isEmpty() &&
               metodoPago != null && !metodoPago.trim().isEmpty() &&
               carritoId != null && carritoId > 0;
    }
}
