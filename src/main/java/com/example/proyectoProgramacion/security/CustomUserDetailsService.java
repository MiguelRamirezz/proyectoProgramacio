package com.example.proyectoProgramacion.security;

import com.example.proyectoProgramacion.exception.ResourceNotFoundException;
import com.example.proyectoProgramacion.model.entity.Usuario;
import com.example.proyectoProgramacion.repository.UsuarioRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Servicio personalizado para cargar detalles de usuario para autenticación.
 * Implementa UserDetailsService de Spring Security para integrarse con el sistema de autenticación.
 */
@Service
@Tag(name = "Autenticación", description = "API para la autenticación de usuarios")
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    @Autowired
    public CustomUserDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    /**
     * Carga un usuario por su nombre de usuario.
     *
     * @param username Nombre de usuario a buscar
     * @return UserDetails del usuario encontrado
     * @throws UsernameNotFoundException si el usuario no existe
     */
    @Override
    @Transactional
    @Operation(
        summary = "Cargar usuario por nombre de usuario",
        description = "Carga los detalles de un usuario por su nombre de usuario para la autenticación"
    )
    @Parameter(name = "username", description = "Nombre de usuario a buscar", required = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con username: " + username));

        // Actualizar último acceso
        usuario.setUltimoAcceso(LocalDateTime.now());
        usuarioRepository.save(usuario);

        return UserPrincipal.create(usuario);
    }

    /**
     * Carga un usuario por su ID.
     *
     * @param id ID del usuario a buscar
     * @return UserDetails del usuario encontrado
     */
    @Transactional
    @Operation(
        summary = "Cargar usuario por ID",
        description = "Carga los detalles de un usuario por su ID"
    )
    @Parameter(name = "id", description = "ID del usuario a buscar", required = true)
    public UserDetails loadUserById(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró el usuario con ID: " + id));

        return UserPrincipal.create(usuario);
    }
}



