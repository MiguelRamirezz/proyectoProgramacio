package com.example.proyectoProgramacion.model.dto.carrito;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO que representa un carrito de compras.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CarritoDTO {
    
    @NotNull(message = "El ID del carrito es obligatorio")
    private Long id;
    
    @NotNull(message = "El ID del usuario es obligatorio")
    private Long usuarioId;
    
    @NotNull(message = "El nombre de usuario es obligatorio")
    private String username;
    
    @Valid
    private List<ItemCarritoDTO> items = new ArrayList<>();
    
    @NotNull(message = "El total es obligatorio")
    private BigDecimal total = BigDecimal.ZERO;
    
    @NotNull(message = "La cantidad de ítems es obligatoria")
    private int cantidadItems = 0;
    
    /**
     * Calcula el total del carrito sumando los subtotales de los ítems.
     * @return El total calculado
     */
    public BigDecimal calcularTotal() {
        if (items == null || items.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return items.stream()
                .map(ItemCarritoDTO::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    /**
     * Actualiza la cantidad de ítems en el carrito.
     */
    public void actualizarCantidadItems() {
        if (items == null) {
            this.cantidadItems = 0;
        } else {
            this.cantidadItems = items.size();
        }
    }
}
