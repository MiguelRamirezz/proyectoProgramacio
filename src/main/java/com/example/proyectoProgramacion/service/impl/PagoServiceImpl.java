package com.example.proyectoProgramacion.service.impl;

import com.example.proyectoProgramacion.model.dto.pago.PagoDTO;
import com.example.proyectoProgramacion.model.dto.pago.PagoRequestDTO;
import com.example.proyectoProgramacion.model.dto.pago.PagoResponseDTO;
import com.example.proyectoProgramacion.model.enums.EstadoPago;
import com.example.proyectoProgramacion.model.entity.Pago;
import com.example.proyectoProgramacion.repository.PagoRepository;
import com.example.proyectoProgramacion.service.interfaces.PagoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PagoServiceImpl implements PagoService {

    private final PagoRepository pagoRepository;

    @Autowired
    public PagoServiceImpl(PagoRepository pagoRepository) {
        this.pagoRepository = pagoRepository;
    }

    @Override
    @Transactional
    public PagoResponseDTO procesarPago(PagoRequestDTO pagoRequestDTO, String nombre) {
        // Validar datos de entrada
        if (pagoRequestDTO == null || pagoRequestDTO.getNumeroTarjeta() == null ||
                pagoRequestDTO.getFechaExpiracion() == null || pagoRequestDTO.getCvv() == null) {
            throw new IllegalArgumentException("Datos de pago incompletos");
        }

        // Crear nuevo pago
        Pago pago = new Pago();
        pago.setMetodo("Tarjeta");
        pago.setFecha(LocalDateTime.now());
        // No usar setEstado si no existe en la clase Pago

        // Obtener últimos 4 dígitos de la tarjeta
        String numeroTarjeta = pagoRequestDTO.getNumeroTarjeta();
        String ultimosDigitos = numeroTarjeta.substring(Math.max(0, numeroTarjeta.length() - 4));
        pago.setUltimosDigitos(ultimosDigitos);

        // Guardar pago
        Pago pagoGuardado = pagoRepository.save(pago);

        // Convertir a DTO de respuesta
        PagoResponseDTO responseDTO = new PagoResponseDTO();
        responseDTO.setId(pagoGuardado.getId());
        responseDTO.setMetodo(pagoGuardado.getMetodo());
        responseDTO.setFecha(pagoGuardado.getFecha());
        responseDTO.setUltimosDigitos(pagoGuardado.getUltimosDigitos());
        // No usar setEstado si no existe en PagoResponseDTO

        return responseDTO;
    }

    @Override
    @Transactional
    public PagoDTO actualizarEstadoPago(Long pagoId, EstadoPago nuevoEstado) {
        Optional<Pago> pagoOpt = pagoRepository.findById(pagoId);
        if (pagoOpt.isPresent()) {
            Pago pago = pagoOpt.get();
            // Si no existe setEstado, debes actualizar el estado de otra manera
            // o agregar este método a tu clase Pago
            pagoRepository.save(pago);
        }
        return null;
    }

    @Transactional(readOnly = true)
    @Override
    public List<PagoDTO> obtenerPagosPorUsuario(Long usuarioId) {
        List<Pago> pagos = pagoRepository.findByOrdenUsuarioId(usuarioId);
        return pagos.stream()
                .map(this::convertirAPagoDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PagoDTO> obtenerPagosPorUsuario(String usuarioId, Pageable pageable) {
        // Convertir String a Long
        Long userId = Long.parseLong(usuarioId);
        // Implementa la lógica para buscar pagos por usuario con paginación
        Specification<Pago> spec = (root, query, cb) ->
                cb.equal(root.get("usuario").get("id"), userId);

        Page<Pago> pagos = pagoRepository.findAll(spec, pageable);
        return pagos.map(this::convertirAPagoDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PagoDTO> obtenerTodosLosPagos(Pageable pageable) {
        Page<Pago> pagos = pagoRepository.findAll(pageable);
        return pagos.map(this::convertirAPagoDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PagoDTO> buscarPagos(EstadoPago estado, LocalDateTime fechaInicio,
                                     LocalDateTime fechaFin, Pageable pageable) {
        // Crear especificación para filtrar
        Specification<Pago> spec = Specification.where(null);

        if (estado != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("estado"), estado));
        }

        if (fechaInicio != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("fecha"), fechaInicio));
        }

        if (fechaFin != null) {
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("fecha"), fechaFin));
        }

        Page<Pago> pagos = pagoRepository.findAll(spec, pageable);
        return pagos.map(this::convertirAPagoDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PagoDTO> obtenerPagoPorId(Long id) {
        return pagoRepository.findById(id)
                .map(this::convertirAPagoDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean verificarPropiedadPago(Long pagoId, String usuarioId) {
        Optional<Pago> pagoOpt = pagoRepository.findById(pagoId);
        if (pagoOpt.isPresent()) {
            Pago pago = pagoOpt.get();
            return pago.getUsuario().getId().toString().equals(usuarioId);
        }
        return false;
    }

    @Override
    @Transactional
    public PagoDTO reembolsarPago(Long pagoId, String usuarioId) {
        // Verificar propiedad
        if (!verificarPropiedadPago(pagoId, usuarioId)) {
            throw new IllegalArgumentException("No tienes permiso para reembolsar este pago");
        }

        Optional<Pago> pagoOpt = pagoRepository.findById(pagoId);
        if (pagoOpt.isPresent()) {
            Pago pago = pagoOpt.get();
            // No usar setEstado si no existe en la clase Pago
            pagoRepository.save(pago);
        } else {
            throw new IllegalArgumentException("Pago no encontrado");
        }
        return null;
    }

    // Método auxiliar para convertir Pago a PagoDTO
    private PagoDTO convertirAPagoDTO(Pago pago) {
        PagoDTO dto = new PagoDTO();
        dto.setId(pago.getId());
        dto.setMetodo(pago.getMetodo());
        dto.setFecha(pago.getFecha());
        dto.setUltimosDigitos(pago.getUltimosDigitos());
        // No usar setEstado si no existe en PagoDTO

        if (pago.getOrden() != null) {
            dto.setOrdenId(pago.getOrden().getId());
        }

        if (pago.getUsuario() != null) {
            dto.setUsuarioId(pago.getUsuario().getId());
            dto.setNombreUsuario(pago.getUsuario().getNombre());
        }

        return dto;
    }
}








