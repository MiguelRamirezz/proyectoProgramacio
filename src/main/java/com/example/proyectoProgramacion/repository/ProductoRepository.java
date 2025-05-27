package com.example.proyectoProgramacion.repository;

import com.example.proyectoProgramacion.model.entity.Producto;
import com.example.proyectoProgramacion.model.enums.Categoria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long>, JpaSpecificationExecutor<Producto> {

    /**
     * Busca productos por categoría que estén activos.
     * @param categoria Categoría de los productos
     * @param pageable Configuración de paginación
     * @return Página de productos de la categoría especificada que están activos
     */
    @Query("SELECT p FROM Producto p WHERE p.categoria = :categoria AND p.activo = true")
    Page<Producto> findByCategoriaAndActivoTrue(@Param("categoria") Categoria categoria, Pageable pageable);

    /**
     * Busca productos por nombre o descripción que contengan el término de búsqueda.
     */
    Page<Producto> findByNombreContainingIgnoreCaseOrDescripcionContainingIgnoreCase(
            String nombre, String descripcion, Pageable pageable);

    /**
     * Obtiene productos destacados y activos ordenados por fecha de creación descendente.
     */
    List<Producto> findByDestacadoTrueAndActivoTrueOrderByFechaCreacionDesc();

    /**
     * Busca productos por categoría.
     * @param categoria La categoría de los productos a buscar
     * @param pageable Configuración de paginación
     * @return Página de productos de la categoría especificada que están activos
     */
    @Query("SELECT p FROM Producto p WHERE p.categoria = :categoria AND p.activo = true")
    Page<Producto> findByCategoria(@Param("categoria") Categoria categoria, Pageable pageable);
    
    /**
     * Busca productos con precio menor o igual al especificado.
     */
    Page<Producto> findByPrecioLessThanEqual(BigDecimal precio, Pageable pageable);
    
    /**
     * Busca productos con descuento mayor a cero y que estén activos.
     * @param descuento Valor mínimo del descuento
     * @param pageable Configuración de paginación
     * @return Página de productos con descuento que están activos
     */
    @Query("SELECT p FROM Producto p WHERE p.descuento > :descuento AND p.activo = true")
    Page<Producto> findByDescuentoGreaterThanAndActivoTrue(@Param("descuento") BigDecimal descuento, Pageable pageable);
    
    /**
     * Busca productos con precio mayor o igual al especificado.
     */
    Page<Producto> findByPrecioGreaterThanEqual(BigDecimal precio, Pageable pageable);
    
    /**
     * Busca productos cuyo nombre de categoría contenga el texto especificado (ignorando mayúsculas/minúsculas).
     * @param nombreCategoria Texto a buscar en el nombre de la categoría
     * @param pageable Configuración de paginación
     * @return Página de productos cuyas categorías coincidan con el texto de búsqueda
     */
    /**
     * Busca productos cuyo nombre de categoría contenga el texto especificado (ignorando mayúsculas/minúsculas).
     * @param nombreCategoria Texto a buscar en el nombre de la categoría
     * @param pageable Configuración de paginación
     * @return Página de productos cuyas categorías coincidan con el texto de búsqueda
     */
    @Query("SELECT p FROM Producto p WHERE LOWER(p.categoria) LIKE LOWER(concat('%', :nombreCategoria, '%')) AND p.activo = true")
    Page<Producto> findByCategoriaNombreContainingIgnoreCase(@Param("nombreCategoria") String nombreCategoria, Pageable pageable);
    
    /**
     * Busca productos con precio entre los valores especificados.
     */
    @Query("SELECT p FROM Producto p WHERE p.precio BETWEEN :precioMin AND :precioMax")
    Page<Producto> findByPrecioBetween(
            @Param("precioMin") BigDecimal precioMin,
            @Param("precioMax") BigDecimal precioMax,
            Pageable pageable);
            
    /**
     * Busca productos con descuento mayor al especificado.
     */
    Page<Producto> findByDescuentoGreaterThan(BigDecimal descuento, Pageable pageable);
    
    /**
     * Cuenta la cantidad de productos activos.
     */
    long countByActivoTrue();
}






