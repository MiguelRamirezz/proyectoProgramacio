package com.example.proyectoProgramacion.service.impl;

import com.example.proyectoProgramacion.exception.ResourceNotFoundException;
import com.example.proyectoProgramacion.model.dto.carrito.CarritoDTO;
import com.example.proyectoProgramacion.model.dto.carrito.ItemCarritoDTO;
import com.example.proyectoProgramacion.model.dto.orden.OrdenDTO;
import com.example.proyectoProgramacion.model.dto.orden.OrdenDetalleDTO;
import com.example.proyectoProgramacion.model.entity.*;
import com.example.proyectoProgramacion.model.enums.EstadoOrden;
import com.example.proyectoProgramacion.repository.OrdenDetalleRepository;
import com.example.proyectoProgramacion.repository.OrdenRepository;
import com.example.proyectoProgramacion.repository.ProductoRepository;
import com.example.proyectoProgramacion.repository.UsuarioRepository;
import com.example.proyectoProgramacion.service.interfaces.CarritoService;
import com.example.proyectoProgramacion.service.interfaces.OrdenService;
import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.persistence.criteria.Predicate;
import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Validated
public class OrdenServiceImpl implements OrdenService {

    private final OrdenRepository ordenRepository;
    private final OrdenDetalleRepository ordenDetalleRepository;
    private final UsuarioRepository usuarioRepository;
    private final ProductoRepository productoRepository;
    private final CarritoService carritoService;
    private final ModelMapper modelMapper;

    public OrdenServiceImpl(OrdenRepository ordenRepository,
                           OrdenDetalleRepository ordenDetalleRepository,
                           UsuarioRepository usuarioRepository,
                           ProductoRepository productoRepository,
                           CarritoService carritoService,
                           ModelMapper modelMapper) {
        this.ordenRepository = ordenRepository;
        this.ordenDetalleRepository = ordenDetalleRepository;
        this.usuarioRepository = usuarioRepository;
        this.productoRepository = productoRepository;
        this.carritoService = carritoService;
        this.modelMapper = modelMapper;
    }

    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public OrdenDTO crearOrden(@NotBlank String nombreUsuario, @NotBlank String direccionEnvio) {
        // Obtener usuario
        Usuario usuario = usuarioRepository.findByUsername(nombreUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        // Obtener carrito del usuario
        CarritoDTO carritoDTO = carritoService.obtenerCarritoPorUsuario(nombreUsuario);

        if (carritoDTO.getItems().isEmpty()) {
            throw new IllegalStateException("No se puede crear una orden con un carrito vacío");
        }

        // Crear orden
        Orden orden = new Orden();
        orden.setUsuario(usuario);
        orden.setFechaCreacion(LocalDateTime.now());
        orden.setEstado(EstadoOrden.PENDIENTE);
        orden.setDireccionEnvio(direccionEnvio);
        orden.setTotal(BigDecimal.ZERO);

        // Guardar orden para obtener ID
        orden = ordenRepository.save(orden);

        // Crear detalles de la orden
        BigDecimal total = BigDecimal.ZERO;
        List<DetalleOrden> detalles = new ArrayList<>();

        for (ItemCarritoDTO item : carritoDTO.getItems()) {
            Producto producto = productoRepository.findById(item.getProductoId())
                    .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));

            // Verificar stock
            if (producto.getStock() < item.getCantidad()) {
                throw new IllegalStateException("Stock insuficiente para el producto: " + producto.getNombre());
            }

            // Actualizar stock
            producto.setStock(producto.getStock() - item.getCantidad());
            productoRepository.save(producto);

            // Crear detalle
            DetalleOrden detalle = new DetalleOrden();
            detalle.setOrden(orden);
            detalle.setProducto(producto);
            detalle.setCantidad(item.getCantidad());
            // Corregir el método setPrecioUnitario
            detalle.setPrecio(producto.getPrecio());
            detalle.setSubtotal(producto.getPrecio().multiply(BigDecimal.valueOf(item.getCantidad())));

            detalles.add(detalle);
            total = total.add(detalle.getSubtotal());
        }

        // Guardar detalles
        ordenDetalleRepository.saveAll(detalles);

        // Actualizar total de la orden
        orden.setTotal(total);
        orden = ordenRepository.save(orden);

        // Vaciar carrito
        carritoService.vaciarCarrito(nombreUsuario);

