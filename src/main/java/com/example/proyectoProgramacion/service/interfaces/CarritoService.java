package com.example.proyectoProgramacion.service.interfaces;

import com.example.proyectoProgramacion.model.dto.carrito.CarritoDTO;
import com.example.proyectoProgramacion.model.dto.carrito.ItemCarritoDTO;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;


@Validated
public interface CarritoService {

    /**
     * Fusiona el carrito de sesión con el carrito del usuario autenticado
     * @apiNote Este método limpia automáticamente el carrito de sesión después de la fusión
     */
    @Retryable(
            value = { Exception.class },
            maxAttempts = 3,
            backoff = @Backoff(delay = 100)
    )
    @Transactional(
            isolation = Isolation.SERIALIZABLE,
            timeout = 10,
            rollbackFor = {Exception.class}
    )
    CarritoDTO fusionarCarritoSesionConUsuario(@NotBlank String nombreUsuario);

    // Métodos para usuarios autenticados
    CarritoDTO obtenerCarritoPorUsuario(@NotBlank String nombreUsuario);

    CarritoDTO agregarProductoAlCarrito(@NotBlank String nombreUsuario, @NotNull ItemCarritoDTO item);

    CarritoDTO actualizarCantidadItem(@NotBlank String nombreUsuario, @NotNull Long itemId, @NotNull Integer cantidad);

    boolean verificarPropiedadItem(@NotNull Long itemId, @NotBlank String nombreUsuario);

    CarritoDTO eliminarItemDelCarrito(@NotBlank String nombreUsuario, @NotNull Long itemId);

    CarritoDTO vaciarCarrito(@NotBlank String nombreUsuario);

    // Métodos para usuarios no autenticados (sesión)
    CarritoDTO obtenerCarritoSesion();

    CarritoDTO agregarProductoAlCarritoSesion(@NotNull ItemCarritoDTO item);

    CarritoDTO actualizarCantidadItemSesion(@NotNull Long itemId, @NotNull Integer cantidad);

    CarritoDTO eliminarItemDelCarritoSesion(@NotNull Long itemId);

    CarritoDTO vaciarCarritoSesion();
}





