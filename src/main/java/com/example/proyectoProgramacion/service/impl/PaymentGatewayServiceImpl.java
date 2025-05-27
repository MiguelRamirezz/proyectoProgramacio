package com.example.proyectoProgramacion.service.impl;

import com.example.proyectoProgramacion.exception.PaymentException;
import com.example.proyectoProgramacion.service.interfaces.PaymentGatewayService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

@Service
public abstract class PaymentGatewayServiceImpl implements PaymentGatewayService {

    private static final Logger logger = LoggerFactory.getLogger(PaymentGatewayServiceImpl.class);

    // Simulación de pagos procesados para reembolsos
    private static final Map<String, BigDecimal> pagosProcesados = new HashMap<>();

    @Value("${payment.gateway.url:https://api.payment-gateway.example.com}")
    private String gatewayUrl;

    @Value("${payment.gateway.api-key:test-api-key}")
    private String apiKey;

    @Value("${payment.gateway.simulate:true}")
    private boolean simularPagos;

    @Override
    public Object procesarPago(String numeroTarjeta, String fechaExpiracion, String cvv,
                               BigDecimal monto, String referenciaPago) {
        // Validar datos de la tarjeta
        validarDatosTarjeta(numeroTarjeta, fechaExpiracion, cvv);

        // Validar monto
        if (monto == null || monto.compareTo(BigDecimal.ZERO) <= 0) {
            throw new PaymentException("El monto debe ser mayor que cero");
        }

        // En un entorno real, aquí se conectaría con el gateway de pago
        // Para este ejemplo, simulamos el procesamiento
        if (simularPagos) {
            logger.info("Simulando procesamiento de pago: {}", referenciaPago);

            // Simulamos que las tarjetas que terminan en número impar fallan
            boolean exitoso = !numeroTarjeta.endsWith("1") &&
                    !numeroTarjeta.endsWith("3") &&
                    !numeroTarjeta.endsWith("5") &&
                    !numeroTarjeta.endsWith("7") &&
                    !numeroTarjeta.endsWith("9");

            if (exitoso) {
                pagosProcesados.put(referenciaPago, monto);
                logger.info("Pago procesado exitosamente: {}", referenciaPago);
            } else {
                logger.info("Pago rechazado: {}", referenciaPago);
            }

            return exitoso;
        } else {
            // Aquí iría la implementación real con el gateway de pago
            throw new UnsupportedOperationException("Conexión con gateway de pago real no implementada");
        }
    }

    @Override
    public boolean procesarReembolso(String referenciaPago) {
        if (referenciaPago == null || referenciaPago.trim().isEmpty()) {
            throw new PaymentException("La referencia de pago no puede estar vacía");
        }

        // En un entorno real, aquí se conectaría con el gateway de pago
        // Para este ejemplo, simulamos el reembolso
        if (simularPagos) {
            logger.info("Simulando procesamiento de reembolso: {}", referenciaPago);

            // Verificar si el pago existe
            if (!pagosProcesados.containsKey(referenciaPago)) {
                logger.info("Reembolso fallido: pago no encontrado - {}", referenciaPago);
                return false;
            }

            // Eliminar el pago de los procesados (simulando el reembolso)
            pagosProcesados.remove(referenciaPago);
            logger.info("Reembolso procesado exitosamente: {}", referenciaPago);

            return true;
        } else {
            // Aquí iría la implementación real con el gateway de pago
            throw new UnsupportedOperationException("Conexión con gateway de pago real no implementada");
        }
    }

    @Override
    public String crearIntentoPago(BigDecimal monto, String descripcion) {
        // Validar monto
        if (monto == null || monto.compareTo(BigDecimal.ZERO) <= 0) {
            throw new PaymentException("El monto debe ser mayor que cero");
        }

        // En un entorno real, aquí se crearía un intento de pago con el gateway
        // Para este ejemplo, generamos un ID único
        String intentoId = "intent_" + System.currentTimeMillis();
        logger.info("Creando intento de pago: {} por {}", intentoId, monto);

        return intentoId;
    }

    // Método auxiliar para validar datos de la tarjeta
    private void validarDatosTarjeta(String numeroTarjeta, String fechaExpiracion, String cvv) {
        // Validar número de tarjeta (debe tener entre 13 y 19 dígitos)
        if (numeroTarjeta == null || !Pattern.matches("^\\d{13,19}$", numeroTarjeta)) {
            throw new PaymentException("Número de tarjeta inválido");
        }

        // Validar fecha de expiración (formato MM/YY)
        if (fechaExpiracion == null || !Pattern.matches("^(0[1-9]|1[0-2])/\\d{2}$", fechaExpiracion)) {
            throw new PaymentException("Formato de fecha de expiración inválido (debe ser MM/YY)");
        }

        // Verificar que la tarjeta no esté expirada
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/yy");
            YearMonth expirationDate = YearMonth.parse(fechaExpiracion, formatter);
            YearMonth currentDate = YearMonth.from(LocalDate.now());

            if (expirationDate.isBefore(currentDate)) {
                throw new PaymentException("La tarjeta ha expirado");
            }
        } catch (DateTimeParseException e) {
            throw new PaymentException("Error al procesar la fecha de expiración");
        }

        // Validar CVV (3 o 4 dígitos)
        if (cvv == null || !Pattern.matches("^\\d{3,4}$", cvv)) {
            throw new PaymentException("CVV inválido");
        }
    }
}


