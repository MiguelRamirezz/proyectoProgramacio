package com.example.proyectoProgramacion.service.impl;

import com.example.proyectoProgramacion.exception.BusinessException;
import com.example.proyectoProgramacion.exception.ResourceAlreadyExistsException;
import com.example.proyectoProgramacion.model.entity.Usuario;
import com.example.proyectoProgramacion.model.dto.auth.JwtAuthResponseDTO;
import com.example.proyectoProgramacion.model.dto.auth.LoginRequestDTO;
import com.example.proyectoProgramacion.model.dto.auth.RegistroUsuarioDTO;
import com.example.proyectoProgramacion.repository.UsuarioRepository;
import com.example.proyectoProgramacion.security.JwtTokenProvider;
import com.example.proyectoProgramacion.service.interfaces.AuthService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;


/**
 * Implementación del servicio de autenticación.
 */
@Slf4j
@Service
@Validated
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Autowired
    public AuthServiceImpl(
            AuthenticationManager authenticationManager,
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenProvider jwtTokenProvider) {
        this.authenticationManager = authenticationManager;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    @Transactional(readOnly = true)
    public JwtAuthResponseDTO autenticarUsuario(
            @NotNull @Valid LoginRequestDTO solicitudLogin,
            @NotBlank String direccionIp) {

        log.debug("Intentando autenticar usuario: {}", solicitudLogin.getUsername());

        try {
            // Buscar el usuario por nombre de usuario o correo electrónico
            String usernameOrEmail = solicitudLogin.getUsername();
            Usuario usuario = usuarioRepository.findByUsernameOrEmail(usernameOrEmail)
                    .orElseThrow(() -> {
                        log.warn("Usuario no encontrado: {}", usernameOrEmail);
                        return new BusinessException("Credenciales inválidas");
                    });

            // Autenticar usando el nombre de usuario real
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            usuario.getUsername(),
                            solicitudLogin.getPassword()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtTokenProvider.generateToken(authentication);
            log.info("Usuario autenticado exitosamente: {}", usuario.getUsername());

            return new JwtAuthResponseDTO(jwt);

        } catch (AuthenticationException e) {
            log.warn("Error de autenticación para el usuario: {}", solicitudLogin.getUsername());
            throw new BusinessException("Credenciales inválidas");
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error inesperado durante la autenticación", e);
            throw new BusinessException("Error al procesar la autenticación");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existePorNombreUsuario(@NotBlank String nombreUsuario) {
        log.debug("Verificando existencia de nombre de usuario: {}", nombreUsuario);
        return usuarioRepository.existsByUsername(nombreUsuario);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existePorCorreo(@NotBlank String correo) {
        log.debug("Verificando existencia de correo: {}", correo);
        return usuarioRepository.existsByEmail(correo);
    }

    @Override
    @Transactional
    public void registrarUsuario(@NotNull @Valid RegistroUsuarioDTO solicitudRegistro) {
        log.debug("Iniciando registro de usuario: {}", solicitudRegistro.getUsername());

        try {
            // Validar que el DTO no sea nulo
            if (solicitudRegistro == null) {
                throw new IllegalArgumentException("La solicitud de registro no puede ser nula");
            }


            // Validar campos obligatorios
            if (solicitudRegistro.getUsername() == null || solicitudRegistro.getUsername().trim().isEmpty()) {
                throw new IllegalArgumentException("El nombre de usuario es obligatorio");
            }
            if (solicitudRegistro.getPassword() == null || solicitudRegistro.getPassword().trim().isEmpty()) {
                throw new IllegalArgumentException("La contraseña es obligatoria");
            }
            if (solicitudRegistro.getEmail() == null || solicitudRegistro.getEmail().trim().isEmpty()) {
                throw new IllegalArgumentException("El correo electrónico es obligatorio");
            }

            // Verificar si el nombre de usuario ya existe
            if (existePorNombreUsuario(solicitudRegistro.getUsername())) {
                log.warn("El nombre de usuario ya está en uso: {}", solicitudRegistro.getUsername());
                throw new ResourceAlreadyExistsException("El nombre de usuario ya está en uso");
            }

            // Verificar si el correo ya está registrado
            if (existePorCorreo(solicitudRegistro.getEmail())) {
                log.warn("El correo electrónico ya está registrado: {}", solicitudRegistro.getEmail());
                throw new ResourceAlreadyExistsException("El correo electrónico ya está registrado");
            }

            // Validar formato de email
            if (!solicitudRegistro.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                throw new IllegalArgumentException("El formato del correo electrónico no es válido");
            }

            // Validar formato de contraseña (mínimo 6 caracteres, al menos una letra y un número)
            if (!solicitudRegistro.getPassword().matches("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{6,}$")) {
                throw new IllegalArgumentException("La contraseña debe tener al menos 6 caracteres, incluyendo letras y números");
            }

            // Crear nuevo usuario con el constructor
            Usuario usuario = new Usuario(
                solicitudRegistro.getUsername().trim(),
                passwordEncoder.encode(solicitudRegistro.getPassword()),
                solicitudRegistro.getEmail().trim().toLowerCase(),
                solicitudRegistro.getNombre() != null ? solicitudRegistro.getNombre().trim() : "",
                solicitudRegistro.getApellido() != null ? solicitudRegistro.getApellido().trim() : ""
            );

            // Configurar teléfono si está presente
            if (solicitudRegistro.getTelefono() != null && !solicitudRegistro.getTelefono().trim().isEmpty()) {
                // Validar formato de teléfono (solo números, 9 dígitos)
                if (!solicitudRegistro.getTelefono().matches("^\\d{9}$")) {
                    throw new IllegalArgumentException("El formato del teléfono no es válido. Debe tener 9 dígitos");
                }
                usuario.setTelefono(solicitudRegistro.getTelefono().trim());
            }

            // Asignar roles según el tipo de usuario
            usuario.addRol("ROLE_USER"); // Todos los usuarios tienen rol USER por defecto
            
            if (solicitudRegistro.isEsAdmin()) {
                usuario.addRol("ROLE_ADMIN");
                log.debug("Rol de administrador asignado al usuario: {}", solicitudRegistro.getUsername());
            }

            // Establecer fecha de creación
            usuario.setFechaCreacion(LocalDateTime.now());
            usuario.setFechaActualizacion(LocalDateTime.now());
            usuario.setActivo(true);

            // Validar la entidad antes de guardar
            if (usuario.getUsername() == null || usuario.getUsername().isEmpty() ||
                usuario.getPassword() == null || usuario.getPassword().isEmpty() ||
                usuario.getEmail() == null || usuario.getEmail().isEmpty()) {
                throw new IllegalArgumentException("Datos de usuario incompletos");
            }

            // Guardar el usuario
            usuario = usuarioRepository.save(usuario);
            log.info("Usuario registrado exitosamente con ID: {}", usuario.getId());

        } catch (ResourceAlreadyExistsException e) {
            log.warn("Error de registro - Recurso duplicado: {}", e.getMessage());
            throw e; // Relanzar la excepción para que el controlador la maneje
        } catch (IllegalArgumentException e) {
            log.warn("Error de validación en el registro: {}", e.getMessage());
            throw new BusinessException(e.getMessage(), e);
        } catch (Exception e) {
            log.error("Error inesperado al registrar el usuario: {}", e.getMessage(), e);
            throw new BusinessException("Error al procesar el registro. Por favor, inténtelo de nuevo más tarde.", e);
        }
    }

    @Override
    @Transactional
    @CacheEvict(value = "tokens", key = "#token")
    public void invalidarToken(@NotBlank String token) {
        log.debug("Invalidando token");
        try {
            jwtTokenProvider.invalidateToken(token);
            log.info("Token invalidado exitosamente");
        } catch (Exception e) {
            log.error("Error al invalidar el token", e);
            throw new BusinessException("Error al invalidar el token");
        }
    }
}
