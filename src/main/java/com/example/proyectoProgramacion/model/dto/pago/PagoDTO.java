package com.example.proyectoProgramacion.model.dto.pago;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO que representa un pago en el sistema.
 */
@Data
public class PagoDTO {
    
    private Long id;
    
    @NotBlank(message = "El método de pago es obligatorio")
    private String metodo;
    
    @NotNull(message = "La fecha del pago es obligatoria")
    private LocalDateTime fecha;
    
    @Size(min = 4, max = 4, message = "Los últimos 4 dígitos son obligatorios")
    private String ultimosDigitos;
    
    @NotNull(message = "El ID de la orden es obligatorio")
    private Long ordenId;
    
    @NotNull(message = "El ID del usuario es obligatorio")
    private Long usuarioId;
    
    @NotBlank(message = "El nombre de usuario es obligatorio")
    private String nombreUsuario;
    
    @NotNull(message = "El monto es obligatorio")
    @PositiveOrZero(message = "El monto no puede ser negativo")
    private BigDecimal monto;
    
    private String estado;
    private String referenciaPago;
    
    /**
     * Valida si el pago está completado con éxito.
     * @return true si el pago está completado, false en caso contrario
     */
    public boolean isCompletado() {
        return "COMPLETADO".equalsIgnoreCase(estado);
    }
}
