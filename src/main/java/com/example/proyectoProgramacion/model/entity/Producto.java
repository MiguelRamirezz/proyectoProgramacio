package com.example.proyectoProgramacion.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import com.example.proyectoProgramacion.model.enums.Categoria;

@Entity
@Table(name = "productos", indexes = {
    @Index(name = "idx_producto_nombre", columnList = "nombre"),
    @Index(name = "idx_producto_categoria", columnList = "categoria"),
    @Index(name = "idx_producto_activo", columnList = "activo")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "{producto.nombre.notBlank}") // Corregido notblank a notBlank
    @Size(min = 2, max = 100, message = "{producto.nombre.size}")
    @Column(nullable = false, length = 100)
    private String nombre;

    @Size(max = 1000, message = "{producto.descripcion.size}")
    @Column(length = 1000)
    private String descripcion;

    @NotNull(message = "{producto.precio.notnull}")
    @DecimalMin(value = "0.01", message = "{producto.precio.min}")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precio;

    @Min(value = 0, message = "{producto.stock.min}")
    @Column(nullable = false)
    private int stock;

    @NotNull(message = "{producto.categoria.notnull}")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private Categoria categoria;

    @Size(max = 50)
    @Column(length = 50)
    private String tipoPrenda;

    @Size(max = 100)
    @Column(length = 100)
    private String franquicia;

    @Size(max = 20)
    @Column(length = 20)
    private String talla;

    @Size(max = 50)
    @Column(length = 50)
    private String color;

    @Size(max = 100)
    @Column(length = 100)
    private String material;

    @Size(max = 255)
    @Column(length = 255) // Eliminado name="imagen_url" que no se resuelve
    private String imagenUrl;

    private boolean destacado;

    @Column(nullable = false)
    private boolean activo = true;

    @DecimalMin(value = "0.00", message = "{producto.descuento.min}")
    @Column(precision = 5, scale = 2)
    private BigDecimal descuento = BigDecimal.ZERO;

    @CreationTimestamp
    @Column(updatable = false) // Eliminado name="fecha_creacion" que no se resuelve
    private LocalDateTime fechaCreacion;

    @UpdateTimestamp
    private LocalDateTime fechaActualizacion; // Eliminado name="fecha_actualizacion" que no se resuelve

    @OneToMany(mappedBy = "producto", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemCarrito> itemsCarrito = new ArrayList<>();

    // Debes asegurarte de que la clase ItemOrden exista en tu proyecto
    // Si no existe, debes crearla o eliminar esta relación
    @OneToMany(mappedBy = "producto", cascade = CascadeType.ALL)
    private List<DetalleOrden> itemsOrden = new ArrayList<>(); // Cambiado ItemOrden a DetalleOrden (asumiendo que es el nombre correcto)
}




