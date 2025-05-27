package com.example.proyectoProgramacion.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entidad que representa un pago realizado por un usuario.
 */
@Setter
@Getter
@Entity
@Table(name = "pagos", indexes = {
    @Index(name = "idx_pago_orden", columnList = "orden_id"),
    @Index(name = "idx_pago_usuario", columnList = "usuario_id"),
    @Index(name = "idx_pago_fecha", columnList = "fecha")
})
public class Pago {
    // Getters y Setters
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El método de pago es obligatorio")
    @Size(max = 50, message = "El método de pago no puede tener más de 50 caracteres")
    @Column(nullable = false, length = 50)
    private String metodo;

    @NotNull(message = "La fecha del pago es obligatoria")
    @Column(nullable = false)
    private LocalDateTime fecha;

    @Size(max = 4, message = "Los últimos dígitos no pueden tener más de 4 caracteres")
    @Column(name = "ultimos_digitos", length = 4)
    private String ultimosDigitos;

    @PositiveOrZero(message = "El monto debe ser mayor o igual a cero")
    @Column(precision = 10, scale = 2)
    private BigDecimal monto;

    @Size(max = 50, message = "El estado no puede tener más de 50 caracteres")
    @Column(length = 50)
    private String estado;

    @Size(max = 255, message = "La referencia no puede tener más de 255 caracteres")
    private String referencia;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "orden_id", nullable = false)
    private Orden orden;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @CreationTimestamp
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @UpdateTimestamp
    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    /**
     * Constructor por defecto.
     */
    public Pago() {
        this.fecha = LocalDateTime.now();
    }

    /**
     * Constructor con parámetros básicos.
     * 
     * @param metodo Método de pago
     * @param monto Monto del pago
     * @param orden Orden asociada
     * @param usuario Usuario que realiza el pago
     */
    public Pago(String metodo, BigDecimal monto, Orden orden, Usuario usuario) {
        this.metodo = metodo;
        this.monto = monto;
        this.orden = orden;
        this.usuario = usuario;
        this.fecha = LocalDateTime.now();
        this.estado = "PENDIENTE";
    }
}




