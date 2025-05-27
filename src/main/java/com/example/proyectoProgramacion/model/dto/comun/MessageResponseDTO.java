package com.example.proyectoProgramacion.model.dto.comun;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO genérico para respuestas de mensajes simples.
 * Se utiliza para enviar mensajes de éxito, error o información al cliente.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MessageResponseDTO {
    
    /**
     * El mensaje a enviar en la respuesta
     */
    private String message;
    
    // El constructor con todos los argumentos es generado por @AllArgsConstructor
}
