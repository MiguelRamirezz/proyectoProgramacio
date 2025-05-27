package com.example.proyectoProgramacion.repository;

import com.example.proyectoProgramacion.model.entity.Carrito;
import com.example.proyectoProgramacion.model.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio para gestionar operaciones de base de datos relacionadas con los carritos.
 */
@Repository
public interface CarritoRepository extends JpaRepository<Carrito, Long> {

    /**
     * Busca el carrito activo de un usuario.
     *
     * @param usuario El usuario propietario del carrito
     * @return Un Optional con el carrito si existe
     */
    Optional<Carrito> findByUsuario(Usuario usuario);

    /**
     * Busca el carrito activo de un usuario por su ID.
     *
     * @param usuarioId ID del usuario propietario del carrito
     * @return Un Optional con el carrito si existe
     */
    Optional<Carrito> findByUsuarioId(Long usuarioId);
}


