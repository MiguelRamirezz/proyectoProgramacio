package com.example.proyectoProgramacion.exception;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Clase para representar respuestas de error estandarizadas en la API.
 * Se utiliza para devolver información estructurada sobre errores.
 */
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    private int status;
    private String code;
    private String message;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;

    private String path;
    private String details;
    @lombok.Setter
    private List<ValidationError> errors;

    // Constructor por defecto
    public ErrorResponse() {
        this.timestamp = LocalDateTime.now();
    }

    // Constructor con parámetros básicos
    public ErrorResponse(int status, String message) {
        this();
        this.status = status;
        this.message = message;
    }

    // Constructor completo
    public ErrorResponse(int status, String code, String message, String path) {
        this();
        this.status = status;
        this.code = code;
        this.message = message;
        this.path = path;
    }

    /**
     * Añade un error de validación.
     *
     * @param field Campo que falló la validación
     * @param message Mensaje de error
     */
    public void addValidationError(String field, String message) {
        if (errors == null) {
            errors = new ArrayList<>();
        }
        errors.add(new ValidationError(field, message));
    }

    /**
     * Añade múltiples errores de validación.
     *
     * @param validationErrors Lista de errores de validación
     */
    public void addValidationErrors(List<ValidationError> validationErrors) {
        if (errors == null) {
            errors = new ArrayList<>();
        }
        errors.addAll(validationErrors);
    }

    // Getters y setters

    public void setStatus(int status) {
        this.status = status;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    /**
         * Clase interna para representar errores de validación específicos.
         */
        @JsonInclude(JsonInclude.Include.NON_NULL)
        public record ValidationError(String field, String message) {

    }
}


