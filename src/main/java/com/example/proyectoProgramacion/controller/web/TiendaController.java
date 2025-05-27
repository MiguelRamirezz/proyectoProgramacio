package com.example.proyectoProgramacion.controller.web;

import com.example.proyectoProgramacion.model.dto.producto.ProductoDTO;
import com.example.proyectoProgramacion.model.enums.Categoria;
import com.example.proyectoProgramacion.exception.ResourceNotFoundException;
import com.example.proyectoProgramacion.service.interfaces.ProductoService;
import com.example.proyectoProgramacion.service.interfaces.CategoriaService;
import com.example.proyectoProgramacion.model.dto.producto.CategoriaDTO;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import com.example.proyectoProgramacion.util.AppConstants;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/tienda")
public class TiendaController {

    private final ProductoService productoService;
    private final CategoriaService categoriaService;

    public TiendaController(ProductoService productoService, CategoriaService categoriaService) {
        this.productoService = productoService;
        this.categoriaService = categoriaService;
    }

    @GetMapping
    public String tienda(
            @PageableDefault(size = 12, sort = "nombre", direction = Sort.Direction.ASC) Pageable pageable,
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String categoria,
            @RequestParam(required = false) BigDecimal precioMin,
            @RequestParam(required = false) BigDecimal precioMax,
            @RequestParam(required = false) String ordenar,
            Model model) {

        try {
            // Validar y ajustar el tamaño de página
            int pageSize = Math.min(pageable.getPageSize(), 24); // Tamaño máximo de 24 productos por página
            
            // Configurar ordenación
            Sort sort = Sort.by(Sort.Direction.ASC, "nombre"); // Orden por defecto
            if (ordenar != null && !ordenar.isEmpty()) {
                switch (ordenar.toLowerCase()) {
                    case "precio-asc":
                        sort = Sort.by(Sort.Direction.ASC, "precio");
                        break;
                    case "precio-desc":
                        sort = Sort.by(Sort.Direction.DESC, "precio");
                        break;
                    case "nombre-asc":
                        sort = Sort.by(Sort.Direction.ASC, "nombre");
                        break;
                    case "nombre-desc":
                        sort = Sort.by(Sort.Direction.DESC, "nombre");
                        break;
                    default:
                        sort = Sort.by(Sort.Direction.ASC, "nombre");
                }
            }
            
            Pageable pageableOrdenado = PageRequest.of(
                pageable.getPageNumber(), 
                pageSize,
                sort
            );

            Page<ProductoDTO> productos;
            
            // Aplicar filtros según los parámetros
            if (nombre != null && !nombre.isEmpty()) {
                productos = productoService.buscarProductos(nombre, pageableOrdenado);
            } else if (categoria != null && !categoria.isEmpty()) {
                try {
                    Categoria catEnum = Categoria.valueOf(categoria.toUpperCase());
                    productos = productoService.obtenerProductosPorCategoria(catEnum, pageableOrdenado);
                } catch (IllegalArgumentException e) {
                    log.warn("Categoría no válida: {}", categoria, e);
                    model.addAttribute("error", "Categoría no válida: " + categoria);
                    categoria = null; // Resetear el filtro de categoría
                    productos = productoService.obtenerTodosLosProductos(pageableOrdenado);
                }
            } else if (precioMin != null || precioMax != null) {
                // Validar parámetros de búsqueda
                if (precioMin != null && precioMax != null && precioMin.compareTo(precioMax) > 0) {
                    model.addAttribute("error", "El precio mínimo no puede ser mayor que el precio máximo");
                    precioMin = null;
                    precioMax = null;
                }
                productos = productoService.filtrarPorPrecio(precioMin, precioMax, pageableOrdenado);
            } else {
                // Sin filtros, obtener todos los productos
                productos = productoService.obtenerTodosLosProductos(pageableOrdenado);
            }

            // Obtener todas las categorías para el filtro
            List<CategoriaDTO> categorias = categoriaService.obtenerTodasLasCategorias();
            List<String> nombresCategorias = categorias.stream()
                .map(dto -> dto.getCategoria().name().toLowerCase())
                .collect(java.util.stream.Collectors.toList());

            model.addAttribute("productos", productos);
            model.addAttribute("categorias", nombresCategorias);
            model.addAttribute("nombre", nombre);
            model.addAttribute("categoriaSeleccionada", categoria);
            model.addAttribute("precioMin", precioMin);
            model.addAttribute("precioMax", precioMax);
            model.addAttribute("ordenar", ordenar);

            return "tienda/productos";
        } catch (Exception e) {
            log.error("Error al cargar los productos: {}", e.getMessage(), e);
            model.addAttribute("error", "Error al cargar los productos. Por favor, inténtalo de nuevo más tarde.");
            return "tienda/productos";
        }
    }

