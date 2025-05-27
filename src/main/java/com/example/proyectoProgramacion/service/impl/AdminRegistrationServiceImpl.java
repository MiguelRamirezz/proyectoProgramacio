package com.example.proyectoProgramacion.service.impl;

import com.example.proyectoProgramacion.config.AdminProperties;
import com.example.proyectoProgramacion.exception.BusinessException;
import com.example.proyectoProgramacion.exception.UnauthorizedException;
import com.example.proyectoProgramacion.exception.ResourceAlreadyExistsException;
import com.example.proyectoProgramacion.model.dto.auth.RegistroUsuarioDTO;
import com.example.proyectoProgramacion.model.entity.Usuario;
import com.example.proyectoProgramacion.repository.UsuarioRepository;
import com.example.proyectoProgramacion.service.interfaces.AdminRegistrationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminRegistrationServiceImpl implements AdminRegistrationService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final AdminProperties adminProperties;

    @Override
    @Transactional
    public void registrarAdmin(RegistroUsuarioDTO registroDTO) {
        // Validar que se proporcione la clave de administrador
        if (registroDTO.getAdminAuthKey() == null || registroDTO.getAdminAuthKey().trim().isEmpty()) {
            throw new UnauthorizedException("Se requiere una clave de autenticación para registrar un administrador");
        }
        
        // Validar la clave de administrador
        if (!validarClaveAdmin(registroDTO.getAdminAuthKey())) {
            throw new UnauthorizedException("Clave de autenticación inválida");
        }

        // Validar los datos del registro
        validarDatosRegistro(registroDTO);

        try {
            // Crear y guardar el administrador
            Usuario admin = crearUsuarioAdmin(registroDTO);
            usuarioRepository.save(admin);
            log.info("Administrador registrado exitosamente: {}", registroDTO.getUsername());

        } catch (Exception e) {
            log.error("Error al registrar administrador: {}", e.getMessage(), e);
            throw new BusinessException("Error al registrar el administrador: " + e.getMessage());
        }
    }
    
    @Override
    public boolean validarClaveAdmin(String clave) {
        if (clave == null || clave.trim().isEmpty()) {
            return false;
        }
        return adminProperties.getRegistrationKey().equals(clave);
    }
    
    /**
     * Valida los datos del registro antes de crear el usuario administrador
     * @param registroDTO Datos del registro a validar
     */
    private void validarDatosRegistro(RegistroUsuarioDTO registroDTO) {
        // Verificar si el nombre de usuario ya existe
        if (usuarioRepository.existsByUsername(registroDTO.getUsername())) {
            throw new ResourceAlreadyExistsException("El nombre de usuario ya está en uso");
        }

        // Verificar si el correo ya está registrado
        if (usuarioRepository.existsByEmail(registroDTO.getEmail())) {
            throw new ResourceAlreadyExistsException("El correo electrónico ya está registrado");
        }
    }
    
    /**
     * Crea un nuevo usuario administrador a partir de los datos del DTO
     * @param registroDTO Datos del registro
     * @return Usuario administrador creado
     */
    private Usuario crearUsuarioAdmin(RegistroUsuarioDTO registroDTO) {
        Usuario admin = new Usuario(
            registroDTO.getUsername(),
            passwordEncoder.encode(registroDTO.getPassword()),
            registroDTO.getEmail(),
            registroDTO.getNombre(),
            registroDTO.getApellido()
        );

        // Configurar teléfono si está presente
        if (registroDTO.getTelefono() != null && !registroDTO.getTelefono().isEmpty()) {
            admin.setTelefono(registroDTO.getTelefono());
        }

        // Asignar roles de administrador
        admin.addRol("ROLE_ADMIN");
        admin.addRol("ROLE_USER");
        admin.setActivo(true);
        
        return admin;
    }
}
