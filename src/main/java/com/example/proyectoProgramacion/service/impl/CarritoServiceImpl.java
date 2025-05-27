package com.example.proyectoProgramacion.service.impl;

import com.example.proyectoProgramacion.util.AppConstants;
import com.example.proyectoProgramacion.model.dto.carrito.CarritoDTO;
import com.example.proyectoProgramacion.model.dto.carrito.ItemCarritoDTO;
import com.example.proyectoProgramacion.model.entity.Carrito;
import com.example.proyectoProgramacion.model.entity.ItemCarrito;
import com.example.proyectoProgramacion.model.entity.Producto;
import com.example.proyectoProgramacion.model.entity.Usuario;
import com.example.proyectoProgramacion.repository.CarritoRepository;
import com.example.proyectoProgramacion.repository.ItemCarritoRepository;
import com.example.proyectoProgramacion.repository.ProductoRepository;
import com.example.proyectoProgramacion.repository.UsuarioRepository;
import com.example.proyectoProgramacion.service.SessionManager;
import com.example.proyectoProgramacion.service.interfaces.CarritoService;
import com.example.proyectoProgramacion.validation.GruposValidacionCarrito;
import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Validated
public class CarritoServiceImpl implements CarritoService {
    // Importar constantes de la aplicación
    private static final String MSG_USUARIO_NO_ENCONTRADO = AppConstants.USUARIO_NO_ENCONTRADO_FORMAT;
    private static final String MSG_PRODUCTO_NO_ENCONTRADO = AppConstants.PRODUCTO_NO_ENCONTRADO_FORMAT;
    private static final String MSG_STOCK_INSUFICIENTE = "Stock insuficiente para el producto ID: %d. Disponible: %d, Solicitado: %d";
    private static final String MSG_ITEM_NO_ENCONTRADO = "No se encontró el ítem con ID: %d en el carrito";
    private static final String MSG_ERROR_ACCESO_DATOS = "Error al acceder a los datos";

    private final CarritoRepository carritoRepository;
    private final ItemCarritoRepository itemCarritoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ProductoRepository productoRepository;
    private final SessionManager sessionManager;
    private final ModelMapper modelMapper;

    public CarritoServiceImpl(
            CarritoRepository carritoRepository,
            ItemCarritoRepository itemCarritoRepository,
            UsuarioRepository usuarioRepository,
            ProductoRepository productoRepository,
            SessionManager sessionManager,
            ModelMapper modelMapper
    ) {
        this.carritoRepository = carritoRepository;
        this.itemCarritoRepository = itemCarritoRepository;
        this.usuarioRepository = usuarioRepository;
        this.productoRepository = productoRepository;
        this.sessionManager = sessionManager;
        this.modelMapper = modelMapper;
    }

