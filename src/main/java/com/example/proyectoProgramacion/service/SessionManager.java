package com.example.proyectoProgramacion.service;

import com.example.proyectoProgramacion.model.dto.carrito.CarritoDTO;
import lombok.Setter;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;

/**
 * Gestor de sesión para almacenar datos del carrito de compras
 * para usuarios no autenticados
 */
@Setter
@Component
@SessionScope
public class SessionManager implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * -- SETTER --
     *  Establece el carrito de la sesión actual
     *
     * @param carritoSesion CarritoDTO con los items del carrito
     */
    private CarritoDTO carritoSesion;

    /**
     * Obtiene el carrito de la sesión actual
     * @return CarritoDTO con los items del carrito de sesión
     */
    public CarritoDTO getCarritoSesion() {
        if (carritoSesion == null) {
            carritoSesion = new CarritoDTO();
            carritoSesion.setItems(new ArrayList<>());
        }
        return carritoSesion;
    }

    /**
     * Limpia todos los datos de la sesión
     */
    public void limpiarSesion() {
        this.carritoSesion = null;
    }
}

