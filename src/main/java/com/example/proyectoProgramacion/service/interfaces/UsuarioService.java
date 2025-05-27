package com.example.proyectoProgramacion.service.interfaces;

import com.example.proyectoProgramacion.model.dto.usuario.CambioPasswordDTO;
import com.example.proyectoProgramacion.model.dto.usuario.UsuarioDTO;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Set;


@Validated
public interface UsuarioService {

    /**
     * Obtiene un usuario por su nombre de usuario
     * @param username nombre de usuario
     * @return DTO con la información del usuario
     */
    UsuarioDTO obtenerUsuarioPorUsername(@NotBlank String username);

    /**
     * Obtiene un usuario por su ID
     * @param id ID del usuario
     * @return DTO con la información del usuario
     */
    UsuarioDTO obtenerUsuarioPorId(@NotNull Long id);

    /**
     * Actualiza la información de un usuario
     * @param username nombre de usuario
     * @param usuarioDTO datos actualizados del usuario
     * @return DTO con la información del usuario actualizado
     */
    UsuarioDTO actualizarUsuario(@NotBlank String username, @NotNull @Valid UsuarioDTO usuarioDTO);

    /**
     * Cambia la contraseña de un usuario
     * @param username nombre de usuario
     * @param cambioPasswordDTO datos para el cambio de contraseña
     */
    void cambiarPassword(@NotBlank String username, @NotNull @Valid CambioPasswordDTO cambioPasswordDTO);

    /**
     * Desactiva una cuenta de usuario
     * @param username nombre de usuario
     */
    void desactivarCuenta(@NotBlank String username);

    /**
     * Activa una cuenta de usuario
     * @param username nombre de usuario
     */
    void activarCuenta(@NotBlank String username);

    /**
     * Obtiene todos los usuarios con paginación
     * @param pageable información de paginación
     * @return Página con todos los usuarios
     * @cacheable Los resultados se almacenan en caché con la clave 'todosUsuarios'
     */
    @Cacheable(cacheNames = "todosUsuarios", key = "#pageable.pageNumber + '_' + #pageable.pageSize")
    Page<UsuarioDTO> obtenerTodosLosUsuarios(Pageable pageable);

    /**
     * Busca usuarios por término de búsqueda
     * @param termino término de búsqueda
     * @param pageable información de paginación
     * @return Página con los usuarios que coinciden con el término
     */
    Page<UsuarioDTO> buscarUsuarios(String termino, Pageable pageable);
    
    /**
     * Elimina un usuario por su ID
     * @param id ID del usuario a eliminar
     */
    void eliminarUsuario(@NotNull Long id);
}



