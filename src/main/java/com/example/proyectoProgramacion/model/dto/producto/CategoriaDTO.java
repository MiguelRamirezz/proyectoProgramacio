package com.example.proyectoProgramacion.model.dto.producto;

import com.example.proyectoProgramacion.model.enums.Categoria;
import com.example.proyectoProgramacion.validation.ValidCategoria;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO que representa una categoría de producto en el sistema.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO que representa una categoría de producto")
public class CategoriaDTO {

    @NotNull(message = "La categoría no puede ser nula")
    @ValidCategoria
    @Schema(description = "Tipo de categoría", required = true, example = "CAMISETAS")
    private Categoria categoria;
    
    @NotBlank(message = "La descripción no puede estar vacía")
    @Size(min = 10, max = 500, message = "La descripción debe tener entre 10 y 500 caracteres")
    @Schema(description = "Descripción detallada de la categoría", 
            required = true, 
            example = "Camisetas de manga corta de diferentes estilos y colores")
    private String descripcion;
    
    @Schema(description = "URL de la imagen representativa de la categoría", 
            example = "https://ejemplo.com/imagenes/camisetas.jpg")
    private String imagenUrl;
    
    @Builder.Default
    @Schema(description = "Indica si la categoría está activa en el sistema", 
            example = "true")
    private boolean activa = true;
    
    /**
     * Obtiene el nombre legible de la categoría.
     * @return el nombre legible de la categoría
     */
    public String getNombreLegible() {
        if (categoria == null) {
            return "";
        }
        String nombre = categoria.name().toLowerCase();
        return nombre.substring(0, 1).toUpperCase() + nombre.substring(1);
    }
    
    /**
     * Valida si la categoría está activa.
     * @return true si la categoría está activa, false en caso contrario
     */
    public boolean isActiva() {
        return activa;
    }
}
