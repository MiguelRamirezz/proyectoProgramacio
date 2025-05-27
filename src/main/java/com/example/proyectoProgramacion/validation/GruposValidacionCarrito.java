package com.example.proyectoProgramacion.validation;

/**
 * Grupos de validación para las operaciones del carrito
 */
public interface GruposValidacionCarrito {
    /**
     * Grupo de validación para agregar productos al carrito
     */
    interface AlAgregar {}

    /**
     * Grupo de validación para actualizar cantidades en el carrito
     */
    interface AlActualizar {}

    /**
     * Grupo de validación para eliminar items del carrito
     */
    interface AlEliminar {}

    /**
     * Grupo de validación para operaciones de checkout
     */
    interface AlComprar {}
}


