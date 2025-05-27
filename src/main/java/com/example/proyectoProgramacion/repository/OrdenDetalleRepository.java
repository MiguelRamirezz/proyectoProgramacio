package com.example.proyectoProgramacion.repository;

import com.example.proyectoProgramacion.model.entity.DetalleOrden;
import com.example.proyectoProgramacion.model.entity.Orden;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio para gestionar operaciones de base de datos relacionadas con los detalles de órdenes.
 */
@Repository
public interface OrdenDetalleRepository extends JpaRepository<DetalleOrden, Long> {

    /**
     * Busca todos los detalles de una orden específica.
     *
     * @param orden La orden cuyos detalles se desean obtener
     * @return Lista de detalles de la orden
     */
    List<DetalleOrden> findByOrden(Orden orden);

    /**
     * Busca todos los detalles de una orden por su ID.
     *
     * @param ordenId ID de la orden cuyos detalles se desean obtener
     * @return Lista de detalles de la orden
     */
    List<DetalleOrden> findByOrdenId(Long ordenId);
}


