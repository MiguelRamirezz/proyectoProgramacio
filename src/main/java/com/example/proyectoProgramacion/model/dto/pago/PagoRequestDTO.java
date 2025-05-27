package com.example.proyectoProgramacion.model.dto.pago;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

/**
 * DTO para la solicitud de procesamiento de pago.
 */
@Data
public class PagoRequestDTO {
    
    @NotBlank(message = "El número de tarjeta es obligatorio")
    @Pattern(regexp = "^[0-9]{16}$", message = "El número de tarjeta debe tener 16 dígitos")
    private String numeroTarjeta;
    
    @NotBlank(message = "La fecha de expiración es obligatoria (MM/YY)")
    @Pattern(regexp = "^(0[1-9]|1[0-2])\\/?([0-9]{2})$", 
             message = "La fecha de expiración debe tener el formato MM/YY")
    private String fechaExpiracion;
    
    @NotBlank(message = "El CVV es obligatorio")
    @Pattern(regexp = "^[0-9]{3,4}$", message = "El CVV debe tener 3 o 4 dígitos")
    private String cvv;
    
    @NotBlank(message = "El método de pago es obligatorio")
    private String metodoPago;
    
    @NotNull(message = "El monto es obligatorio")
    @DecimalMin(value = "0.01", message = "El monto debe ser mayor a cero")
    private BigDecimal monto;
    
    @NotNull(message = "El ID de la orden es obligatorio")
    private Long ordenId;
    
    /**
     * Obtiene los últimos 4 dígitos del número de tarjeta.
     * @return los últimos 4 dígitos o cadena vacía si no es válido
     */
    public String getUltimosCuatroDigitos() {
        if (numeroTarjeta == null || numeroTarjeta.length() < 4) {
            return "";
        }
        return numeroTarjeta.substring(numeroTarjeta.length() - 4);
    }
    
    /**
     * Valida si la información de la tarjeta es válida.
     * @return true si la información es válida, false en caso contrario
     */
    public boolean esInformacionValida() {
        return numeroTarjeta != null && !numeroTarjeta.trim().isEmpty() &&
               fechaExpiracion != null && !fechaExpiracion.trim().isEmpty() &&
               cvv != null && !cvv.trim().isEmpty() &&
               monto != null && monto.compareTo(BigDecimal.ZERO) > 0;
    }
}
