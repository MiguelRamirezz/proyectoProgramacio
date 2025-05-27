package com.example.proyectoProgramacion.model.dto.orden;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO que representa una orden de compra en el sistema.
 */
@Data
public class OrdenDTO {
    
    private Long id;
    
    @NotBlank(message = "El nombre de usuario es obligatorio")
    private String usuario;
    
    @NotNull(message = "La fecha de creación es obligatoria")
    private LocalDateTime fechaCreacion;
    
    private LocalDateTime fechaActualizacion;
    
    @NotBlank(message = "El estado de la orden es obligatorio")
    private String estado;
    
    @NotNull(message = "El total es obligatorio")
    @PositiveOrZero(message = "El total no puede ser negativo")
    private BigDecimal total;
    
    @NotBlank(message = "La dirección de envío es obligatoria")
    private String direccionEnvio;
    
    @NotBlank(message = "El método de pago es obligatorio")
    private String metodoPago;
    
    @Valid
    private List<ItemOrdenDTO> items = new ArrayList<>();
    
    /**
     * DTO que representa un ítem dentro de una orden.
     */
    @Data
    public static class ItemOrdenDTO {
        
        private Long id;
        
        @NotNull(message = "El ID del producto es obligatorio")
        private Long productoId;
        
        @NotBlank(message = "El nombre del producto es obligatorio")
        private String nombreProducto;
        
        @NotNull(message = "La cantidad es obligatoria")
        @PositiveOrZero(message = "La cantidad no puede ser negativa")
        private Integer cantidad;
        
        @NotNull(message = "El precio unitario es obligatorio")
        @PositiveOrZero(message = "El precio unitario no puede ser negativo")
        private BigDecimal precioUnitario;
        
        @NotNull(message = "El subtotal es obligatorio")
        @PositiveOrZero(message = "El subtotal no puede ser negativo")
        private BigDecimal subtotal;
        
        /**
         * Calcula el subtotal del ítem basado en la cantidad y el precio unitario.
         * @return el subtotal calculado
         */
        public BigDecimal calcularSubtotal() {
            if (cantidad == null || precioUnitario == null) {
                return BigDecimal.ZERO;
            }
            return precioUnitario.multiply(BigDecimal.valueOf(cantidad));
        }
    }
}
