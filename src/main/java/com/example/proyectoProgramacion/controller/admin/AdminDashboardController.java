package com.example.proyectoProgramacion.controller.admin;

import com.example.proyectoProgramacion.model.dto.producto.ProductoDTO;
import com.example.proyectoProgramacion.model.dto.usuario.UsuarioDTO;
import com.example.proyectoProgramacion.model.dto.orden.OrdenDTO;
import com.example.proyectoProgramacion.service.interfaces.ProductoService;
import com.example.proyectoProgramacion.service.interfaces.UsuarioService;
import com.example.proyectoProgramacion.service.interfaces.OrdenService;
import com.example.proyectoProgramacion.service.interfaces.CategoriaService;
import com.example.proyectoProgramacion.util.AppConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.proyectoProgramacion.model.enums.EstadoOrden;
import java.util.List;
import java.util.Objects;

@Slf4j
@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ROLE_ADMIN')")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final ProductoService productoService;
    private final UsuarioService usuarioService;
    private final OrdenService ordenService;
    private final CategoriaService categoriaService;

    @GetMapping
    public String panelAdmin(Model model) {
        try {
            // Obtener estadísticas para el panel de control
            long totalProductos = productoService.obtenerTodosLosProductos(Pageable.unpaged()).getTotalElements();
            long totalUsuarios = usuarioService.obtenerTodosLosUsuarios(Pageable.unpaged()).getTotalElements();
            long totalOrdenes = ordenService.obtenerTodasLasOrdenes(Pageable.unpaged()).getTotalElements();
            
            // Obtener las últimas órdenes
            Pageable ultimasOrdenesPageable = PageRequest.of(0, 5, Sort.by("fechaCreacion").descending());
            List<OrdenDTO> ultimasOrdenes = ordenService.obtenerTodasLasOrdenes(ultimasOrdenesPageable).getContent();
            
            model.addAttribute("totalProductos", totalProductos);
            model.addAttribute("totalUsuarios", totalUsuarios);
            model.addAttribute("totalOrdenes", totalOrdenes);
            model.addAttribute("ultimasOrdenes", ultimasOrdenes);
            
            return "admin/dashboard";
            
        } catch (Exception e) {
            log.error("Error al cargar el panel de administración: {}", e.getMessage(), e);
            model.addAttribute("error", "Error al cargar el panel de administración");
            return "error";
        }
    }

    // Gestión de Productos
    @GetMapping("/productos")
    public String listarProductos(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sort", defaultValue = "id") String sort,
            @RequestParam(value = "direction", defaultValue = "asc") String direction,
            Model model) {
        
        Sort.Direction sortDirection = "desc".equalsIgnoreCase(direction) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, sortDirection, sort);
        
        Page<ProductoDTO> productos = productoService.obtenerTodosLosProductos(pageable);
        
        model.addAttribute("productos", productos);
        model.addAttribute("sort", sort);
        model.addAttribute("direction", direction);
        model.addAttribute("pageSizes", new int[]{5, 10, 20, 50});
        
        return "admin/productos/lista";
    }

    @GetMapping("/productos/nuevo")
    public String mostrarFormularioNuevoProducto(Model model) {
        model.addAttribute("producto", new ProductoDTO());
        model.addAttribute("categorias", categoriaService.obtenerTodasLasCategorias());
        return "admin/productos/formulario";
    }

    @PostMapping(value = "/productos/guardar", consumes = {"multipart/form-data"})
    public String guardarProducto(
            @ModelAttribute("producto") ProductoDTO productoDTO,
            @RequestParam(value = "imagen", required = false) MultipartFile imagen,
            RedirectAttributes redirectAttributes) {
        
        try {
            if (imagen != null && !imagen.isEmpty()) {
                // Guardar la imagen y actualizar la URL en el DTO
                productoDTO = productoService.subirImagen(productoDTO.getId(), imagen);
            }
            
            if (productoDTO.getId() == null) {
                productoService.crearProducto(productoDTO);
                redirectAttributes.addFlashAttribute("mensaje", "Producto creado exitosamente");
            } else {
                productoService.actualizarProducto(productoDTO.getId(), productoDTO);
                redirectAttributes.addFlashAttribute("mensaje", "Producto actualizado exitosamente");
            }
            
            return "redirect:/admin/productos";
            
        } catch (Exception e) {
            log.error("Error al guardar el producto: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Error al guardar el producto: " + e.getMessage());
            return "redirect:/admin/productos/nuevo";
        }
    }

    @GetMapping("/productos/editar/{id}")
    public String mostrarFormularioEditarProducto(@PathVariable Long id, Model model) {
        try {
            ProductoDTO productoDTO = productoService.obtenerProductoPorId(id);
            model.addAttribute("producto", productoDTO);
            model.addAttribute("categorias", categoriaService.obtenerTodasLasCategorias());
            return "admin/productos/formulario";
        } catch (Exception e) {
            log.error("Error al cargar el producto para editar: {}", e.getMessage(), e);
            return "redirect:/admin/productos";
        }
    }

    @PostMapping("/productos/eliminar/{id}")
    public String eliminarProducto(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            productoService.eliminarProducto(id);
            redirectAttributes.addFlashAttribute("mensaje", "Producto eliminado exitosamente");
        } catch (Exception e) {
            log.error("Error al eliminar el producto: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Error al eliminar el producto: " + e.getMessage());
        }
        return "redirect:/admin/productos";
    }

    // Gestión de Usuarios
    @GetMapping("/usuarios")
    public String listarUsuarios(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sort", defaultValue = "id") String sort,
            @RequestParam(value = "direction", defaultValue = "asc") String direction,
            Model model) {
        
        Sort.Direction sortDirection = "desc".equalsIgnoreCase(direction) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, sortDirection, sort);
        
        Page<UsuarioDTO> usuarios = usuarioService.obtenerTodosLosUsuarios(pageable);
        
        model.addAttribute("usuarios", usuarios);
        model.addAttribute("sort", sort);
        model.addAttribute("direction", direction);
        model.addAttribute("pageSizes", new int[]{5, 10, 20, 50});
        
        return "admin/usuarios/lista";
    }

    // Gestión de Órdenes
    @GetMapping("/ordenes")
    public String listarOrdenes(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sort", defaultValue = "fechaCreacion") String sort,
            @RequestParam(value = "direction", defaultValue = "desc") String direction,
            Model model) {
        
        // Validar parámetros
        size = Math.min(size, 50);
        page = Math.max(0, page);
        
        Sort.Direction sortDirection = "desc".equalsIgnoreCase(direction) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, sortDirection, sort);
        
        Page<OrdenDTO> ordenes = ordenService.obtenerTodasLasOrdenes(pageable);
        
        model.addAttribute("ordenes", ordenes);
        model.addAttribute("sort", sort);
        model.addAttribute("direction", direction);
        model.addAttribute("pageSizes", new int[]{5, 10, 20, 50});
        
        return "admin/ordenes/lista";
    }

    @GetMapping("/ordenes/{id}")
    public String verDetalleOrden(@PathVariable Long id, Model model) {
        try {
            OrdenDTO orden = ordenService.obtenerOrdenPorId(id);
            model.addAttribute("orden", orden);
            return "admin/ordenes/detalle";
        } catch (Exception e) {
            log.error("Error al cargar el detalle de la orden: {}", e.getMessage(), e);
            return "redirect:/admin/ordenes";
        }
    }

    @PostMapping("/ordenes/{id}/estado")
    public String actualizarEstadoOrden(
            @PathVariable Long id,
            @RequestParam EstadoOrden nuevoEstado,
            RedirectAttributes redirectAttributes) {
        
        try {
            ordenService.actualizarEstadoOrden(id, nuevoEstado);
            redirectAttributes.addFlashAttribute("mensaje", "Estado de la orden actualizado exitosamente");
        } catch (IllegalArgumentException e) {
            log.error("Estado de orden no válido: {}", nuevoEstado, e);
            redirectAttributes.addFlashAttribute("error", "Estado de orden no válido: " + nuevoEstado);
        } catch (Exception e) {
            log.error("Error al actualizar el estado de la orden: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Error al actualizar el estado de la orden: " + e.getMessage());
        }
        
        return "redirect:/admin/ordenes/" + id;
    }
}