    @Override
    @CacheEvict(
            cacheNames = {"carritoUsuario", "carritoSesion"},
            key = "#nombreUsuario"
    )
    @Retryable(
            value = { Exception.class },
            maxAttempts = 3,
            backoff = @Backoff(delay = 100)
    )
    @Transactional(
            isolation = Isolation.SERIALIZABLE,
            timeout = 10,
            rollbackFor = {Exception.class}
    )
    public CarritoDTO fusionarCarritoSesionConUsuario(@NotBlank String nombreUsuario) {
        // Obtener carrito de sesión
        CarritoDTO carritoSesion = obtenerCarritoSesion();
        if (carritoSesion == null || carritoSesion.getItems().isEmpty()) {
            return obtenerCarritoPorUsuario(nombreUsuario);
        }

        // Obtener carrito del usuario
        Usuario usuario = usuarioRepository.findByUsername(nombreUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Carrito carritoUsuario = carritoRepository.findByUsuarioId(usuario.getId())
                .orElseGet(() -> {
                    Carrito nuevoCarrito = new Carrito();
                    nuevoCarrito.setUsuario(usuario);
                    return carritoRepository.save(nuevoCarrito);
                });

        // Fusionar items del carrito de sesión al carrito del usuario
        for (ItemCarritoDTO itemDTO : carritoSesion.getItems()) {
            Producto producto = productoRepository.findById(itemDTO.getProductoId())
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

            // Verificar si el producto ya existe en el carrito del usuario
            Optional<ItemCarrito> itemExistente = carritoUsuario.getItems().stream()
                    .filter(item -> item.getProducto().getId().equals(itemDTO.getProductoId()))
                    .findFirst();

            if (itemExistente.isPresent()) {
                // Actualizar cantidad
                ItemCarrito item = itemExistente.get();
                item.setCantidad(item.getCantidad() + itemDTO.getCantidad());
                itemCarritoRepository.save(item);
            } else {
                // Agregar nuevo item
                ItemCarrito nuevoItem = new ItemCarrito();
                nuevoItem.setCarrito(carritoUsuario);
                nuevoItem.setProducto(producto);
                nuevoItem.setCantidad(itemDTO.getCantidad());
                carritoUsuario.getItems().add(nuevoItem);
                itemCarritoRepository.save(nuevoItem);
            }
        }

        // Guardar carrito actualizado
        carritoRepository.save(carritoUsuario);

        // Limpiar carrito de sesión
        vaciarCarritoSesion();

        // Devolver carrito fusionado
        return convertirADTO(carritoUsuario);
    }

    @Override
    @Cacheable(cacheNames = "carritoUsuario", key = "#nombreUsuario", unless = "#result == null")
    public CarritoDTO obtenerCarritoPorUsuario(@NotBlank String nombreUsuario) {
        Usuario usuario = usuarioRepository.findByUsername(nombreUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Carrito carrito = carritoRepository.findByUsuarioId(usuario.getId())
                .orElseGet(() -> {
                    Carrito nuevoCarrito = new Carrito();
                    nuevoCarrito.setUsuario(usuario);
                    return carritoRepository.save(nuevoCarrito);
                });

        return convertirADTO(carrito);
    }

    @Override
    @CacheEvict(cacheNames = "carritoUsuario", key = "#nombreUsuario")
    @Validated(GruposValidacionCarrito.AlAgregar.class)
    public CarritoDTO agregarProductoAlCarrito(@NotBlank String nombreUsuario, @NotNull @Valid ItemCarritoDTO itemDTO) {
        Usuario usuario = usuarioRepository.findByUsername(nombreUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Carrito carrito = carritoRepository.findByUsuarioId(usuario.getId())
                .orElseGet(() -> {
                    Carrito nuevoCarrito = new Carrito();
                    nuevoCarrito.setUsuario(usuario);
                    return carritoRepository.save(nuevoCarrito);
                });

        Producto producto = productoRepository.findById(itemDTO.getProductoId())
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        // Verificar stock
        if (producto.getStock() < itemDTO.getCantidad()) {
            throw new RuntimeException("Stock insuficiente");
        }

        // Verificar si el producto ya existe en el carrito
        Optional<ItemCarrito> itemExistente = carrito.getItems().stream()
                .filter(item -> item.getProducto().getId().equals(itemDTO.getProductoId()))
                .findFirst();

        if (itemExistente.isPresent()) {
            // Actualizar cantidad
            ItemCarrito item = itemExistente.get();
            item.setCantidad(item.getCantidad() + itemDTO.getCantidad());
            itemCarritoRepository.save(item);
        } else {
            // Agregar nuevo item
            ItemCarrito nuevoItem = new ItemCarrito();
            nuevoItem.setCarrito(carrito);
            nuevoItem.setProducto(producto);
            nuevoItem.setCantidad(itemDTO.getCantidad());
            carrito.getItems().add(nuevoItem);
            itemCarritoRepository.save(nuevoItem);
        }

        carritoRepository.save(carrito);
        return convertirADTO(carrito);
    }

    @Override
    @CacheEvict(cacheNames = "carritoUsuario", key = "#nombreUsuario")
    @Validated(GruposValidacionCarrito.AlActualizar.class)
    public CarritoDTO actualizarCantidadItem(@NotBlank String nombreUsuario, @NotNull Long itemId, @NotNull Integer cantidad) {
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a cero");
        }

        Usuario usuario = usuarioRepository.findByUsername(nombreUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Carrito carrito = carritoRepository.findByUsuarioId(usuario.getId())
                .orElseThrow(() -> new RuntimeException("Carrito no encontrado"));

        ItemCarrito item = itemCarritoRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item no encontrado"));

        // Verificar que el item pertenece al carrito del usuario
        if (!item.getCarrito().getId().equals(carrito.getId())) {
            throw new RuntimeException("El item no pertenece al carrito del usuario");
        }

        // Verificar stock
        if (item.getProducto().getStock() < cantidad) {
            throw new RuntimeException("Stock insuficiente");
        }

        item.setCantidad(cantidad);
        itemCarritoRepository.save(item);

        return convertirADTO(carrito);
    }

    @Override
    public boolean verificarPropiedadItem(@NotNull Long itemId, @NotBlank String nombreUsuario) {
        Usuario usuario = usuarioRepository.findByUsername(nombreUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Carrito carrito = carritoRepository.findByUsuarioId(usuario.getId())
                .orElseThrow(() -> new RuntimeException("Carrito no encontrado"));

        ItemCarrito item = itemCarritoRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item no encontrado"));

        return item.getCarrito().getId().equals(carrito.getId());
    }

    @Override
    @CacheEvict(cacheNames = "carritoUsuario", key = "#nombreUsuario")
    @Validated(GruposValidacionCarrito.AlEliminar.class)
    public CarritoDTO eliminarItemDelCarrito(@NotBlank String nombreUsuario, @NotNull Long itemId) {
        Usuario usuario = usuarioRepository.findByUsername(nombreUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Carrito carrito = carritoRepository.findByUsuarioId(usuario.getId())
                .orElseThrow(() -> new RuntimeException("Carrito no encontrado"));

        ItemCarrito item = itemCarritoRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item no encontrado"));

        // Verificar que el item pertenece al carrito del usuario
        if (!item.getCarrito().getId().equals(carrito.getId())) {
            throw new RuntimeException("El item no pertenece al carrito del usuario");
        }

        carrito.getItems().remove(item);
        itemCarritoRepository.delete(item);
        carritoRepository.save(carrito);

        return convertirADTO(carrito);
    }

    @Override
    @CacheEvict(cacheNames = "carritoUsuario", key = "#nombreUsuario")
    public CarritoDTO vaciarCarrito(@NotBlank String nombreUsuario) {
        Usuario usuario = usuarioRepository.findByUsername(nombreUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Carrito carrito = carritoRepository.findByUsuarioId(usuario.getId())
                .orElseThrow(() -> new RuntimeException("Carrito no encontrado"));

        itemCarritoRepository.deleteAll(carrito.getItems());
        carrito.getItems().clear();
        carritoRepository.save(carrito);

        return convertirADTO(carrito);
    }

    // Métodos para usuarios no autenticados (sesión)
    @Override
    @Cacheable(
            cacheNames = "carritoSesion",
            key = "T(org.springframework.web.context.request.RequestContextHolder).getRequestAttributes().getSessionId()",
            unless = "#result == null"
    )
    public CarritoDTO obtenerCarritoSesion() {
        return sessionManager.getCarritoSesion();
    }

    @Override
    @CacheEvict(
            cacheNames = "carritoSesion",
            key = "T(org.springframework.web.context.request.RequestContextHolder).getRequestAttributes().getSessionId()"
    )
    @Validated(GruposValidacionCarrito.AlAgregar.class)
    public CarritoDTO agregarProductoAlCarritoSesion(@NotNull @Valid ItemCarritoDTO itemDTO) {
        CarritoDTO carritoSesion = sessionManager.getCarritoSesion();

        // Verificar stock
        Producto producto = productoRepository.findById(itemDTO.getProductoId())
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        if (producto.getStock() < itemDTO.getCantidad()) {
            throw new RuntimeException("Stock insuficiente");
        }

        // Verificar si el producto ya existe en el carrito de sesión
        Optional<ItemCarritoDTO> itemExistente = carritoSesion.getItems().stream()
                .filter(item -> item.getProductoId().equals(itemDTO.getProductoId()))
                .findFirst();

        if (itemExistente.isPresent()) {
            // Actualizar cantidad
            ItemCarritoDTO item = itemExistente.get();
            item.setCantidad(item.getCantidad() + itemDTO.getCantidad());
        } else {
            // Agregar nuevo item
            carritoSesion.getItems().add(itemDTO);
        }

        sessionManager.setCarritoSesion(carritoSesion);
        return carritoSesion;
    }

    @Override
    @CacheEvict(
            cacheNames = "carritoSesion",
            key = "T(org.springframework.web.context.request.RequestContextHolder).getRequestAttributes().getSessionId()"
    )
    @Validated(GruposValidacionCarrito.AlActualizar.class)
    public CarritoDTO actualizarCantidadItemSesion(@NotNull Long itemId, @NotNull Integer cantidad) {
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a cero");
        }

        CarritoDTO carritoSesion = sessionManager.getCarritoSesion();

        // Buscar el item en el carrito de sesión
        Optional<ItemCarritoDTO> itemOptional = carritoSesion.getItems().stream()
                .filter(item -> item.getId().equals(itemId))
                .findFirst();

        if (!itemOptional.isPresent()) {
            throw new RuntimeException("Item no encontrado en el carrito de sesión");
        }

        ItemCarritoDTO item = itemOptional.get();

        // Verificar stock
        Producto producto = productoRepository.findById(item.getProductoId())
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        if (producto.getStock() < cantidad) {
            throw new RuntimeException("Stock insuficiente");
        }

        item.setCantidad(cantidad);
        sessionManager.setCarritoSesion(carritoSesion);

        return carritoSesion;
    }

    @Override
    @CacheEvict(
            cacheNames = "carritoSesion",
            key = "T(org.springframework.web.context.request.RequestContextHolder).getRequestAttributes().getSessionId()"
    )
    @Validated(GruposValidacionCarrito.AlEliminar.class)
    public CarritoDTO eliminarItemDelCarritoSesion(@NotNull Long itemId) {
        CarritoDTO carritoSesion = sessionManager.getCarritoSesion();

        // Eliminar el item del carrito de sesión
        boolean eliminado = carritoSesion.getItems().removeIf(item -> item.getId().equals(itemId));

        if (!eliminado) {
            throw new RuntimeException("Item no encontrado en el carrito de sesión");
        }

        sessionManager.setCarritoSesion(carritoSesion);

        return carritoSesion;
    }

    @Override
    @CacheEvict(
            cacheNames = "carritoSesion",
            key = "T(org.springframework.web.context.request.RequestContextHolder).getRequestAttributes().getSessionId()"
    )
    public CarritoDTO vaciarCarritoSesion() {
        CarritoDTO carritoSesion = new CarritoDTO();
        carritoSesion.setItems(new ArrayList<>());
        sessionManager.setCarritoSesion(carritoSesion);
        return carritoSesion;
    }

    // Método auxiliar para convertir entidad a DTO
    private CarritoDTO convertirADTO(Carrito carrito) {
        CarritoDTO carritoDTO = modelMapper.map(carrito, CarritoDTO.class);

        // Mapear items manualmente si es necesario
        if (carrito.getItems() != null) {
            List<ItemCarritoDTO> itemsDTO = carrito.getItems().stream()
                    .map(item -> {
                        ItemCarritoDTO itemDTO = modelMapper.map(item, ItemCarritoDTO.class);
                        itemDTO.setProductoId(item.getProducto().getId());
                        itemDTO.setNombreProducto(item.getProducto().getNombre());
                        itemDTO.setPrecioUnitario(item.getProducto().getPrecio());
                        return itemDTO;
                    })
                    .collect(Collectors.toList());

            carritoDTO.setItems(itemsDTO);
        } else {
            carritoDTO.setItems(new ArrayList<>());
        }

        return carritoDTO;
    }
}







