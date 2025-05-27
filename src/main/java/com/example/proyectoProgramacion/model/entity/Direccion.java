package com.example.proyectoProgramacion.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Entidad que representa una dirección de envío o facturación de un usuario.
 */
@Entity
@Table(name = "direcciones")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Direccion {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;
    
    @Column(nullable = false, length = 100)
    private String nombreCompleto;
    
    @Column(nullable = false, length = 200)
    private String calle;
    
    @Column(length = 100)
    private String referencia;
    
    @Column(nullable = false, length = 100)
    private String ciudad;
    
    @Column(nullable = false, length = 100)
    private String estado;
    
    @Column(name = "codigo_postal", nullable = false, length = 10)
    private String codigoPostal;
    
    @Column(nullable = false, length = 100)
    private String pais;
    
    @Column(nullable = false, length = 20)
    private String telefono;
    
    @Column(nullable = false)
    private boolean principal;
    
    @Column(length = 50)
    private String alias;
    
    @CreationTimestamp
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;
    
    @UpdateTimestamp
    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;
}
