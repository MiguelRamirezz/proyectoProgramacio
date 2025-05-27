package com.example.proyectoProgramacion.model.dto.direccion;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para representar una dirección de envío o facturación.
 * Utilizado en el proceso de compra y gestión de direcciones del usuario.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DireccionDTO {
    
    /**
     * Identificador único de la dirección
     */
    private Long id;
    
    /**
     * Nombre completo del destinatario
     */
    @NotBlank(message = "El nombre completo es obligatorio")
    @Size(max = 100, message = "El nombre no puede tener más de 100 caracteres")
    private String nombreCompleto;
    
    /**
     * Primera línea de la dirección (calle y número)
     */
    @NotBlank(message = "La calle es obligatoria")
    @Size(max = 200, message = "La calle no puede tener más de 200 caracteres")
    private String calle;
    
    /**
     * Segunda línea de la dirección (departamento, piso, etc.)
     */
    @Size(max = 100, message = "La referencia no puede tener más de 100 caracteres")
    private String referencia;
    
    /**
     * Ciudad o localidad
     */
    @NotBlank(message = "La ciudad es obligatoria")
    @Size(max = 100, message = "La ciudad no puede tener más de 100 caracteres")
    private String ciudad;
    
    /**
     * Estado o provincia
     */
    @NotBlank(message = "El estado es obligatorio")
    @Size(max = 100, message = "El estado no puede tener más de 100 caracteres")
    private String estado;
    
    /**
     * Código postal
     */
    @NotBlank(message = "El código postal es obligatorio")
    @Pattern(regexp = "^[0-9]{5}$", message = "El código postal debe tener 5 dígitos")
    private String codigoPostal;
    
    /**
     * País
     */
    @NotBlank(message = "El país es obligatorio")
    @Size(max = 100, message = "El país no puede tener más de 100 caracteres")
    private String pais;
    
    /**
     * Teléfono de contacto para envíos
     */
    @NotBlank(message = "El teléfono de contacto es obligatorio")
    @Pattern(regexp = "^\\+?[0-9\\s-]{10,}$", 
             message = "El formato del teléfono no es válido. Use el formato: +52 55 1234 5678 o 5512345678")
    private String telefono;
    
    /**
     * Indica si es la dirección principal del usuario
     */
    private boolean principal;
    
    /**
     * Alias para identificar la dirección (ej: Casa, Trabajo, etc.)
     */
    @Size(max = 50, message = "El alias no puede tener más de 50 caracteres")
    private String alias;
}
