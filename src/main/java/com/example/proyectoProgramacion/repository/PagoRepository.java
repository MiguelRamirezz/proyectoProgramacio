package com.example.proyectoProgramacion.repository;

import com.example.proyectoProgramacion.model.entity.Pago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

@Repository
public interface PagoRepository extends JpaRepository<Pago, Long>, JpaSpecificationExecutor<Pago> {

    // Método para encontrar pagos por orden y usuario
    List<Pago> findByOrdenUsuarioId(Long usuarioId);

    // Implementación del método findAll con Specification
    @Override
    Page<Pago> findAll(Specification<Pago> spec, Pageable pageable);
}



