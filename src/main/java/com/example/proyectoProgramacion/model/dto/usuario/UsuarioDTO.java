package com.example.proyectoProgramacion.model.dto.usuario;

import com.example.proyectoProgramacion.model.dto.direccion.DireccionDTO;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO que representa un usuario en el sistema.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "DTO que representa un usuario en el sistema")
public class UsuarioDTO {

    @Schema(description = "ID único del usuario", example = "1")
    private Long id;

    @NotBlank(message = "El nombre de usuario es obligatorio")
    @Size(min = 3, max = 50, message = "El nombre de usuario debe tener entre 3 y 50 caracteres")
    @Pattern(regexp = "^[a-zA-Z0-9._-]+$", message = "El nombre de usuario solo puede contener letras, números, puntos, guiones bajos y guiones")
    @Schema(description = "Nombre de usuario único", example = "johndoe", required = true)
    private String username;

    @NotBlank(message = "El correo electrónico es obligatorio")
    @Email(message = "El formato del correo electrónico no es válido")
    @Schema(description = "Correo electrónico del usuario", example = "usuario@ejemplo.com", required = true)
    private String email;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 2, max = 50, message = "El nombre debe tener entre 2 y 50 caracteres")
    @Schema(description = "Nombre del usuario", example = "Juan", required = true)
    private String nombre;

    @NotBlank(message = "El apellido es obligatorio")
    @Size(min = 2, max = 50, message = "El apellido debe tener entre 2 y 50 caracteres")
    @Schema(description = "Apellido del usuario", example = "Pérez", required = true)
    private String apellido;

    @Pattern(regexp = "^\\+?[0-9]{9,15}$", message = "El formato del teléfono no es válido")
    @Schema(description = "Número de teléfono del usuario", example = "+1234567890")
    private String telefono;

    @Schema(description = "Lista de roles del usuario", example = "[\"ROLE_USER\"]")
    private List<String> roles;

    @NotNull(message = "El estado activo no puede ser nulo")
    @Schema(description = "Indica si el usuario está activo", example = "true")
    private Boolean activo;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Fecha de creación del usuario", example = "2023-01-01 12:00:00")
    private LocalDateTime fechaCreacion;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Fecha del último acceso del usuario", example = "2023-01-01 12:00:00")
    private LocalDateTime ultimoAcceso;

    @Schema(description = "Lista de direcciones del usuario")
    private List<DireccionDTO> direcciones;

    /**
     * Obtiene el nombre completo del usuario.
     * @return el nombre completo en formato "nombre apellido"
     */
    public String getNombreCompleto() {
        return String.format("%s %s", 
            this.nombre != null ? this.nombre : "", 
            this.apellido != null ? this.apellido : "")
            .trim();
    }

    /**
     * Verifica si el usuario tiene un rol específico.
     * @param rol el rol a verificar
     * @return true si el usuario tiene el rol, false en caso contrario
     */
    public boolean tieneRol(String rol) {
        return this.roles != null && this.roles.contains(rol);
    }

    /**
     * Verifica si el usuario es administrador.
     * @return true si el usuario tiene el rol de administrador, false en caso contrario
     */
    public boolean esAdmin() {
        return tieneRol("ROLE_ADMIN");
    }
}
