package com.example.proyectoProgramacion.service.impl;

import com.example.proyectoProgramacion.exception.AuthenticationException;
import com.example.proyectoProgramacion.exception.ResourceNotFoundException;
import com.example.proyectoProgramacion.exception.BusinessException;
import com.example.proyectoProgramacion.model.dto.usuario.CambioPasswordDTO;
import com.example.proyectoProgramacion.model.dto.usuario.UsuarioDTO;
import com.example.proyectoProgramacion.model.entity.Usuario;
import com.example.proyectoProgramacion.repository.UsuarioRepository;
import com.example.proyectoProgramacion.service.interfaces.UsuarioService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Validated
@Slf4j
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;

    @Autowired
    public UsuarioServiceImpl(UsuarioRepository usuarioRepository, 
                            PasswordEncoder passwordEncoder, 
                            ModelMapper modelMapper) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.modelMapper = modelMapper;
    }

    @Override
    @Cacheable(cacheNames = "usuario", key = "#username")
    public UsuarioDTO obtenerUsuarioPorUsername(@NotBlank String username) {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        return convertirADTO(usuario);
    }

    @Override
    @Cacheable(cacheNames = "usuarioPorId", key = "#id")
    public UsuarioDTO obtenerUsuarioPorId(@NotNull Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        return convertirADTO(usuario);
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = {"usuario", "usuarioPorId"}, allEntries = true)
    public UsuarioDTO actualizarUsuario(@NotBlank String username, @NotNull @Valid UsuarioDTO usuarioDTO) {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        // Verificar si el email ya está en uso por otro usuario
        if (!usuario.getEmail().equals(usuarioDTO.getEmail()) &&
                usuarioRepository.existsByEmail(usuarioDTO.getEmail())) {
            throw new IllegalArgumentException("El correo electrónico ya está en uso");
        }

        // Actualizar campos
        usuario.setNombre(usuarioDTO.getNombre());
        usuario.setApellido(usuarioDTO.getApellido());
        usuario.setEmail(usuarioDTO.getEmail());
        usuario.setFechaActualizacion(LocalDateTime.now());

        usuario = usuarioRepository.save(usuario);

        return convertirADTO(usuario);
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = {"usuario", "usuarioPorId"}, allEntries = true)
    public void cambiarPassword(@NotBlank String username, @NotNull @Valid CambioPasswordDTO cambioPasswordDTO) {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        // Verificar que la contraseña actual sea correcta
        if (!passwordEncoder.matches(cambioPasswordDTO.getPasswordActual(), usuario.getPassword())) {
            throw new AuthenticationException("La contraseña actual es incorrecta");
        }


        // Verificar que la nueva contraseña y su confirmación coincidan
        if (!cambioPasswordDTO.getPasswordNueva().equals(cambioPasswordDTO.getConfirmacionPassword())) {
            throw new IllegalArgumentException("La nueva contraseña y su confirmación no coinciden");
        }


        // Actualizar contraseña
        usuario.setPassword(passwordEncoder.encode(cambioPasswordDTO.getPasswordNueva()));
        usuario.setFechaActualizacion(LocalDateTime.now());

        usuarioRepository.save(usuario);
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = {"usuario", "usuarioPorId", "todosUsuarios"}, allEntries = true)
    public void desactivarCuenta(@NotBlank String username) {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        usuario.setActivo(false);
        usuario.setFechaActualizacion(LocalDateTime.now());

        usuarioRepository.save(usuario);
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = {"usuario", "usuarioPorId", "todosUsuarios"}, allEntries = true)
    public void activarCuenta(@NotBlank String username) {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        usuario.setActivo(true);
        usuario.setFechaActualizacion(LocalDateTime.now());

        usuarioRepository.save(usuario);
    }

    @Override
    @Cacheable(cacheNames = "todosUsuarios", key = "#pageable.pageNumber + '_' + #pageable.pageSize")
    public Page<UsuarioDTO> obtenerTodosLosUsuarios(Pageable pageable) {
        Page<Usuario> usuarios = usuarioRepository.findAll(pageable);
        return usuarios.map(this::convertirADTO);
    }
    
    @Override
    @Transactional
    @CacheEvict(cacheNames = {"usuario", "usuarioPorId", "todosUsuarios"}, allEntries = true)
    public void eliminarUsuario(@NotNull Long id) {
        if (!usuarioRepository.existsById(id)) {
            throw new ResourceNotFoundException("Usuario no encontrado con ID: " + id);
        }
        usuarioRepository.deleteById(id);
    }
    

    @Override
    @Cacheable(cacheNames = "buscarUsuarios", key = "#termino + '_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    public Page<UsuarioDTO> buscarUsuarios(String termino, Pageable pageable) {
        try {
            // Si el término de búsqueda está vacío, devolver todos los usuarios
            if (termino == null || termino.trim().isEmpty()) {
                return obtenerTodosLosUsuarios(pageable);
            }
            
            // Buscar usuarios que coincidan con el término en nombre, apellido, email o username
            String busqueda = "%" + termino.toLowerCase() + "%";
            Page<Usuario> usuarios = usuarioRepository.findByNombreContainingIgnoreCaseOrApellidoContainingIgnoreCaseOrEmailContainingIgnoreCaseOrUsernameContainingIgnoreCase(
                busqueda, busqueda, busqueda, busqueda, pageable);
                
            return usuarios.map(this::convertirADTO);
        } catch (Exception e) {
            log.error("Error al buscar usuarios con término: " + termino, e);
            throw new BusinessException("Error al buscar usuarios");
        }
    }

    // Método auxiliar para convertir entidad a DTO
    private UsuarioDTO convertirADTO(Usuario usuario) {
        UsuarioDTO dto = modelMapper.map(usuario, UsuarioDTO.class);

        // Mapear roles y convertir a List<String>
        List<String> roleNames = new ArrayList<>(usuario.getRoles());

        dto.setRoles(roleNames);

        return dto;
    }
}
