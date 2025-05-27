package com.example.proyectoProgramacion.service.interfaces;

import com.example.proyectoProgramacion.model.dto.producto.ProductoDTO;
import com.example.proyectoProgramacion.model.enums.Categoria;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.List;


@Validated
public interface ProductoService {

    /**
     * Crea un nuevo producto
     * @param productoDTO datos del producto
     * @return DTO con la información del producto creado
     */
    ProductoDTO crearProducto(@NotNull @Valid ProductoDTO productoDTO);

    /**
     * Actualiza un producto existente
     * @param id ID del producto
     * @param productoDTO datos actualizados del producto
     * @return DTO con la información del producto actualizado
     */
    ProductoDTO actualizarProducto(@NotNull Long id, @NotNull @Valid ProductoDTO productoDTO);

    /**
     * Elimina un producto
     * @param id ID del producto a eliminar
     */
    void eliminarProducto(@NotNull Long id);

    /**
     * Obtiene un producto por su ID
     * @param id ID del producto
     * @return DTO con la información del producto
     */
    ProductoDTO obtenerProductoPorId(@NotNull Long id);

    /**
     * Obtiene todos los productos con paginación
     * @param pageable información de paginación
     * @return Página con todos los productos
     * @cacheable Los resultados se almacenan en caché con la clave 'productos'
     */
    @Cacheable(value = "productos", key = "#pageable.pageNumber + '_' + #pageable.pageSize")
    Page<ProductoDTO> obtenerTodosLosProductos(Pageable pageable);

    /**
     * Obtiene productos por categoría
     * @param categoria categoría de los productos a buscar
     * @param pageable información de paginación
     * @return Página con los productos de la categoría
     * @cacheable Los resultados se almacenan en caché con la clave 'productosPorCategoria_<categoria>_<página>_<tamaño>'
     */
    @Cacheable(value = "productosPorCategoria", key = "#categoria.name() + '_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    Page<ProductoDTO> obtenerProductosPorCategoria(@NotNull Categoria categoria, Pageable pageable);

    /**
     * Obtiene productos por ID de categoría (obsoleto, usar obtenerProductosPorCategoria en su lugar)
     * @param categoriaId ID de la categoría (como String)
     * @param pageable información de paginación
     * @return Página con los productos de la categoría
     * @deprecated Usar {@link #obtenerProductosPorCategoria(Categoria, Pageable)} en su lugar
     */
    @Deprecated(since = "1.0", forRemoval = true)
    default Page<ProductoDTO> obtenerProductosPorCategoriaId(@NotNull String categoriaId, Pageable pageable) {
        try {
            Categoria categoria = Categoria.valueOf(categoriaId.toUpperCase());
            return obtenerProductosPorCategoria(categoria, pageable);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Categoría no válida: " + categoriaId, e);
        }
    }

    /**
     * Busca productos por nombre o descripción
     * @param termino término de búsqueda (no puede estar vacío)
     * @param pageable información de paginación
     * @return Página con los productos que coinciden con el término de búsqueda
     */
    Page<ProductoDTO> buscarProductos(@NotBlank String termino, Pageable pageable);

    /**
     * Filtra productos por rango de precios
     * @param precioMin precio mínimo (opcional)
     * @param precioMax precio máximo (opcional)
     * @param pageable información de paginación
     * @return Página con los productos que cumplen con el rango de precios
     */
    Page<ProductoDTO> filtrarPorPrecio(BigDecimal precioMin, BigDecimal precioMax, Pageable pageable);

    /**
     * Obtiene productos por nombre de categoría
     * @param nombreCategoria nombre de la categoría a buscar
     * @param pageable información de paginación
     * @return Página con los productos de la categoría especificada
     */
    Page<ProductoDTO> obtenerProductosPorNombreCategoria(String nombreCategoria, Pageable pageable);

    /**
     * Actualiza el stock de un producto
     * @param id ID del producto (debe ser positivo)
     * @param cantidad nueva cantidad de stock (debe ser 0 o mayor)
     * @return DTO con la información del producto actualizado
     * @throws IllegalArgumentException si el ID no existe o la cantidad es inválida
     */
    ProductoDTO actualizarStock(@NotNull @Positive Long id, @NotNull @Positive Integer cantidad)
            throws IllegalArgumentException;

    /**
     * Sube una imagen para un producto
     * @param id ID del producto (debe ser positivo)
     * @param imagen archivo de imagen (no nulo)
     * @return DTO con la información del producto actualizado
     * @throws IllegalArgumentException si el ID no existe o el archivo no es una imagen válida
     */
    ProductoDTO subirImagen(@NotNull @Positive Long id, @NotNull MultipartFile imagen)
            throws IllegalArgumentException;

    /**
     * Obtiene productos destacados
     * @param limite número máximo de productos a retornar (debe ser mayor a 0)
     * @return Lista de productos destacados
     * @throws IllegalArgumentException si el límite es menor o igual a 0
     * @cacheable Los resultados se almacenan en caché con la clave 'productosDestacados_<limite>'
     */
    @Cacheable(value = "productosDestacados", key = "#limite")
    List<ProductoDTO> obtenerProductosDestacados(@NotNull @Positive Integer limite)
            throws IllegalArgumentException;

    /**
     * Obtiene productos en oferta con paginación
     * @param pageable información de paginación
     * @return Página con los productos en oferta
     * @cacheable Los resultados se almacenan en caché con la clave 'productosOferta_<página>_<tamaño>'
     */
    @Cacheable(value = "productosOferta", key = "#pageable.pageNumber + '_' + #pageable.pageSize")
    Page<ProductoDTO> obtenerProductosEnOferta(Pageable pageable);
    
    /**
     * Cuenta la cantidad de productos activos en el sistema
     * @return número de productos activos
     */
    long contarProductosActivos();
}


