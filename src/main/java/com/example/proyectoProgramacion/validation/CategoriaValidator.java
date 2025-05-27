package com.example.proyectoProgramacion.validation;

import com.example.proyectoProgramacion.model.enums.Categoria;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validador para la anotación @ValidCategoria.
 * Verifica que el valor de la categoría sea uno de los valores permitidos en el enum Categoria.
 */
public class CategoriaValidator implements ConstraintValidator<ValidCategoria, Categoria> {

    /**
     * Inicializa el validador.
     * @param constraintAnnotation la anotación de restricción
     */
    @Override
    public void initialize(ValidCategoria constraintAnnotation) {
        // No se necesita inicialización adicional
    }

    /**
     * Valida que la categoría sea válida.
     * @param categoria la categoría a validar
     * @param context el contexto de validación
     * @return true si la categoría es válida, false en caso contrario
     */
    @Override
    public boolean isValid(Categoria categoria, ConstraintValidatorContext context) {
        // Si la categoría es nula, se considera válida (usar @NotNull si es requerida)
        if (categoria == null) {
            return true;
        }
        
        // Verificar si la categoría es uno de los valores del enum
        try {
            Categoria.valueOf(categoria.name());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
