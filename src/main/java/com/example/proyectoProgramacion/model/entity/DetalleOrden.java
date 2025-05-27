package com.example.proyectoProgramacion.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entidad que representa un detalle de orden (línea de producto).
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "detalles_orden", indexes = {
    @Index(name = "idx_detalle_orden_orden", columnList = "orden_id"),
    @Index(name = "idx_detalle_orden_producto", columnList = "producto_id")
})
public class DetalleOrden {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "La orden es obligatoria")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "orden_id", nullable = false, foreignKey = @ForeignKey(name = "fk_detalle_orden"))
    private Orden orden;

    @NotNull(message = "El producto es obligatorio")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "producto_id", nullable = false, foreignKey = @ForeignKey(name = "fk_detalle_producto"))
    private Producto producto;

    @NotBlank(message = "El nombre del producto es obligatorio")
    @Size(max = 255, message = "El nombre del producto no puede tener más de 255 caracteres")
    @Column(name = "nombre_producto", nullable = false, length = 255)
    private String nombreProducto;

    @Size(max = 512, message = "La URL de la imagen no puede tener más de 512 caracteres")
    @Column(name = "imagen_producto", length = 512)
    private String imagenProducto;

    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 1, message = "La cantidad debe ser al menos 1")
    @Column(nullable = false)
    private Integer cantidad;

    @NotNull(message = "El precio es obligatorio")
    @DecimalMin(value = "0.01", message = "El precio debe ser mayor que cero")
    @Digits(integer = 10, fraction = 2, message = "El precio debe tener máximo 10 dígitos enteros y 2 decimales")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precio;

    @NotNull(message = "El subtotal es obligatorio")
    @DecimalMin(value = "0.00", message = "El subtotal no puede ser negativo")
    @Digits(integer = 10, fraction = 2, message = "El subtotal debe tener máximo 10 dígitos enteros y 2 decimales")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;

    // Se eliminó el campo SKU ya que no está presente en la entidad Producto
    
    @CreationTimestamp
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;
    
    @UpdateTimestamp
    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    /**
     * Constructor con parámetros principales.
     * 
     * @param producto Producto del detalle
     * @param cantidad Cantidad de productos
     */
    public DetalleOrden(Producto producto, int cantidad) {
        this.producto = producto;
        this.cantidad = cantidad;
        this.nombreProducto = producto.getNombre();
        this.precio = producto.getPrecio();
        this.imagenProducto = producto.getImagenUrl();
        calcularSubtotal();
    }

    public void setProducto(Producto producto) {
        this.producto = producto;
        if (producto != null) {
            if (this.nombreProducto == null) {
                this.nombreProducto = producto.getNombre();
            }
            if (this.precio == null) {
                this.precio = producto.getPrecio();
            }
            if (this.imagenProducto == null) {
                this.imagenProducto = producto.getImagenUrl();
            }
            // Se eliminó la asignación de SKU ya que no está presente en la entidad Producto
            calcularSubtotal();
        }
    }

    public String getImagenProducto() {
        return imagenProducto;
    }

    public void setImagenProducto(String imagenProducto) {
        this.imagenProducto = imagenProducto;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
        calcularSubtotal();
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
        calcularSubtotal();
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    /**
     * Calcula el subtotal basado en el precio y la cantidad.
     */
    @PrePersist
    @PreUpdate
    public void calcularSubtotal() {
        if (this.precio != null && this.cantidad != null) {
            this.subtotal = this.precio.multiply(BigDecimal.valueOf(this.cantidad));
        } else {
            this.subtotal = BigDecimal.ZERO;
        }
    }
    
    @Override
    public String toString() {
        return "DetalleOrden{" +
                "id=" + id +
                ", producto=" + (producto != null ? producto.getId() : null) +
                ", cantidad=" + cantidad +
                ", subtotal=" + subtotal +
                '}';
    }
}



