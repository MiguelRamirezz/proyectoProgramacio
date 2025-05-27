package com.example.proyectoProgramacion.repository;

import com.example.proyectoProgramacion.model.entity.Orden;
import com.example.proyectoProgramacion.model.entity.Usuario;
import com.example.proyectoProgramacion.model.enums.EstadoOrden;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio para gestionar operaciones de base de datos relacionadas con las órdenes.
 */
@Repository
public interface OrdenRepository extends JpaRepository<Orden, Long> {

    /**
     * Busca todas las órdenes de un usuario específico.
     *
     * @param usuario El usuario cuyas órdenes se desean obtener
     * @return Lista de órdenes del usuario
     */
    List<Orden> findByUsuario(Usuario usuario);

    /**
     * Busca todas las órdenes de un usuario paginadas.
     *
     * @param usuario El usuario cuyas órdenes se desean obtener
     * @param pageable Configuración de paginación
     * @return Página de órdenes del usuario
     */
    Page<Orden> findByUsuario(Usuario usuario, Pageable pageable);

    /**
     * Busca todas las órdenes de un usuario por su ID.
     *
     * @param usuarioId ID del usuario cuyas órdenes se desean obtener
     * @return Lista de órdenes del usuario
     */
    List<Orden> findByUsuarioId(Long usuarioId);

    /**
     * Busca una orden por su número único.
     *
     * @param numero Número único de la orden
     * @return Un Optional con la orden si existe
     */
    Optional<Orden> findByNumero(String numero);

    /**
     * Busca órdenes por estado.
     *
     * @param estado Estado de las órdenes a buscar
     * @return Lista de órdenes con el estado especificado
     */
    List<Orden> findByEstado(EstadoOrden estado);

    /**
     * Busca órdenes creadas en un rango de fechas.
     *
     * @param inicio Fecha de inicio del rango
     * @param fin Fecha de fin del rango
     * @return Lista de órdenes creadas en el rango especificado
     */
    List<Orden> findByFechaCreacionBetween(LocalDateTime inicio, LocalDateTime fin);

    Page<Orden> findAll(Specification<Orden> spec, Pageable pageable);
}