        // Convertir a DTO y retornar
        return convertirADTO(orden);
    }

    @Override
    @Cacheable(cacheNames = "ordenes", key = "#ordenId")
    public OrdenDTO obtenerOrdenPorId(Long ordenId) {
        Orden orden = ordenRepository.findById(ordenId)
                .orElseThrow(() -> new ResourceNotFoundException("Orden no encontrada"));

        return convertirADTO(orden);
    }

    @Override
    @Cacheable(cacheNames = "ordenesUsuario", key = "#nombreUsuario + '_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    public Page<OrdenDTO> obtenerOrdenesPorUsuario(String nombreUsuario, Pageable pageable) {
        Usuario usuario = usuarioRepository.findByUsername(nombreUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        Page<Orden> ordenes = ordenRepository.findByUsuario(usuario, pageable);

        return ordenes.map(this::convertirADTO);
    }

    @Override
    @Cacheable(cacheNames = "todasOrdenes", key = "#pageable.pageNumber + '_' + #pageable.pageSize")
    public Page<OrdenDTO> obtenerTodasLasOrdenes(Pageable pageable) {
        Page<Orden> ordenes = ordenRepository.findAll(pageable);

        return ordenes.map(this::convertirADTO);
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = {"ordenes", "ordenesUsuario", "todasOrdenes"}, allEntries = true)
    public OrdenDTO actualizarEstadoOrden(Long ordenId, EstadoOrden estado) {
        Orden orden = ordenRepository.findById(ordenId)
                .orElseThrow(() -> new ResourceNotFoundException("Orden no encontrada"));

        // Verificar si el cambio de estado es válido
        if (orden.getEstado() == EstadoOrden.CANCELADA) {
            throw new IllegalStateException("No se puede cambiar el estado de una orden cancelada");
        }

        if (orden.getEstado() == EstadoOrden.ENTREGADA && estado != EstadoOrden.ENTREGADA) {
            throw new IllegalStateException("No se puede cambiar el estado de una orden entregada");
        }

        orden.setEstado(estado);
        orden = ordenRepository.save(orden);

        return convertirADTO(orden);
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = {"ordenes", "ordenesUsuario", "todasOrdenes"}, allEntries = true)
    public OrdenDTO cancelarOrden(Long ordenId, String nombreUsuario) {
        Orden orden = ordenRepository.findById(ordenId)
                .orElseThrow(() -> new ResourceNotFoundException("Orden no encontrada"));

        Usuario usuario = usuarioRepository.findByUsername(nombreUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        // Verificar si la orden pertenece al usuario
        if (!orden.getUsuario().getId().equals(usuario.getId())) {
            throw new IllegalStateException("La orden no pertenece al usuario");
        }

        // Verificar si la orden ya está cancelada o entregada
        if (orden.getEstado() == EstadoOrden.CANCELADA) {
            throw new IllegalStateException("La orden ya está cancelada");
        }

        if (orden.getEstado() == EstadoOrden.ENTREGADA) {
            throw new IllegalStateException("No se puede cancelar una orden entregada");
        }

        // Cancelar orden
        orden.setEstado(EstadoOrden.CANCELADA);
        orden = ordenRepository.save(orden);

        // Restaurar stock de productos
        List<DetalleOrden> detalles = ordenDetalleRepository.findByOrdenId(ordenId);
        for (DetalleOrden detalle : detalles) {
            Producto producto = detalle.getProducto();
            producto.setStock(producto.getStock() + detalle.getCantidad());
            productoRepository.save(producto);
        }

        return convertirADTO(orden);
    }

    @Override
    @Cacheable(cacheNames = "busquedaOrdenes", key = "#estado + '_' + #fechaInicio + '_' + #fechaFin + '_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    public Page<OrdenDTO> buscarOrdenes(EstadoOrden estado, LocalDateTime fechaInicio,
                                        LocalDateTime fechaFin, Pageable pageable) {
        Specification<Orden> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (estado != null) {
                predicates.add(cb.equal(root.get("estado"), estado));
            }

            if (fechaInicio != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("fechaCreacion"), fechaInicio));
            }

            if (fechaFin != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("fechaCreacion"), fechaFin));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        // Corregir el método findAll con Specification
        Page<Orden> ordenes = ordenRepository.findAll(spec, pageable);

        return ordenes.map(this::convertirADTO);
    }

    @Override
    public boolean verificarPropiedadOrden(Long ordenId, String nombreUsuario) {
        Orden orden = ordenRepository.findById(ordenId)
                .orElseThrow(() -> new ResourceNotFoundException("Orden no encontrada"));

        Usuario usuario = usuarioRepository.findByUsername(nombreUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        return orden.getUsuario().getId().equals(usuario.getId());
    }

    @Override
    @Cacheable(cacheNames = "detallesOrden", key = "#ordenId")
    public List<OrdenDetalleDTO> obtenerDetallesOrden(Long ordenId) {
        List<DetalleOrden> detalles = ordenDetalleRepository.findByOrdenId(ordenId);

        return detalles.stream()
                .map(detalle -> {
                    OrdenDetalleDTO dto = modelMapper.map(detalle, OrdenDetalleDTO.class);
                    dto.setProductoId(detalle.getProducto().getId());
                    dto.setNombreProducto(detalle.getProducto().getNombre());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable(cacheNames = "totalOrdenes")
    public long contarOrdenesTotales() {
        return ordenRepository.count();
    }

    @Override
    @Cacheable(cacheNames = "ordenesRecientes", key = "#limite")
    public List<OrdenDTO> obtenerOrdenesRecientes(int limite) {
        Pageable pageable = PageRequest.of(0, limite, Sort.by(Sort.Direction.DESC, "fechaCreacion"));
        Page<Orden> ordenes = ordenRepository.findAll(pageable);
        return ordenes.getContent().stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    // Método auxiliar para convertir entidad a DTO
    private OrdenDTO convertirADTO(Orden orden) {
        OrdenDTO ordenDTO = modelMapper.map(orden, OrdenDTO.class);
        // Corregir los métodos setUsuarioId y setNombreUsuario
        ordenDTO.setId(orden.getUsuario().getId());
        ordenDTO.setUsuario(orden.getUsuario().getUsername());
        return ordenDTO;
    }
}








