package com.example.proyectoProgramacion.exception;

import lombok.Getter;

/**
 * Excepción personalizada para errores relacionados con el procesamiento de pagos.
 * Esta excepción se lanza cuando ocurren problemas durante las operaciones de pago,
 * como validación de tarjetas, conexión con la pasarela de pago, o errores en el
 * procesamiento de transacciones.
 */
@Getter
public class PaymentException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Código de error asociado con la excepción
     * -- GETTER --
     *  Obtiene el código de error asociado con esta excepción.
     *
     * @return el código de error, o null si no se ha especificado

     */
    private String errorCode;

    /**
     * Construye una nueva excepción de pago con el mensaje de error especificado.
     *
     * @param message el mensaje detallado del error
     */
    public PaymentException(String message) {
        super(message);
    }

    /**
     * Construye una nueva excepción de pago con el mensaje y la causa especificados.
     *
     * @param message el mensaje detallado del error
     * @param cause la causa raíz de la excepción
     */
    public PaymentException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Construye una nueva excepción de pago con el código de error y el mensaje especificados.
     *
     * @param errorCode el código de error asociado con la excepción
     * @param message el mensaje detallado del error
     */
    public PaymentException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    /**
     * Construye una nueva excepción de pago con el código de error, el mensaje y la causa especificados.
     *
     * @param errorCode el código de error asociado con la excepción
     * @param message el mensaje detallado del error
     * @param cause la causa raíz de la excepción
     */
    public PaymentException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

}

