package com.example.proyectoProgramacion.service.interfaces;

import com.example.proyectoProgramacion.model.dto.orden.OrdenDTO;
import com.example.proyectoProgramacion.model.dto.orden.OrdenDetalleDTO;
import com.example.proyectoProgramacion.model.enums.EstadoOrden;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDateTime;
import java.util.List;

@Validated
public interface OrdenService {

    /**
     * Crea una nueva orden para un usuario a partir de su carrito
     * @param nombreUsuario nombre del usuario (no debe estar en blanco)
     * @param direccionEnvio dirección de envío (no debe estar en blanco)
     * @return DTO con la información de la orden creada
     * @throws jakarta.persistence.EntityNotFoundException si el usuario no existe
     * @throws IllegalStateException si el carrito está vacío
     */
    OrdenDTO crearOrden(@NotBlank String nombreUsuario, @NotBlank String direccionEnvio);

    /**
     * Obtiene una orden por su ID
     * @param ordenId ID de la orden (debe ser positivo)
     * @return DTO con la información de la orden
     * @throws jakarta.persistence.EntityNotFoundException si la orden no existe
     */
    OrdenDTO obtenerOrdenPorId(@NotNull @Positive Long ordenId);

    /**
     * Obtiene todas las órdenes de un usuario
     * @param nombreUsuario nombre del usuario (no debe estar en blanco)
     * @param pageable información de paginación (no nulo)
     * @return Página con las órdenes del usuario
     * @throws jakarta.persistence.EntityNotFoundException si el usuario no existe
     */
    Page<OrdenDTO> obtenerOrdenesPorUsuario(@NotBlank String nombreUsuario, @NotNull Pageable pageable);

    /**
     * Obtiene todas las órdenes con paginación
     * @param pageable información de paginación (no nulo)
     * @return Página con todas las órdenes
     * @cacheable Los resultados se almacenan en caché con la clave 'todasOrdenes_<página>_<tamaño>'
     */
    @Cacheable(cacheNames = "todasOrdenes", key = "#pageable.pageNumber + '_' + #pageable.pageSize")
    Page<OrdenDTO> obtenerTodasLasOrdenes(@NotNull Pageable pageable);

    /**
     * Busca órdenes por nombre de usuario (alias de obtenerOrdenesPorUsuario)
     * @param username nombre de usuario (no debe estar en blanco)
     * @param pageable información de paginación (no nulo)
     * @return Página con las órdenes del usuario
     * @deprecated Usar {@link #obtenerOrdenesPorUsuario(String, Pageable)} en su lugar
     */
    @Deprecated(since = "1.0", forRemoval = true)
    default Page<OrdenDTO> findAllByUsername(@NotBlank String username, @NotNull Pageable pageable) {
        return obtenerOrdenesPorUsuario(username, pageable);
    }

    /**
     * Actualiza el estado de una orden
     * @param ordenId ID de la orden (debe ser positivo)
     * @param estado nuevo estado (no nulo)
     * @return DTO con la información de la orden actualizada
     * @throws jakarta.persistence.EntityNotFoundException si la orden no existe
     * @throws IllegalStateException si el cambio de estado no es válido
     */
    OrdenDTO actualizarEstadoOrden(@NotNull @Positive Long ordenId, @NotNull EstadoOrden estado);

    /**
     * Cancela una orden
     * @param ordenId ID de la orden (debe ser positivo)
     * @param nombreUsuario nombre del usuario (no debe estar en blanco)
     * @return DTO con la información de la orden cancelada
     * @throws jakarta.persistence.EntityNotFoundException si la orden o el usuario no existen
     * @throws IllegalStateException si la orden no puede ser cancelada
     * @throws SecurityException si el usuario no tiene permiso para cancelar la orden
     */
    OrdenDTO cancelarOrden(@NotNull @Positive Long ordenId, @NotBlank String nombreUsuario);

    /**
     * Busca órdenes por diferentes criterios
     * @param estado estado de la orden (opcional)
     * @param fechaInicio fecha de inicio (opcional, debe ser anterior o igual a fechaFin)
     * @param fechaFin fecha de fin (opcional, debe ser posterior o igual a fechaInicio)
     * @param pageable información de paginación (no nulo)
     * @return Página con las órdenes que cumplen los criterios
     * @throws IllegalArgumentException si las fechas no son válidas
     */
    Page<OrdenDTO> buscarOrdenes(EstadoOrden estado, LocalDateTime fechaInicio,
                                 LocalDateTime fechaFin, @NotNull Pageable pageable);

    /**
     * Verifica si una orden pertenece a un usuario
     * @param ordenId ID de la orden (debe ser positivo)
     * @param nombreUsuario nombre del usuario (no debe estar en blanco)
     * @return true si la orden pertenece al usuario, false en caso contrario
     * @throws jakarta.persistence.EntityNotFoundException si la orden no existe
     */
    boolean verificarPropiedadOrden(@NotNull @Positive Long ordenId, @NotBlank String nombreUsuario);

    /**
     * Obtiene los detalles de una orden
     * @param ordenId ID de la orden (debe ser positivo)
     * @return Lista de detalles de la orden
     * @throws jakarta.persistence.EntityNotFoundException si la orden no existe
     */
    List<OrdenDetalleDTO> obtenerDetallesOrden(@NotNull @Positive Long ordenId);
    
    /**
     * Cuenta el total de órdenes en el sistema
     * @return número total de órdenes
     */
    long contarOrdenesTotales();
    
    /**
     * Obtiene las órdenes recientes
     * @param limite número máximo de órdenes a devolver (debe ser positivo)
     * @return Lista de órdenes recientes
     */
    List<OrdenDTO> obtenerOrdenesRecientes(@Positive int limite);
}
