package com.example.proyectoProgramacion.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Proveedor para generar y validar tokens JWT.
 * Esta clase maneja toda la lógica relacionada con la creación, validación
 * y extracción de información de tokens JWT utilizados para la autenticación.
 */

@Component
@Tag(name = "Autenticación", description = "API para la generación y validación de tokens JWT")
public class JwtTokenProvider {

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.expiration}")
    private int jwtExpirationInMs;

    // Set para almacenar tokens invalidados
    private final Set<String> blacklistedTokens = new HashSet<>();

    /**
     * Genera un token JWT para un usuario autenticado.
     *
     * @param authentication Objeto de autenticación
     * @return Token JWT generado
     */
    @Operation(
            summary = "Generar token JWT",
            description = "Genera un nuevo token JWT para un usuario autenticado"
    )
    @Parameter(name = "authentication", description = "Objeto de autenticación del usuario", required = true)
    public String generateToken(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationInMs);

        Map<String, Object> claims = new HashMap<>();
        claims.put("username", userPrincipal.getUsername());
        claims.put("roles", userPrincipal.getAuthorities());

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(Long.toString(userPrincipal.getId()))
                .setIssuedAt(new Date())
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    /**
     * Obtiene el ID de usuario desde un token JWT.
     *
     * @param token Token JWT
     * @return ID del usuario
     */
    @Operation(
            summary = "Obtener ID de usuario desde token",
            description = "Extrae el ID de usuario de un token JWT válido"
    )
    @Parameter(name = "token", description = "Token JWT del cual extraer el ID de usuario", required = true)
    public Long getUserIdFromJWT(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();

        return Long.parseLong(claims.getSubject());
    }

    /**
     * Valida un token JWT.
     *
     * @param authToken Token JWT a validar
     * @return true si el token es válido, false en caso contrario
     */
    @Operation(
            summary = "Validar token JWT",
            description = "Verifica si un token JWT es válido y no ha expirado"
    )
    @Parameter(name = "authToken", description = "Token JWT a validar", required = true)
    public boolean validateToken(String authToken) {
        try {
            // Verificar si el token está en la lista negra
            if (blacklistedTokens.contains(authToken)) {
                return false;
            }

            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(authToken);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }

    /**
     * Invalida un token JWT añadiéndolo a una lista negra.
     *
     * @param token Token JWT a invalidar
     */
    @Operation(
            summary = "Invalidar token JWT",
            description = "Invalida un token JWT para que no pueda ser utilizado nuevamente"
    )
    @Parameter(name = "token", description = "Token JWT a invalidar", required = true)
    public void invalidateToken(String token) {
        if (token == null || token.isEmpty()) {
            throw new IllegalArgumentException("El token no puede ser nulo o vacío");
        }

        try {
            // Verificar que el token sea válido antes de invalidarlo
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);

            // Agregar el token a la lista negra
            blacklistedTokens.add(token);
        } catch (JwtException | IllegalArgumentException ex) {
            throw new IllegalArgumentException("Token JWT inválido", ex);
        }
    }

    /**
     * Verifica si un token está en la lista negra.
     *
     * @param token Token JWT a verificar
     * @return true si el token está en la lista negra, false en caso contrario
     */
    public boolean isTokenBlacklisted(String token) {
        return blacklistedTokens.contains(token);
    }

    /**
     * Limpia tokens expirados de la lista negra para gestionar el uso de memoria.
     * Este método debería ser llamado periódicamente, por ejemplo, mediante un programador.
     */
    public void cleanupExpiredBlacklistedTokens() {
        Date now = new Date();
        blacklistedTokens.removeIf(token -> {
            try {
                Claims claims = Jwts.parserBuilder()
                        .setSigningKey(getSigningKey())
                        .build()
                        .parseClaimsJws(token)
                        .getBody();

                return claims.getExpiration().before(now);
            } catch (Exception e) {
                // Si hay un error al parsear, probablemente el token ya expiró o es inválido
                return true;
            }
        });
    }

    /**
     * Obtiene la clave de firma para el token.
     *
     * @return Clave de firma
     */
    private Key getSigningKey() {
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}





