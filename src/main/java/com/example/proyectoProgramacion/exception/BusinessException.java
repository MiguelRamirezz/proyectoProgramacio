package com.example.proyectoProgramacion.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.time.LocalDateTime;

/**
 * Excepción para errores de lógica de negocio.
 * Se utiliza para indicar condiciones de error esperadas en la lógica de negocio.
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class BusinessException extends RuntimeException {

    private final String code;
    private final LocalDateTime timestamp;
    private final String details;

    /**
     * Constructor con mensaje de error.
     *
     * @param message Mensaje descriptivo del error
     */
    public BusinessException(String message) {
        super(message);
        this.code = "BUSINESS_ERROR";
        this.timestamp = LocalDateTime.now();
        this.details = null;
    }

    /**
     * Constructor con mensaje de error y código personalizado.
     *
     * @param message Mensaje descriptivo del error
     * @param code Código de error personalizado
     */
    public BusinessException(String message, String code) {
        super(message);
        this.code = code;
        this.timestamp = LocalDateTime.now();
        this.details = null;
    }

    /**
     * Constructor completo con mensaje, código y detalles.
     *
     * @param message Mensaje descriptivo del error
     * @param code Código de error personalizado
     * @param details Detalles adicionales sobre el error
     */
    public BusinessException(String message, String code, String details) {
        super(message);
        this.code = code;
        this.timestamp = LocalDateTime.now();
        this.details = details;
    }

    /**
     * Constructor con causa raíz.
     *
     * @param message Mensaje descriptivo del error
     * @param cause Excepción que causó este error
     */
    public BusinessException(String message, Throwable cause) {
        super(message, cause);
        this.code = "BUSINESS_ERROR";
        this.timestamp = LocalDateTime.now();
        this.details = cause.getMessage();
    }

    /**
     * @return Código de error
     */
    public String getCode() {
        return code;
    }

    /**
     * @return Timestamp de cuando ocurrió el error
     */
    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    /**
     * @return Detalles adicionales del error
     */
    public String getDetails() {
        return details;
    }
}
