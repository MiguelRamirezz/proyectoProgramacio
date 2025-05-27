package com.example.proyectoProgramacion.repository;

import com.example.proyectoProgramacion.model.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio para gestionar operaciones de base de datos relacionadas con los usuarios.
 */
@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    /**
     * Busca un usuario por su nombre de usuario o correo electrónico.
     *
     * @param usernameOrEmail Nombre de usuario o correo electrónico a buscar
     * @return Un Optional con el usuario si existe
     */
    @Query("SELECT u FROM Usuario u WHERE u.username = :usernameOrEmail OR u.email = :usernameOrEmail")
    Optional<Usuario> findByUsernameOrEmail(@Param("usernameOrEmail") String usernameOrEmail);
    
    /**
     * Busca un usuario por su nombre de usuario.
     *
     * @param username Nombre de usuario a buscar
     * @return Un Optional con el usuario si existe
     */
    Optional<Usuario> findByUsername(String username);

    /**
     * Busca un usuario por su correo electrónico.
     *
     * @param email Correo electrónico a buscar
     * @return Un Optional con el usuario si existe
     */
    Optional<Usuario> findByEmail(String email);

    /**
     * Verifica si existe un usuario con el nombre de usuario especificado.
     *
     * @param username Nombre de usuario a verificar
     * @return true si existe, false en caso contrario
     */
    boolean existsByUsername(String username);

    /**
     * Verifica si existe un usuario con el correo electrónico especificado.
     *
     * @param email Correo electrónico a verificar
     * @return true si existe, false en caso contrario
     */
    boolean existsByEmail(String email);
    
    /**
     * Busca usuarios que coincidan con el término de búsqueda en nombre, apellido, email o username.
     * La búsqueda no distingue entre mayúsculas y minúsculas.
     *
     * @param nombre Término de búsqueda para el nombre
     * @param apellido Término de búsqueda para el apellido
     * @param email Término de búsqueda para el email
     * @param username Término de búsqueda para el username
     * @param pageable Configuración de paginación
     * @return Página de usuarios que coinciden con los criterios de búsqueda
     */
    @Query("SELECT u FROM Usuario u WHERE " +
           "LOWER(u.nombre) LIKE LOWER(:nombre) OR " +
           "LOWER(u.apellido) LIKE LOWER(:apellido) OR " +
           "LOWER(u.email) LIKE LOWER(:email) OR " +
           "LOWER(u.username) LIKE LOWER(:username)")
    Page<Usuario> findByNombreContainingIgnoreCaseOrApellidoContainingIgnoreCaseOrEmailContainingIgnoreCaseOrUsernameContainingIgnoreCase(
        @Param("nombre") String nombre,
        @Param("apellido") String apellido,
        @Param("email") String email,
        @Param("username") String username,
        Pageable pageable);
}


