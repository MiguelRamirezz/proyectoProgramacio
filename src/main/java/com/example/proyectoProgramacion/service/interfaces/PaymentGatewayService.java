package com.example.proyectoProgramacion.service.interfaces;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Interfaz para el servicio de pasarela de pagos
 */
public interface PaymentGatewayService {
    /**
     * Crea un intento de pago
     * @param monto monto a pagar
     * @param moneda código de moneda
     * @param descripcion descripción del pago
     * @return información del intento de pago
     */
    Map<String, Object> crearIntentoPago(BigDecimal monto, String moneda, String descripcion);

    /**
     * Confirma un pago previamente iniciado
     * @param intentoId identificador del intento de pago
     * @return resultado de la confirmación
     */
    Map<String, Object> confirmarPago(String intentoId);

    /**
     * Procesa un pago con tarjeta de crédito
     *
     * @param numeroTarjeta número de tarjeta
     * @param fechaExp      fecha de expiración en formato MM/YY
     * @param cvv           código de seguridad
     * @param monto         monto a pagar
     * @param moneda        código de moneda
     * @return resultado del procesamiento del pago
     */
    Object procesarPago(String numeroTarjeta, String fechaExp, String cvv, BigDecimal monto, String moneda);

    /**
     * Verifica el estado de un pago
     * @param referencia referencia del pago
     * @return true si el pago está confirmado, false en caso contrario
     */
    boolean verificarPago(String referencia);

    /**
     * Procesa un reembolso simple
     *
     * @param idTransaccion identificador de la transacción a reembolsar
     * @return información del reembolso procesado
     */
    boolean procesarReembolso(String idTransaccion);

    /**
     * Procesa un pago con datos en formato Map
     * @param datosPago mapa con los datos del pago
     * @return resultado del procesamiento del pago
     */
    @Retryable(
            value = {RuntimeException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000)
    )
    Map<String, Object> procesarPago(@NotNull @Valid Map<String, Object> datosPago);

    /**
     * Verifica el estado detallado de un pago
     * @param idPago identificador del pago
     * @return información detallada del estado del pago
     */
    Map<String, Object> verificarEstadoPago(@NotBlank String idPago);

    /**
     * Procesa un reembolso con monto específico
     * @param idPago identificador del pago a reembolsar
     * @param monto monto a reembolsar
     * @return información del reembolso procesado
     */
    @Retryable(
            value = {RuntimeException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000)
    )
    Map<String, Object> procesarReembolsoMonto(
            @NotBlank String idPago,
            @NotNull BigDecimal monto);

    @Retryable(
            value = {RuntimeException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000)
    )
    Map<String, Object> procesarReembolso(
            @NotBlank String idPago,
            @NotNull BigDecimal monto);

    String crearIntentoPago(BigDecimal monto, String descripcion);
}



