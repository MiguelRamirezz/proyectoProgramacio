package com.example.proyectoProgramacion.model.dto.usuario;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para el cambio de contraseña de un usuario.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO para el cambio de contraseña de un usuario")
public class CambioPasswordDTO {

    @NotBlank(message = "La contraseña actual es obligatoria")
    @Schema(description = "Contraseña actual del usuario", example = "MiContraseñaActual123!", required = true)
    private String passwordActual;

    @NotBlank(message = "La nueva contraseña es obligatoria")
    @Size(min = 8, message = "La nueva contraseña debe tener al menos 8 caracteres")
    @Pattern(
        regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$",
        message = "La contraseña debe contener al menos una mayúscula, una minúscula, un número y un carácter especial"
    )
    @Schema(
        description = "Nueva contraseña del usuario",
        example = "NuevaContraseña123!",
        required = true,
        pattern = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$"
    )
    private String passwordNueva;

    @NotBlank(message = "La confirmación de la contraseña es obligatoria")
    @Schema(description = "Confirmación de la nueva contraseña", example = "NuevaContraseña123!", required = true)
    private String confirmacionPassword;

    /**
     * Verifica si la nueva contraseña y su confirmación coinciden.
     * @return true si las contraseñas coinciden, false en caso contrario
     */
    public boolean validarCoincidenciaContrasenas() {
        return passwordNueva != null && passwordNueva.equals(confirmacionPassword);
    }

    /**
     * Verifica si la nueva contraseña es diferente a la actual.
     * @return true si las contraseñas son diferentes, false si son iguales
     */
    public boolean esContrasenaDiferente() {
        return passwordActual != null && !passwordActual.equals(passwordNueva);
    }
}
