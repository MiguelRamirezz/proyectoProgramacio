package com.example.proyectoProgramacion.model.entity;

import com.example.proyectoProgramacion.model.enums.EstadoOrden;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad que representa una orden de compra.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "ordenes", indexes = {
    @Index(name = "idx_orden_usuario", columnList = "usuario_id"),
    @Index(name = "idx_orden_estado", columnList = "estado"),
    @Index(name = "idx_orden_fecha", columnList = "fecha_creacion")
})
public class Orden {

    // Getters y setters
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El número de orden es obligatorio")
    @Size(max = 20, message = "El número de orden no puede tener más de 20 caracteres")
    @Column(nullable = false, unique = true, length = 20)
    private String numero;

    @NotNull(message = "El usuario es obligatorio")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false, foreignKey = @ForeignKey(name = "fk_orden_usuario"))
    private Usuario usuario;

    @CreationTimestamp
    @Column(name = "fecha_creacion", nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime fechaCreacion;
    
    @UpdateTimestamp
    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    @NotNull(message = "El estado de la orden es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20, columnDefinition = "VARCHAR(20) DEFAULT 'PENDIENTE'")
    private EstadoOrden estado = EstadoOrden.PENDIENTE;

    @NotNull(message = "El subtotal es obligatorio")
    @DecimalMin(value = "0.00", message = "El subtotal no puede ser negativo")
    @Digits(integer = 10, fraction = 2, message = "El subtotal debe tener máximo 10 dígitos enteros y 2 decimales")
    @Column(nullable = false, precision = 10, scale = 2, columnDefinition = "DECIMAL(10,2) DEFAULT 0.00")
    private BigDecimal subtotal = BigDecimal.ZERO;

    @DecimalMin(value = "0.00", message = "Los impuestos no pueden ser negativos")
    @Digits(integer = 10, fraction = 2, message = "Los impuestos deben tener máximo 10 dígitos enteros y 2 decimales")
    @Column(precision = 10, scale = 2, columnDefinition = "DECIMAL(10,2) DEFAULT 0.00")
    private BigDecimal impuestos = BigDecimal.ZERO;

    @DecimalMin(value = "0.00", message = "El envío no puede ser negativo")
    @Digits(integer = 10, fraction = 2, message = "El envío debe tener máximo 10 dígitos enteros y 2 decimales")
    @Column(precision = 10, scale = 2, columnDefinition = "DECIMAL(10,2) DEFAULT 0.00")
    private BigDecimal envio = BigDecimal.ZERO;

    @NotNull(message = "El total es obligatorio")
    @DecimalMin(value = "0.00", message = "El total no puede ser negativo")
    @Digits(integer = 10, fraction = 2, message = "El total debe tener máximo 10 dígitos enteros y 2 decimales")
    @Column(nullable = false, precision = 10, scale = 2, columnDefinition = "DECIMAL(10,2) DEFAULT 0.00")
    private BigDecimal total = BigDecimal.ZERO;

    @Size(max = 500, message = "La dirección de envío no puede tener más de 500 caracteres")
    @Column(name = "direccion_envio", length = 500)
    private String direccionEnvio;

    @Size(max = 50, message = "El método de pago no puede tener más de 50 caracteres")
    @Column(name = "metodo_pago", length = 50)
    private String metodoPago;
    
    @Size(max = 1000, message = "Las notas no pueden tener más de 1000 caracteres")
    @Column(length = 1000)
    private String notas;

    @OneToMany(mappedBy = "orden", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetalleOrden> detalles = new ArrayList<>();

    @OneToMany(mappedBy = "orden", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Pago> pagos = new ArrayList<>();
    
    /**
     * Constructor con parámetros básicos.
     * 
     * @param numero Número de la orden
     * @param usuario Usuario que realiza la orden
     */
    public Orden(String numero, Usuario usuario) {
        this.numero = numero;
        this.usuario = usuario;
        this.estado = EstadoOrden.PENDIENTE;
        this.subtotal = BigDecimal.ZERO;
        this.impuestos = BigDecimal.ZERO;
        this.envio = BigDecimal.ZERO;
        this.total = BigDecimal.ZERO;
    }

    /**
     * Agrega un detalle a la orden.
     * 
     * @param detalle Detalle a agregar
     * @return La instancia actual de Orden para método chaining
     */
    public Orden agregarDetalle(DetalleOrden detalle) {
        if (detalle != null) {
            detalles.add(detalle);
            detalle.setOrden(this);
            calcularTotal();
        }
        return this;
    }

    /**
     * Elimina un detalle de la orden.
     * 
     * @param detalle Detalle a eliminar
     * @return true si se eliminó el detalle, false en caso contrario
     */
    public boolean eliminarDetalle(DetalleOrden detalle) {
        boolean removed = detalles.remove(detalle);
        if (removed) {
            detalle.setOrden(null);
            calcularTotal();
        }
        return removed;
    }

    /**
     * Agrega un pago a la orden.
     * 
     * @param pago Pago a agregar
     * @return La instancia actual de Orden para método chaining
     */
    public Orden agregarPago(Pago pago) {
        if (pago != null) {
            pagos.add(pago);
            pago.setOrden(this);
            actualizarEstadoSegunPagos();
        }
        return this;
    }
    
    /**
     * Actualiza el estado de la orden según los pagos realizados.
     */
    private void actualizarEstadoSegunPagos() {
        if (pagos == null || pagos.isEmpty()) {
            this.estado = EstadoOrden.PENDIENTE;
            return;
        }
        
        BigDecimal totalPagado = pagos.stream()
            .filter(p -> p.getMonto() != null)
            .map(Pago::getMonto)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
            
        if (totalPagado.compareTo(BigDecimal.ZERO) == 0) {
            this.estado = EstadoOrden.PENDIENTE;
        } else if (totalPagado.compareTo(total) >= 0) {
            this.estado = EstadoOrden.PAGADA;
        } else {
            this.estado = EstadoOrden.PAGO_PARCIAL;
        }
    }

    /**
     * Calcula el total de la orden basado en los detalles, impuestos y envío.
     * 
     * @return La instancia actual de Orden para método chaining
     */
    public Orden calcularTotal() {
        // Calcular subtotal sumando todos los detalles
        this.subtotal = detalles.stream()
            .filter(d -> d != null && d.getSubtotal() != null)
            .map(DetalleOrden::getSubtotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
            
        // Calcular total sumando subtotal, impuestos y envío
        this.total = this.subtotal
            .add(impuestos != null ? impuestos : BigDecimal.ZERO)
            .add(envio != null ? envio : BigDecimal.ZERO);
            
        // Asegurar que el total no sea negativo
        if (this.total.compareTo(BigDecimal.ZERO) < 0) {
            this.total = BigDecimal.ZERO;
        }
        
        return this;
    }
    
    /**
     * Verifica si la orden puede ser cancelada.
     * 
     * @return true si la orden puede ser cancelada, false en caso contrario
     */
    public boolean puedeSerCancelada() {
        return this.estado == EstadoOrden.PENDIENTE || 
               this.estado == EstadoOrden.PAGO_PARCIAL;
    }
    
    /**
     * Cancela la orden si es posible.
     * 
     * @return true si la orden fue cancelada, false en caso contrario
     */
    public boolean cancelar() {
        if (puedeSerCancelada()) {
            this.estado = EstadoOrden.CANCELADA;
            return true;
        }
        return false;
    }
    
    /**
     * Marca la orden como enviada.
     * 
     * @param numeroSeguimiento Número de seguimiento del envío
     * @return true si la orden pudo ser marcada como enviada
     */
    public boolean marcarComoEnviada(String numeroSeguimiento) {
        if (this.estado == EstadoOrden.PAGADA || this.estado == EstadoOrden.PAGO_PARCIAL) {
            this.estado = EstadoOrden.ENVIADA;
            this.notas = (this.notas != null ? this.notas + "\n" : "") + 
                        "Enviada con seguimiento: " + numeroSeguimiento;
            return true;
        }
        return false;
    }
    
    @Override
    public String toString() {
        return "Orden{" +
                "id=" + id +
                ", numero='" + numero + '\'' +
                ", estado=" + estado +
                ", total=" + total +
                '}';
    }
}


