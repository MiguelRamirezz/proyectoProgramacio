package com.example.proyectoProgramacion.controller.api;

import com.example.proyectoProgramacion.model.dto.producto.ProductoDTO;
import com.example.proyectoProgramacion.service.interfaces.ProductoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;



import com.example.proyectoProgramacion.model.enums.Categoria;
import java.math.BigDecimal;
import java.util.List;


@RestController
@RequestMapping("/api/productos")
@Tag(name = "Productos", description = "API para consulta de productos")
@Validated
public class ProductoController {

    private final ProductoService productoService;

    @Autowired
    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    @GetMapping
    @Cacheable(value = "productosCache", key = "#pageable.pageNumber + '_' + #pageable.pageSize + '_' + #pageable.sort + '_' + #nombre + '_' + #categoria + '_' + #precioMin + '_' + #precioMax")
    @Operation(summary = "Listar productos", description = "Obtiene una lista paginada de productos con filtros opcionales")
    public ResponseEntity<Page<ProductoDTO>> listarProductos(
            @PageableDefault(size = 12, sort = "nombre", direction = Sort.Direction.ASC) Pageable pageable,
            @Parameter(description = "Filtrar por nombre") @RequestParam(required = false) String nombre,
            @Parameter(description = "Filtrar por categoría") @RequestParam(required = false) String categoria,
            @Parameter(description = "Precio mínimo") @RequestParam(required = false) BigDecimal precioMin,
            @Parameter(description = "Precio máximo") @RequestParam(required = false) BigDecimal precioMax) {

        // Validar parámetros de búsqueda
        if (precioMin != null && precioMax != null && precioMin.compareTo(precioMax) > 0) {
            throw new IllegalArgumentException("El precio mínimo no puede ser mayor que el precio máximo");
        }

        // Si hay filtros de precio, usamos filtrarPorPrecio
        if (precioMin != null || precioMax != null) {
            return ResponseEntity.ok(productoService.filtrarPorPrecio(
                precioMin != null ? precioMin : BigDecimal.ZERO,
                precioMax != null ? precioMax : new BigDecimal("999999.99"),
                pageable
            ));
        } 
        // Si hay categoría, usamos obtenerProductosPorCategoria
        else if (categoria != null && !categoria.isEmpty()) {
            try {
                Categoria catEnum = Categoria.valueOf(categoria.toUpperCase());
                return ResponseEntity.ok(productoService.obtenerProductosPorCategoria(catEnum, pageable));
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Categoría no válida: " + categoria);
            }
        }
        // Si hay nombre, usamos buscarProductos
        else if (nombre != null && !nombre.trim().isEmpty()) {
            return ResponseEntity.ok(productoService.buscarProductos(nombre, pageable));
        }
        // Si no hay filtros, obtenemos todos los productos
        return ResponseEntity.ok(productoService.obtenerTodosLosProductos(pageable));
    }

    @GetMapping("/{id}")
    @Cacheable(value = "productoCache", key = "#id")
    @Operation(summary = "Obtener producto por ID", description = "Retorna un producto según su ID")
    @ApiResponse(responseCode = "200", description = "Producto encontrado")
    @ApiResponse(responseCode = "404", description = "Producto no encontrado")
    public ResponseEntity<ProductoDTO> obtenerProducto(@PathVariable Long id) {
        return ResponseEntity.ok(productoService.obtenerProductoPorId(id));
    }

    @GetMapping("/destacados")
    @Cacheable(value = "productosDestacadosCache")
    @Operation(summary = "Listar productos destacados", description = "Obtiene una lista de productos destacados")
    public ResponseEntity<List<ProductoDTO>> listarProductosDestacados() {
return ResponseEntity.ok(productoService.obtenerProductosDestacados(10)); // Usamos 10 como límite por defecto
    }

    @GetMapping("/categorias")
    @Cacheable(value = "categoriasCache")
    @Operation(summary = "Listar categorías", description = "Obtiene una lista de todas las categorías disponibles")
    public ResponseEntity<List<Categoria>> listarCategorias() {
        return ResponseEntity.ok(List.of(Categoria.values()));
    }

    @GetMapping("/categoria/{categoria}")
    @Cacheable(value = "productosPorCategoriaCache", key = "#categoria + '_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    @Operation(summary = "Listar productos por categoría", description = "Obtiene una lista paginada de productos por categoría")
    public ResponseEntity<Page<ProductoDTO>> listarProductosPorCategoria(
            @PathVariable String categoria,
            @PageableDefault(size = 12) Pageable pageable) {
        try {
            Categoria catEnum = Categoria.valueOf(categoria.toUpperCase());
            return ResponseEntity.ok(productoService.obtenerProductosPorCategoria(catEnum, pageable));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Categoría no válida: " + categoria);
        }
    }

    @GetMapping("/buscar")
    @Operation(summary = "Buscar productos", description = "Busca productos por término de búsqueda")
    public ResponseEntity<Page<ProductoDTO>> buscarProductos(
            @Parameter(description = "Término de búsqueda") @RequestParam String q,
            @PageableDefault(size = 12) Pageable pageable) {
        return ResponseEntity.ok(productoService.buscarProductos(q, pageable));
    }
}


