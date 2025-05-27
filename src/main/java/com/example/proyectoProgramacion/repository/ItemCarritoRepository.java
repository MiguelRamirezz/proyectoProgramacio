package com.example.proyectoProgramacion.repository;

import com.example.proyectoProgramacion.model.entity.Carrito;
import com.example.proyectoProgramacion.model.entity.ItemCarrito;
import com.example.proyectoProgramacion.model.entity.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para gestionar operaciones de base de datos relacionadas con los ítems del carrito.
 */
@Repository
public interface ItemCarritoRepository extends JpaRepository<ItemCarrito, Long> {

    /**
     * Busca todos los ítems de un carrito específico.
     *
     * @param carrito El carrito cuyos ítems se desean obtener
     * @return Lista de ítems del carrito
     */
    List<ItemCarrito> findByCarrito(Carrito carrito);

    /**
     * Busca un ítem específico en un carrito por su producto.
     *
     * @param carrito El carrito donde buscar
     * @param producto El producto a buscar en el carrito
     * @return Un Optional con el ítem si existe
     */
    Optional<ItemCarrito> findByCarritoAndProducto(Carrito carrito, Producto producto);
}


