package com.example.proyectoProgramacion.model.enums;

import lombok.Getter;

/**
 * Enumeración que representa los posibles estados de una orden.
 */
@Getter
public enum EstadoOrden {
    COMPLETADA("Completada"),
    CANCELADA("Cancelada"),
    PENDIENTE("Pendiente"),
    PAGADA("Pagada"),
    PAGO_PARCIAL("Pago parcial"),
    EN_PROCESO("En proceso"),
    ENVIADA("Enviada"),
    ENTREGADA("Entregada"),
    DEVUELTA("Devuelta");

    private final String descripcion;

    EstadoOrden(String descripcion) {
        this.descripcion = descripcion;
    }

}

