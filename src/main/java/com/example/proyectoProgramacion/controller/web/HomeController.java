package com.example.proyectoProgramacion.controller.web;

import com.example.proyectoProgramacion.model.enums.Categoria;
import com.example.proyectoProgramacion.model.dto.producto.CategoriaDTO;
import com.example.proyectoProgramacion.model.dto.producto.ProductoDTO;
import com.example.proyectoProgramacion.service.interfaces.CategoriaService;
import com.example.proyectoProgramacion.service.interfaces.ProductoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@Slf4j
@RequiredArgsConstructor
public class HomeController {

    private final ProductoService productoService;
    private final CategoriaService categoriaService;
    
    private static final int PRODUCTOS_DESTACADOS = 8;
    private static final int PRODUCTOS_NUEVOS = 4;

    @GetMapping("/")
    public String home(Model model) {
        try {
            log.info("Cargando página de inicio...");
            
            // Obtener categorías para el menú
            List<CategoriaDTO> categorias = categoriaService.obtenerTodasLasCategorias();
            model.addAttribute("categorias", categorias);
            
            // Obtener productos destacados (con oferta)
            Pageable destacadosPageable = PageRequest.of(0, PRODUCTOS_DESTACADOS, Sort.by("fechaCreacion").descending());
            List<ProductoDTO> productosDestacados = productoService.obtenerProductosEnOferta(destacadosPageable).getContent();
            model.addAttribute("productosDestacados", productosDestacados);

            // Obtener productos nuevos
            Pageable nuevosPageable = PageRequest.of(0, PRODUCTOS_NUEVOS, Sort.by("fechaCreacion").descending());
            List<ProductoDTO> productosNuevos = productoService.obtenerTodosLosProductos(nuevosPageable).getContent();
            model.addAttribute("productosNuevos", productosNuevos);

            // Metadatos para SEO
            model.addAttribute("title", "Inicio - Gellverse");
            model.addAttribute("pageTitle", "Bienvenido a Gellverse");
            model.addAttribute("pageDescription", "Descubre las últimas tendencias en moda con nuestra exclusiva colección de ropa y accesorios.");
            
            log.info("Página de inicio cargada exitosamente con {} productos destacados y {} productos nuevos", 
                    productosDestacados.size(), productosNuevos.size());
                    
            return "web/index";
            
        } catch (Exception e) {
            log.error("Error al cargar la página de inicio: {}", e.getMessage(), e);
            model.addAttribute("error", "Lo sentimos, ha ocurrido un error al cargar la página de inicio. Por favor, inténtalo de nuevo más tarde.");
            return "error/error";
        }
    }

    @GetMapping("/about")
    public String about(Model model) {
        model.addAttribute("viewName", "about");
        return "fragments/layout";
    }

    @GetMapping("/contact")
    public String contact(Model model) {
        model.addAttribute("viewName", "contact");
        return "fragments/layout";
    }

    @GetMapping("/terms")
    public String terms(Model model) {
        model.addAttribute("viewName", "terms");
        return "fragments/layout";
    }

    @GetMapping("/privacy")
    public String privacy(Model model) {
        model.addAttribute("viewName", "privacy");
        return "fragments/layout";
    }
}


