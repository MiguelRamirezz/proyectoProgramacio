package com.example.proyectoProgramacion.model.dto.orden;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

import java.math.BigDecimal;

/**
 * DTO que representa un detalle de orden (línea de producto).
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrdenDetalleDTO {

    private Long id;

    @NotNull(message = "El ID del producto es obligatorio")
    private Long productoId;

    @NotBlank(message = "El nombre del producto es obligatorio")
    private String nombreProducto;

    private String imagenProducto;

    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 1, message = "La cantidad mínima es 1")
    private Integer cantidad;

    @NotNull(message = "El precio es obligatorio")
    @PositiveOrZero(message = "El precio no puede ser negativo")
    private BigDecimal precio;

    @NotNull(message = "El subtotal es obligatorio")
    @PositiveOrZero(message = "El subtotal no puede ser negativo")
    private BigDecimal subtotal;

    private String sku;

    @NotNull(message = "El ID de la orden es obligatorio")
    private Long ordenId;

    /**
     * Calcula el subtotal basado en la cantidad y el precio.
     * @return el subtotal calculado
     */
    public BigDecimal calcularSubtotal() {
        if (cantidad == null || precio == null) {
            return BigDecimal.ZERO;
        }
        return precio.multiply(BigDecimal.valueOf(cantidad));
    }

    /**
     * Establece el precio y recalcula automáticamente el subtotal.
     * @param precio el nuevo precio
     */
    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
        if (cantidad != null && precio != null) {
            this.subtotal = calcularSubtotal();
        }
    }

    /**
     * Establece la cantidad y recalcula automáticamente el subtotal.
     * @param cantidad la nueva cantidad
     */
    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
        if (precio != null && cantidad != null) {
            this.subtotal = calcularSubtotal();
        }
    }
}
