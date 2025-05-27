package com.example.proyectoProgramacion.model.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para la respuesta de autenticación JWT.
 * Contiene el token de acceso y el tipo de token.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class JwtAuthResponseDTO {
    
    /**
     * Token JWT generado para la autenticación
     */
    private String token;
    
    /**
     * Tipo de token (siempre "Bearer" para JWT)
     */
    private String tokenType = "Bearer";

    /**
     * Constructor con solo el token (el tipo se establece por defecto a "Bearer")
     * @param token Token JWT generado
     */
    public JwtAuthResponseDTO(String token) {
        this.token = token;
    }
}