    @GetMapping("/categoria/{categoria}")
    public String productosPorCategoria(
            @PathVariable String categoria,
            @PageableDefault(size = 12) Pageable pageable,
            Model model) {

        try {
            // Validar que la categoría no esté vacía
            if (categoria == null || categoria.trim().isEmpty()) {
                throw new IllegalArgumentException("La categoría no puede estar vacía");
            }
            
            // Convertir el string a enum Categoria
            Categoria catEnum = Categoria.valueOf(categoria.toUpperCase());
            
            // Llamar al servicio con el enum Categoria
            Page<ProductoDTO> productos = productoService.obtenerProductosPorCategoria(catEnum, pageable);
            
            // Obtener todas las categorías para el menú de navegación
            List<CategoriaDTO> categoriasDTO = categoriaService.obtenerTodasLasCategorias();
            List<String> nombresCategorias = categoriasDTO.stream()
                .map(dto -> dto.getCategoria().name().toLowerCase())
                .collect(java.util.stream.Collectors.toList());

            model.addAttribute("productos", productos);
            model.addAttribute("categorias", nombresCategorias);
            model.addAttribute("categoriaSeleccionada", categoria.toLowerCase());
            model.addAttribute("tituloPagina", "Productos de " + categoria);

            return "tienda/productos";
            
        } catch (IllegalArgumentException e) {
            log.warn("Categoría no válida: {}", categoria, e);
            model.addAttribute("error", "Categoría no encontrada: " + categoria);
            return "redirect:/tienda";
        } catch (Exception e) {
            log.error("Error al cargar productos por categoría: {}", e.getMessage(), e);
            model.addAttribute("error", "Ocurrió un error al cargar los productos. Por favor, inténtalo de nuevo más tarde.");
            return "redirect:/tienda";
        }
    }

    @GetMapping("/buscar")
    public String buscarProductos(
            @RequestParam String q,
            @PageableDefault(size = 12) Pageable pageable,
            Model model) {

        try {
            Page<ProductoDTO> productos = productoService.buscarProductos(q, pageable);
            
            List<Categoria> categorias = List.of(Categoria.values());
            List<String> nombresCategorias = categorias.stream()
                .map(Enum::name)
                .map(String::toLowerCase)
                .collect(java.util.stream.Collectors.toList());

            model.addAttribute("productos", productos);
            model.addAttribute("categorias", nombresCategorias);
            model.addAttribute("terminoBusqueda", q);

            return "tienda/productos";
        } catch (Exception e) {
            model.addAttribute("error", "Error al buscar productos: " + e.getMessage());
            return "tienda/productos";
        }
    }

    @GetMapping("/producto/{id}")
    @Cacheable(value = "productoDetalleCache", key = "#id")
    public String detalleProducto(@PathVariable Long id, Model model) {
        try {
            // Obtener el producto
            ProductoDTO producto = productoService.obtenerProductoPorId(id);
            if (producto == null) {
                throw new ResourceNotFoundException("Producto no encontrado con ID: " + id);
            }

            // Obtener productos relacionados (misma categoría)
            // Crear un Pageable para los productos relacionados
            Pageable pageableRelacionados = PageRequest.of(
                Integer.parseInt(AppConstants.NUMERO_PAGINA_POR_DEFECTO), 
                AppConstants.TAMANO_PAGINA_RELACIONADOS
            );
            // Obtener productos de la misma categoría
            Page<ProductoDTO> productosRelacionadosPage = productoService
                .obtenerProductosPorCategoria(producto.getCategoria(), pageableRelacionados);
            
            // Filtrar para excluir el producto actual
            List<ProductoDTO> productosRelacionados = productosRelacionadosPage.getContent().stream()
                .filter(p -> !p.getId().equals(id))
                .limit(4)
                .collect(java.util.stream.Collectors.toList());

            model.addAttribute("producto", producto);
            model.addAttribute("productosRelacionados", productosRelacionados);

            return "tienda/detalle-producto";
        } catch (ResourceNotFoundException e) {
            model.addAttribute("error", e.getMessage());
            return "redirect:/tienda";
        } catch (Exception e) {
            model.addAttribute("error", "Error al cargar el producto: " + e.getMessage());
            return "redirect:/tienda";
        }
    }
    
    /**
     * Método auxiliar para obtener el objeto Sort según el parámetro de ordenación
     */
    private Sort getSorting(String ordenar) {
        if (ordenar == null) {
            return Sort.by("nombre").ascending();
        }
        
        return switch (ordenar) {
            case "precioAsc" -> Sort.by("precio").ascending();
            case "precioDesc" -> Sort.by("precio").descending();
            case "nombreAsc" -> Sort.by("nombre").ascending();
            case "nombreDesc" -> Sort.by("nombre").descending();
            case "masRecientes" -> Sort.by("fechaCreacion").descending();
            default -> Sort.by("nombre").ascending();
        };
    }
}


