package com.example.proyectoProgramacion.service.interfaces;

import com.example.proyectoProgramacion.model.dto.auth.RegistroUsuarioDTO;

/**
 * Servicio para el registro de administradores
 */
public interface AdminRegistrationService {
    
    /**
     * Registra un nuevo administrador en el sistema
     * @param registroDTO Datos del administrador a registrar
     * @throws com.example.proyectoProgramacion.exception.BusinessException Si ocurre un error durante el registro
     * @throws com.example.proyectoProgramacion.exception.ResourceAlreadyExistsException Si el usuario o correo ya existen
     */
    void registrarAdmin(RegistroUsuarioDTO registroDTO);
    
    /**
     * Valida si la clave proporcionada coincide con la clave de administrador configurada
     * @param clave Clave a validar
     * @return true si la clave es válida, false en caso contrario
     */
    boolean validarClaveAdmin(String clave);
}
