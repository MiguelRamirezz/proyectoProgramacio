package com.example.proyectoProgramacion.model.dto.pago;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO para la respuesta de un pago procesado.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Respuesta de un pago procesado")
public class PagoResponseDTO {
    
    @Schema(description = "ID único del pago", example = "1")
    private Long id;
    
    @NotBlank(message = "El método de pago es obligatorio")
    @Schema(description = "Método de pago utilizado", example = "VISA")
    private String metodo;
    
    @NotNull(message = "La fecha del pago es obligatoria")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Fecha y hora del pago", example = "2023-12-31 23:59:59")
    private LocalDateTime fecha;
    
    @NotBlank(message = "Los últimos dígitos son obligatorios")
    @Schema(description = "Últimos 4 dígitos de la tarjeta", example = "4242")
    private String ultimosDigitos;
    
    @NotNull(message = "El ID de la orden es obligatorio")
    @Schema(description = "ID de la orden asociada al pago", example = "1001")
    private Long ordenId;
    
    @Schema(description = "Estado del pago", example = "COMPLETADO")
    private String estado;
    
    @Schema(description = "Monto del pago", example = "99.99")
    private BigDecimal monto;
    
    @Schema(description = "Código de referencia del pago", example = "PAY-123456789")
    private String referenciaPago;
    
    @Schema(description = "Mensaje descriptivo del resultado del pago", example = "Pago procesado exitosamente")
    private String mensaje;
    
    /**
     * Crea una respuesta de pago exitoso.
     * @param id ID del pago
     * @param metodo Método de pago
     * @param ordenId ID de la orden
     * @param monto Monto del pago
     * @param ultimosDigitos Últimos 4 dígitos de la tarjeta
     * @return PagoResponseDTO configurado para éxito
     */
    public static PagoResponseDTO crearExitoso(
            Long id, String metodo, Long ordenId, 
            BigDecimal monto, String ultimosDigitos) {
        return PagoResponseDTO.builder()
                .id(id)
                .metodo(metodo)
                .fecha(LocalDateTime.now())
                .ordenId(ordenId)
                .monto(monto)
                .ultimosDigitos(ultimosDigitos)
                .estado("COMPLETADO")
                .mensaje("Pago procesado exitosamente")
                .build();
    }
    
    /**
     * Crea una respuesta de pago fallido.
     * @param mensaje Mensaje de error
     * @return PagoResponseDTO configurado para fallo
     */
    public static PagoResponseDTO crearFallido(String mensaje) {
        return PagoResponseDTO.builder()
                .estado("FALLIDO")
                .mensaje(mensaje)
                .fecha(LocalDateTime.now())
                .build();
    }
}
