package com.example.proyectoProgramacion.model.dto.producto;

import com.example.proyectoProgramacion.model.enums.Categoria;
import com.example.proyectoProgramacion.validation.ValidCategoria;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO que representa un producto en el sistema.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductoDTO {
    
    private Long id;
    
    @NotBlank(message = "El nombre del producto es obligatorio")
    @Size(min = 3, max = 100, message = "El nombre debe tener entre 3 y 100 caracteres")
    private String nombre;
    
    @NotBlank(message = "La descripción es obligatoria")
    @Size(min = 10, max = 1000, message = "La descripción debe tener entre 10 y 1000 caracteres")
    private String descripcion;
    
    @NotNull(message = "El precio es obligatorio")
    @DecimalMin(value = "0.01", message = "El precio debe ser mayor a 0")
    private BigDecimal precio;
    
    @Min(value = 0, message = "El stock no puede ser negativo")
    private int stock;
    
    @ValidCategoria
    private Categoria categoria;
    
    @NotBlank(message = "El tipo de prenda es obligatorio")
    private String tipoPrenda;
    
    private String franquicia;
    
    @NotBlank(message = "La talla es obligatoria")
    private String talla;
    
    private String color;
    private String material;
    
    @NotBlank(message = "La URL de la imagen es obligatoria")
    @Pattern(regexp = "^https?://.*\\.(?:jpg|jpeg|png|gif|webp)$", 
             message = "La URL de la imagen debe ser válida")
    private String imagenUrl;
    
    private boolean destacado;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
    
    @Builder.Default
    private boolean activo = true;
    
    /**
     * Verifica si el producto está disponible (activo y con stock).
     * @return true si el producto está disponible, false en caso contrario
     */
    public boolean isDisponible() {
        return activo && stock > 0;
    }
    
    /**
     * Reduce el stock del producto en la cantidad especificada.
     * @param cantidad la cantidad a reducir
     * @throws IllegalStateException si no hay suficiente stock
     */
    public void reducirStock(int cantidad) {
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a 0");
        }
        if (this.stock < cantidad) {
            throw new IllegalStateException("Stock insuficiente");
        }
        this.stock -= cantidad;
    }
    
    /**
     * Aumenta el stock del producto en la cantidad especificada.
     * @param cantidad la cantidad a aumentar
     * @throws IllegalArgumentException si la cantidad no es positiva
     */
    public void aumentarStock(int cantidad) {
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a 0");
        }
        this.stock += cantidad;
    }
}
