package com.example.proyectoProgramacion.model.dto.carrito;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO que representa un ítem en el carrito de compras.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemCarritoDTO {
    
    @NotNull(message = "El ID del ítem es obligatorio")
    private Long id;
    
    @NotNull(message = "El ID del carrito es obligatorio")
    private Long carritoId;
    
    @NotNull(message = "El ID del producto es obligatorio")
    private Long productoId;
    
    @NotNull(message = "El nombre del producto es obligatorio")
    private String nombreProducto;
    
    private String imagenProducto;
    
    @Min(value = 1, message = "La cantidad debe ser al menos 1")
    private int cantidad;
    
    @NotNull(message = "El precio unitario es obligatorio")
    @DecimalMin(value = "0.0", inclusive = false, message = "El precio debe ser mayor que 0")
    private BigDecimal precioUnitario;
    
    @Min(value = 0, message = "El stock no puede ser negativo")
    private int stockDisponible;
    
    @NotNull(message = "El subtotal es obligatorio")
    @DecimalMin(value = "0.0", message = "El subtotal no puede ser negativo")
    private BigDecimal subtotal;
}
