package com.example.proyectoProgramacion.service.interfaces;

import com.example.proyectoProgramacion.model.dto.pago.PagoDTO;
import com.example.proyectoProgramacion.model.dto.pago.PagoRequestDTO;
import com.example.proyectoProgramacion.model.dto.pago.PagoResponseDTO;
import com.example.proyectoProgramacion.model.enums.EstadoPago;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Validated
public interface PagoService {

    /**
     * Procesa un pago para una orden
     * @param pagoRequestDTO datos del pago
     * @param nombreUsuario nombre del usuario
     * @return respuesta del procesamiento del pago
     */
    PagoResponseDTO procesarPago(@NotNull @Valid PagoRequestDTO pagoRequestDTO, @NotBlank String nombreUsuario);

    /**
     * Obtiene un pago por su ID
     *
     * @param pagoId ID del pago
     * @return DTO con la información del pago
     */
    Optional<PagoDTO> obtenerPagoPorId(@NotNull Long pagoId);

    @Transactional(readOnly = true)
    List<PagoDTO> obtenerPagosPorUsuario(Long usuarioId);

    /**
     * Obtiene todos los pagos de un usuario
     * @param nombreUsuario nombre del usuario
     * @param pageable información de paginación
     * @return Página con los pagos del usuario
     */
    Page<PagoDTO> obtenerPagosPorUsuario(@NotBlank String nombreUsuario, Pageable pageable);

    /**
     * Obtiene todos los pagos
     * @param pageable información de paginación
     * @return Página con todos los pagos
     */
    Page<PagoDTO> obtenerTodosLosPagos(Pageable pageable);

    /**
     * Actualiza el estado de un pago
     * @param pagoId ID del pago
     * @param estado nuevo estado
     * @return DTO con la información del pago actualizado
     */
    PagoDTO actualizarEstadoPago(@NotNull Long pagoId, @NotNull EstadoPago estado);

    /**
     * Busca pagos por diferentes criterios
     * @param estado estado del pago (opcional)
     * @param fechaInicio fecha de inicio (opcional)
     * @param fechaFin fecha de fin (opcional)
     * @param pageable información de paginación
     * @return Página con los pagos que cumplen los criterios
     */
    Page<PagoDTO> buscarPagos(EstadoPago estado, LocalDateTime fechaInicio,
                              LocalDateTime fechaFin, Pageable pageable);

    /**
     * Verifica si un pago pertenece a un usuario
     * @param pagoId ID del pago
     * @param nombreUsuario nombre del usuario
     * @return true si el pago pertenece al usuario, false en caso contrario
     */
    boolean verificarPropiedadPago(@NotNull Long pagoId, @NotBlank String nombreUsuario);

    /**
     * Reembolsa un pago
     * @param pagoId ID del pago
     * @param nombreUsuario nombre del usuario
     * @return DTO con la información del pago reembolsado
     */
    PagoDTO reembolsarPago(@NotNull Long pagoId, @NotBlank String nombreUsuario);
}





