package com.example.proyectoProgramacion.service.impl;

import com.example.proyectoProgramacion.exception.ResourceNotFoundException;
import com.example.proyectoProgramacion.model.dto.producto.ProductoDTO;
import com.example.proyectoProgramacion.model.entity.Producto;
import com.example.proyectoProgramacion.model.enums.Categoria;
import com.example.proyectoProgramacion.repository.ProductoRepository;
import com.example.proyectoProgramacion.service.interfaces.ProductoService;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Validated
public class ProductoServiceImpl implements ProductoService {

    // Usar constructor injection en lugar de field injection
    private final ProductoRepository productoRepository;
    private final ModelMapper modelMapper;

    @Autowired
    public ProductoServiceImpl(ProductoRepository productoRepository,
                             ModelMapper modelMapper) {
        this.productoRepository = productoRepository;
        this.modelMapper = modelMapper;
        
        // Configuración básica del mapeo
        this.modelMapper.getConfiguration()
            .setMatchingStrategy(MatchingStrategies.STRICT)
            .setSkipNullEnabled(true);
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = {"productos", "productosPorCategoria", "productosDestacados"}, allEntries = true)
    public ProductoDTO crearProducto(@NotNull @Valid ProductoDTO productoDTO) {
        Producto producto = convertirAEntidad(productoDTO);
        producto.setFechaCreacion(LocalDateTime.now());
        producto = productoRepository.save(producto);
        return convertirADTO(producto);
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = {"productos", "producto", "productosPorCategoria", "productosDestacados"}, allEntries = true)
    public ProductoDTO actualizarProducto(@NotNull Long id, @NotNull @Valid ProductoDTO productoDTO) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));

        // Actualizar campos
        producto.setNombre(productoDTO.getNombre());
        producto.setDescripcion(productoDTO.getDescripcion());
        producto.setPrecio(productoDTO.getPrecio());
        producto.setStock(productoDTO.getStock());
        producto.setActivo(productoDTO.isActivo());
        producto.setDestacado(productoDTO.isDestacado());

        // Actualizar categoría si se proporciona
        if (productoDTO.getCategoria() != null) {
            producto.setCategoria(productoDTO.getCategoria()); // Ahora trabajamos directamente con el enum
        }

        producto = productoRepository.save(producto);
        return convertirADTO(producto);
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = {"productos", "producto", "productosPorCategoria", "productosDestacados"}, allEntries = true)
    public void eliminarProducto(@NotNull Long id) {
        if (!productoRepository.existsById(id)) {
            throw new ResourceNotFoundException("Producto no encontrado");
        }
        productoRepository.deleteById(id);
    }

    @Override
    @Cacheable(value = "producto", key = "#id")
    public ProductoDTO obtenerProductoPorId(@NotNull Long id) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));
        return convertirADTO(producto);
    }

    @Override
    @Cacheable(value = "productos")
    public Page<ProductoDTO> obtenerTodosLosProductos(Pageable pageable) {
        Page<Producto> productosPage = productoRepository.findAll(pageable);
        return productosPage.map(this::convertirADTO);
    }

    @Override
    public Page<ProductoDTO> buscarProductos(String termino, Pageable pageable) {
        Page<Producto> productosPage = productoRepository.findByNombreContainingIgnoreCaseOrDescripcionContainingIgnoreCase(
                termino, termino, pageable);
        return productosPage.map(this::convertirADTO);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "productosPorCategoria", key = "#categoria.name() + '_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    public Page<ProductoDTO> obtenerProductosPorCategoria(@NotNull Categoria categoria, Pageable pageable) {
        Page<Producto> productosPage = productoRepository.findByCategoriaAndActivoTrue(categoria, pageable);
        return productosPage.map(this::convertirADTO);
    }
    
    @Override
    @Transactional
    public ProductoDTO actualizarStock(@NotNull Long id, @NotNull Integer cantidad) {
        if (cantidad < 0) {
            throw new IllegalArgumentException("La cantidad no puede ser negativa");
        }
        
        Producto producto = productoRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado con ID: " + id));
            
        producto.setStock(cantidad);
        producto.setFechaActualizacion(LocalDateTime.now());
        
        Producto productoActualizado = productoRepository.save(producto);
        return convertirADTO(productoActualizado);
    }
    
    @Override
    @Transactional
    @CacheEvict(value = {"producto", "productos", "productosDestacados"}, key = "#id")
    public ProductoDTO subirImagen(@NotNull Long id, @NotNull MultipartFile imagen) {
        Objects.requireNonNull(imagen, "El archivo de imagen no puede ser nulo");
        
        // Validar que el archivo no esté vacío
        if (imagen.isEmpty()) {
            throw new IllegalArgumentException("La imagen no puede estar vacía");
        }
        
        // Validar que sea una imagen
        String contentType = imagen.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("El archivo debe ser una imagen");
        }
        
        // Obtener el producto
        Producto producto = productoRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con ID: " + id));
        
        // Crear directorio de imágenes si no existe
        String uploadDir = "src/main/resources/static/uploads";
        Path uploadPath = Paths.get(uploadDir);
        
        try {
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            
            // Generar un nombre de archivo único
            String originalFileName = StringUtils.cleanPath(
                Objects.requireNonNull(imagen.getOriginalFilename()));
            String fileExtension = "";
            int lastDotIndex = originalFileName.lastIndexOf('.');
            if (lastDotIndex > 0) {
                fileExtension = originalFileName.substring(lastDotIndex);
            }
            String fileName = "producto_" + id + "_" + System.currentTimeMillis() + fileExtension;
            
            // Guardar el archivo
            Path filePath = uploadPath.resolve(fileName);
            try (InputStream inputStream = imagen.getInputStream()) {
                Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
                
                // Actualizar la URL de la imagen en el producto
                String imageUrl = "/uploads/" + fileName;
                producto.setImagenUrl(imageUrl);
                producto.setFechaActualizacion(LocalDateTime.now());
                
                // Guardar los cambios
                producto = productoRepository.save(producto);
                return convertirADTO(producto);
            }
            
        } catch (IOException e) {
            log.error("Error al guardar la imagen para el producto con ID: " + id, e);
            throw new RuntimeException("No se pudo guardar la imagen: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Error inesperado al subir la imagen para el producto con ID: " + id, e);
            throw new RuntimeException("Error al procesar la imagen", e);
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "productosDestacados", key = "#limite")
    public List<ProductoDTO> obtenerProductosDestacados(@NotNull Integer limite) {
        Objects.requireNonNull(limite, "El límite no puede ser nulo");
        
        // Validar que el límite sea un número positivo
        if (limite <= 0) {
            throw new IllegalArgumentException("El límite debe ser un número positivo");
        }
        
        // Limitar el máximo de productos a devolver
        final int MAX_PRODUCTOS = 50; // Límite máximo razonable
        int limiteFinal = Math.min(limite, MAX_PRODUCTOS);
        
        try {
            // Obtener productos destacados ordenados por fecha de creación descendente
            List<Producto> productos = productoRepository.findByDestacadoTrueAndActivoTrueOrderByFechaCreacionDesc();
            
            if (productos == null || productos.isEmpty()) {
                return Collections.emptyList();
            }
            
            // Limitar la cantidad de resultados
            if (productos.size() > limiteFinal) {
                productos = productos.subList(0, limiteFinal);
            }
            
            // Convertir a DTOs
            return productos.stream()
                    .filter(Objects::nonNull)
                    .map(this::convertirADTO)
                    .collect(Collectors.toList());
                    
        } catch (Exception e) {
            log.error("Error al obtener productos destacados", e);
            return Collections.emptyList();
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "productosOferta", key = "#pageable.pageNumber + '_' + #pageable.pageSize")
    public Page<ProductoDTO> obtenerProductosEnOferta(Pageable pageable) {
        // Buscar productos con descuento mayor a cero y que estén activos
        Page<Producto> productosPage = productoRepository.findByDescuentoGreaterThanAndActivoTrue(
            BigDecimal.ZERO, 
            pageable
        );
        
        return productosPage.map(this::convertirADTO);
    }
    
    @Override
    @Transactional(readOnly = true)
    public long contarProductosActivos() {
        return productoRepository.countByActivoTrue();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductoDTO> obtenerProductosPorNombreCategoria(String nombreCategoria, Pageable pageable) {
        // Buscar productos cuyo nombre de categoría (ignorando mayúsculas/minúsculas) contenga el término de búsqueda
        return productoRepository.findByCategoriaNombreContainingIgnoreCase(nombreCategoria, pageable)
                .map(this::convertirADTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductoDTO> filtrarPorPrecio(BigDecimal precioMin, BigDecimal precioMax, Pageable pageable) {
        if (precioMin == null && precioMax == null) {
            // Si no se especifican precios, devolver todos los productos
            return obtenerTodosLosProductos(pageable);
        }
        
        if (precioMin == null) {
            // Solo precio máximo
            return productoRepository.findByPrecioLessThanEqual(precioMax, pageable)
                    .map(this::convertirADTO);
        } else if (precioMax == null) {
            // Solo precio mínimo
            return productoRepository.findByPrecioGreaterThanEqual(precioMin, pageable)
                    .map(this::convertirADTO);
        } else {
            // Ambos precios especificados
            if (precioMin.compareTo(precioMax) > 0) {
                // Si el precio mínimo es mayor que el máximo, intercambiar valores
                BigDecimal temp = precioMin;
                precioMin = precioMax;
                precioMax = temp;
            }
            return productoRepository.findByPrecioBetween(precioMin, precioMax, pageable)
                    .map(this::convertirADTO);
        }
    }

    // Métodos privados de utilidad
    private Producto convertirAEntidad(ProductoDTO dto) {
        return modelMapper.map(dto, Producto.class);
    }

    private ProductoDTO convertirADTO(Producto producto) {
        return modelMapper.map(producto, ProductoDTO.class);
    }
}


