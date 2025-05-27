package com.example.proyectoProgramacion.service.impl;

import com.example.proyectoProgramacion.service.interfaces.PaymentGatewayService;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
public abstract class StripePaymentGatewayClientImpl implements PaymentGatewayService {

    @Value("${stripe.api.key}")
    private String stripeApiKey;

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeApiKey;
    }

    @Override
    public String crearIntentoPago(BigDecimal monto, String moneda) {
        return crearIntentoPago(monto, moneda, null).toString();
    }

    public Map<String, Object> crearIntentoPago(BigDecimal monto, String moneda, String descripcion) {
        try {
            // Convertir BigDecimal a long (centavos)
            long montoEnCentavos = monto.multiply(new BigDecimal(100)).longValue();

            PaymentIntentCreateParams.Builder paramsBuilder = PaymentIntentCreateParams.builder()
                    .setAmount(montoEnCentavos)
                    .setCurrency(moneda)
                    .setAutomaticPaymentMethods(
                            PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                    .setEnabled(true)
                                    .build()
                    );

            if (descripcion != null && !descripcion.isEmpty()) {
                paramsBuilder.setDescription(descripcion);
            }

            PaymentIntent paymentIntent = PaymentIntent.create(paramsBuilder.build());

            Map<String, Object> respuesta = new HashMap<>();
            respuesta.put("id", paymentIntent.getId());
            respuesta.put("clientSecret", paymentIntent.getClientSecret());
            respuesta.put("status", paymentIntent.getStatus());
            respuesta.put("amount", paymentIntent.getAmount());
            respuesta.put("currency", paymentIntent.getCurrency());

            return respuesta;
        } catch (StripeException e) {
            throw new RuntimeException("Error al crear intento de pago en Stripe: " + e.getMessage(), e);
        }
    }

    @Override
    public Map<String, Object> confirmarPago(String intentoId) {
        try {
            PaymentIntent paymentIntent = PaymentIntent.retrieve(intentoId);

            Map<String, Object> respuesta = new HashMap<>();
            respuesta.put("id", paymentIntent.getId());
            respuesta.put("status", paymentIntent.getStatus());
            respuesta.put("successful", "succeeded".equals(paymentIntent.getStatus()));

            return respuesta;
        } catch (StripeException e) {
            throw new RuntimeException("Error al confirmar pago en Stripe: " + e.getMessage(), e);
        }
    }

    @Override
    public Object procesarPago(String numeroTarjeta, String fechaExp, String cvv, BigDecimal monto, String moneda) {
        try {
            // En una implementación real, aquí se procesaría el pago con los datos de la tarjeta
            // Por seguridad, es mejor usar el client secret en el frontend

            // Esta es una simulación simplificada
            Map<String, Object> respuesta = new HashMap<>();
            respuesta.put("successful", true);
            respuesta.put("referencia", "STRIPE-" + System.currentTimeMillis());
            respuesta.put("mensaje", "Pago procesado con éxito");

            return respuesta;
        } catch (Exception e) {
            throw new RuntimeException("Error al procesar pago con tarjeta: " + e.getMessage(), e);
        }
    }

    @Override
    public Map<String, Object> procesarPago(Map<String, Object> datosPago) {
        try {
            // Extraer datos del mapa
            String numeroTarjeta = (String) datosPago.get("numeroTarjeta");
            String fechaExp = (String) datosPago.get("fechaExpiracion");
            String cvv = (String) datosPago.get("cvv");
            BigDecimal monto;
            if (datosPago.get("monto") instanceof BigDecimal) {
                monto = (BigDecimal) datosPago.get("monto");
            } else if (datosPago.get("monto") instanceof Number) {
                monto = new BigDecimal(datosPago.get("monto").toString());
            } else {
                monto = new BigDecimal(datosPago.get("monto").toString());
            }
            String moneda = (String) datosPago.get("moneda");

            // Procesar el pago utilizando el otro método
            Object resultado = procesarPago(numeroTarjeta, fechaExp, cvv, monto, moneda);
            
            // Verificar que el resultado no sea nulo
            if (resultado == null) {
                throw new RuntimeException("El resultado del pago es nulo");
            }
            
            // Si el resultado ya es un Map, devolverlo directamente
            if (resultado instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> resultadoMap = (Map<String, Object>) resultado;
                return resultadoMap;
            }
            
            // Si no es un Map, crear uno nuevo con el resultado
            Map<String, Object> respuesta = new HashMap<>();
            respuesta.put("successful", true);
            respuesta.put("referencia", "STRIPE-" + System.currentTimeMillis());
            respuesta.put("mensaje", "Pago procesado con éxito");
            respuesta.put("resultado", resultado);
            return respuesta;
        } catch (Exception e) {
            throw new RuntimeException("Error al procesar pago con datos en mapa: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean verificarPago(String referencia) {
        try {
            // En una implementación real, aquí se verificaría el estado del pago en Stripe
            // Simulación simplificada
            return true;
        } catch (Exception e) {
            throw new RuntimeException("Error al verificar pago: " + e.getMessage(), e);
        }
    }

    @Override
    public Map<String, Object> verificarEstadoPago(String idPago) {
        try {
            // Implementación para verificar el estado detallado del pago
            PaymentIntent paymentIntent = PaymentIntent.retrieve(idPago);

            Map<String, Object> respuesta = new HashMap<>();
            respuesta.put("id", paymentIntent.getId());
            respuesta.put("status", paymentIntent.getStatus());
            respuesta.put("amount", paymentIntent.getAmount());
            respuesta.put("currency", paymentIntent.getCurrency());
            respuesta.put("created", paymentIntent.getCreated());
            respuesta.put("successful", "succeeded".equals(paymentIntent.getStatus()));

            return respuesta;
        } catch (StripeException e) {
            throw new RuntimeException("Error al verificar estado del pago: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean procesarReembolso(String idTransaccion) {
        try {
            // Implementación del reembolso usando la API de Stripe
            // En una implementación real, aquí se procesaría el reembolso
            // y se devolvería true si el reembolso fue exitoso, false en caso contrario
            
            // Simulación de reembolso exitoso
            return true;
        } catch (Exception e) {
            throw new RuntimeException("Error al procesar reembolso: " + e.getMessage(), e);
        }
    }

    @Override
    public Map<String, Object> procesarReembolso(String idPago, BigDecimal monto) {
        try {
            // Implementación del reembolso parcial usando la API de Stripe
            // En una implementación real, aquí se procesaría el reembolso con un monto específico

            Map<String, Object> respuesta = new HashMap<>();
            respuesta.put("successful", true);
            respuesta.put("idReembolso", "REFUND-PARTIAL-" + System.currentTimeMillis());
            respuesta.put("mensaje", "Reembolso parcial procesado con éxito");
            respuesta.put("monto", monto);
            respuesta.put("idPagoOriginal", idPago);

            return respuesta;
        } catch (Exception e) {
            throw new RuntimeException("Error al procesar reembolso parcial: " + e.getMessage(), e);
        }
    }
}




