package com.example.proyectoProgramacion.security;

import com.example.proyectoProgramacion.model.entity.Usuario;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Implementación personalizada de UserDetails para representar un usuario autenticado.
 * Esta clase contiene los detalles del usuario que se utilizan para la autenticación y autorización.
 * Extiende UserDetails de Spring Security para integrarse con el framework de seguridad.
 */
@Schema(description = "Detalles del usuario autenticado")
@Hidden
public class UserPrincipal implements UserDetails {

    // Getters
    @Getter
    private final Long id;
    @Getter
    private final String nombre;
    @Getter
    private final String apellido;
    private final String username;
    @Getter
    private final String email;

    @JsonIgnore
    private final String password;

    private final Collection<? extends GrantedAuthority> authorities;
    private final boolean enabled;

    /**
     * Constructor para crear un UserPrincipal.
     */
    public UserPrincipal(Long id, String nombre, String apellido, String username, String email, String password,
                         Collection<? extends GrantedAuthority> authorities, boolean enabled) {
        this.id = id;
        this.nombre = nombre;
        this.apellido = apellido;
        this.username = username;
        this.email = email;
        this.password = password;
        this.authorities = authorities;
        this.enabled = enabled;
    }

    /**
     * Crea un UserPrincipal a partir de un Usuario.
     * Este método estático es el punto de entrada principal para crear una instancia de UserPrincipal
     * a partir de una entidad Usuario de la base de datos.
     *
     * @param usuario Entidad Usuario desde la base de datos
     * @return UserPrincipal creado para uso en la autenticación
     * @throws IllegalArgumentException si el usuario es nulo
     */
    @Schema(description = "Crea un UserPrincipal a partir de una entidad Usuario")
    public static UserPrincipal create(Usuario usuario) {
        Objects.requireNonNull(usuario, "El usuario no puede ser nulo");
        List<GrantedAuthority> authorities = usuario.getRoles().stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        return new UserPrincipal(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getApellido(),
                usuario.getUsername(),
                usuario.getEmail(),
                usuario.getPassword(),
                authorities,
                usuario.getActivo()
        );
    }

    // Implementación de métodos de UserDetails
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    // Métodos equals y hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserPrincipal that = (UserPrincipal) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}


