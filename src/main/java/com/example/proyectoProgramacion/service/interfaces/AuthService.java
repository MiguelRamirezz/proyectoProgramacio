package com.example.proyectoProgramacion.service.interfaces;

import com.example.proyectoProgramacion.exception.BusinessException;
import com.example.proyectoProgramacion.exception.ResourceAlreadyExistsException;
import com.example.proyectoProgramacion.exception.ResourceNotFoundException;
import com.example.proyectoProgramacion.model.dto.auth.JwtAuthResponseDTO;
import com.example.proyectoProgramacion.model.dto.auth.LoginRequestDTO;
import com.example.proyectoProgramacion.model.dto.auth.RegistroUsuarioDTO;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

/**
 * Servicio para la autenticación y autorización de usuarios.
 * <p>
 * Proporciona operaciones para autenticar usuarios, registrar nuevos usuarios
 * y gestionar tokens de autenticación.
 *
 * <p>Ejemplo de uso:
 * <pre>{@code
 * // Autenticar usuario
 * JwtAuthResponseDTO respuesta = authService.autenticarUsuario(solicitudLogin, "192.168.1.1");
 *
 * // Registrar nuevo usuario
 * authService.registrarUsuario(solicitudRegistro);
 * }</pre>
 *
 * @see JwtAuthResponseDTO
 * @see LoginRequestDTO
 * @see RegistroUsuarioDTO
 * @since 1.0
 */
@Validated
public interface AuthService {

    /**
     * Autentica a un usuario y genera un token JWT.
     *
     * @param solicitudLogin Datos de inicio de sesión (no puede ser nulo)
     * @param direccionIp Dirección IP del cliente (no puede estar vacía)
     * @return Respuesta de autenticación con el token JWT
     * @throws BusinessException si las credenciales son inválidas
     * @throws ResourceNotFoundException si el usuario no existe
     */
    @Transactional(readOnly = true)
    JwtAuthResponseDTO autenticarUsuario(
        @NotNull(message = "{auth.solicitud.requerida}") @Valid LoginRequestDTO solicitudLogin,
        @NotBlank(message = "{auth.ip.requerida}") String direccionIp
    );

    /**
     * Verifica si existe un usuario con el nombre de usuario especificado.
     *
     * @param nombreUsuario Nombre de usuario a verificar (no puede estar vacío)
     * @return true si el usuario existe, false en caso contrario
     */
    @Transactional(readOnly = true)
    boolean existePorNombreUsuario(@NotBlank(message = "{usuario.nombre.requerido}") String nombreUsuario);

    /**
     * Verifica si existe un usuario con el correo electrónico especificado.
     *
     * @param correo Correo electrónico a verificar (no puede estar vacío)
     * @return true si el correo está en uso, false en caso contrario
     */
    @Transactional(readOnly = true)
    boolean existePorCorreo(@NotBlank(message = "{usuario.correo.requerido}") String correo);

    /**
     * Registra un nuevo usuario en el sistema.
     *
     * @param solicitudRegistro Datos de registro del usuario (no puede ser nulo)
     * @throws ResourceAlreadyExistsException si el nombre de usuario o correo ya están en uso
     * @throws BusinessException si hay un error al registrar el usuario
     */
    @Transactional
    void registrarUsuario(@NotNull(message = "{auth.solicitud.requerida}") @Valid RegistroUsuarioDTO solicitudRegistro);

    /**
     * Invalida un token JWT para cerrar la sesión del usuario.
     *
     * @param token Token JWT a invalidar (no puede estar vacío)
     * @throws BusinessException si hay un error al invalidar el token
     */
    @Transactional
    @CacheEvict(value = "tokens", key = "#token")
    void invalidarToken(@NotBlank(message = "{auth.token.requerido}") String token);
}

