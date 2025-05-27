package com.example.proyectoProgramacion.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

/**
 * Anotación para validar que una categoría sea válida.
 * Se asegura de que el valor esté presente en el enum Categoria.
 */
@Documented
@Constraint(validatedBy = CategoriaValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidCategoria {
    
    /**
     * Mensaje de error por defecto.
     */
    String message() default "Categoría no válida";
    
    /**
     * Grupos de validación.
     */
    Class<?>[] groups() default {};
    
    /**
     * Payload para la validación.
     */
    Class<? extends Payload>[] payload() default {};
}
