package com.example.proyectoProgramacion.controller.web;

import com.example.proyectoProgramacion.exception.ResourceNotFoundException;
import com.example.proyectoProgramacion.model.dto.carrito.CarritoDTO;
import com.example.proyectoProgramacion.model.dto.carrito.ItemCarritoDTO;
import com.example.proyectoProgramacion.service.interfaces.CarritoService;
import com.example.proyectoProgramacion.service.interfaces.ProductoService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.Objects;

@Slf4j
@Controller
@RequestMapping("/carrito")
@RequiredArgsConstructor
@Validated
public class UnifiedCarritoController {

    private static final String REDIRECT_CARRITO = "redirect:/carrito";
    private static final String REDIRECT_TIENDA = "redirect:/tienda";
    private static final String ATTR_MENSAJE = "mensaje";
    private static final String ATTR_ERROR = "error";
    private static final String VISTA_CARRITO = "carrito/ver";
    
    private final CarritoService carritoService;
    private final ProductoService productoService;

    @GetMapping
    public String verCarrito(Model model, Authentication authentication) {
        try {
            CarritoDTO carrito = isAuthenticated(authentication)
                ? carritoService.obtenerCarritoPorUsuario(authentication.getName())
                : carritoService.obtenerCarritoSesion();

            model.addAttribute("carrito", carrito);
            model.addAttribute("tieneItems", carrito != null && carrito.getItems() != null && !carrito.getItems().isEmpty());
            
            // Calcular el total sumando los subtotales de cada ítem
            if (carrito != null && carrito.getItems() != null) {
                BigDecimal total = carrito.getItems().stream()
                        .map(ItemCarritoDTO::getSubtotal)
                        .filter(Objects::nonNull)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                model.addAttribute("total", total);
            } else {
                model.addAttribute("total", BigDecimal.ZERO);
            }
            
            return VISTA_CARRITO;
            
        } catch (Exception e) {
            log.error("Error al cargar el carrito: {}", e.getMessage(), e);
            model.addAttribute(ATTR_ERROR, "Error al cargar el carrito. Por favor, intente nuevamente.");
            return VISTA_CARRITO;
        }
    }
    
    private boolean isAuthenticated(Authentication authentication) {
        return authentication != null && authentication.isAuthenticated() && 
               !(authentication.getPrincipal() instanceof String && authentication.getPrincipal().equals("anonymousUser"));
    }

    @PostMapping("/agregar")
    @ResponseBody
    @Transactional
    public ResponseEntity<Map<String, Object>> agregarProducto(
            @RequestParam @Positive(message = "ID de producto inválido") Long productoId,
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "La cantidad debe ser al menos 1") Integer cantidad,
            Authentication authentication,
            HttpSession session) {
        
        Map<String, Object> response = new HashMap<>();
        
        if (!isAuthenticated(authentication)) {
            session.setAttribute("redirectAfterLogin", "/producto/" + productoId);
            response.put("redirect", "/auth/login");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        try {
            // Verificar si el producto existe
            if (productoService.obtenerProductoPorId(productoId) == null) {
                throw new ResourceNotFoundException("Producto no encontrado");
            }

            ItemCarritoDTO item = new ItemCarritoDTO();
            item.setProductoId(productoId);
            item.setCantidad(cantidad);

            String username = authentication.getName();
            carritoService.agregarProductoAlCarrito(username, item);
            
            // Obtener el carrito actualizado
            CarritoDTO carrito = carritoService.obtenerCarritoPorUsuario(username);
            int totalItems = carrito.getItems().stream()
                .mapToInt(ItemCarritoDTO::getCantidad)
                .sum();
            
            log.info("Producto {} agregado al carrito del usuario {}. Cantidad: {}", productoId, username, cantidad);
            
            response.put("success", true);
            response.put("message", "Producto agregado al carrito");
            response.put("totalItems", totalItems);
            
            return ResponseEntity.ok(response);

        } catch (ResourceNotFoundException e) {
            log.warn("Producto no encontrado: {}", productoId);
            response.put("success", false);
            response.put("message", "El producto solicitado no existe");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            
        } catch (Exception e) {
            log.error("Error al agregar producto al carrito: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Error al agregar el producto al carrito: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/actualizar")
    @Transactional
    public String actualizarCantidad(
            @RequestParam Long itemId,
            @RequestParam @Min(value = 1, message = "La cantidad debe ser al menos 1") Integer cantidad,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
                
        if (!isAuthenticated(authentication)) {
            return "redirect:/auth/login";
        }

        try {
            String username = authentication.getName();
            carritoService.actualizarCantidadItem(username, itemId, cantidad);
            redirectAttributes.addFlashAttribute(ATTR_MENSAJE, "Cantidad actualizada correctamente");
            
        } catch (Exception e) {
            log.error("Error al actualizar cantidad: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute(ATTR_ERROR, "Error al actualizar la cantidad");
        }
        
        return REDIRECT_CARRITO;
    }

    @PostMapping("/eliminar/{itemId}")
    @Transactional
    public String eliminarItem(
            @PathVariable Long itemId,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
                
        if (!isAuthenticated(authentication)) {
            return "redirect:/auth/login";
        }

        try {
            String username = authentication.getName();
            carritoService.eliminarItemDelCarrito(username, itemId);
            redirectAttributes.addFlashAttribute(ATTR_MENSAJE, "Producto eliminado del carrito");
            
        } catch (Exception e) {
            log.error("Error al eliminar ítem: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute(ATTR_ERROR, "Error al eliminar el producto del carrito");
        }
        
        return REDIRECT_CARRITO;
    }

    @PostMapping("/vaciar")
    @Transactional
    public String vaciarCarrito(
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
                
        if (!isAuthenticated(authentication)) {
            return "redirect:/auth/login";
        }

        try {
            String username = authentication.getName();
            carritoService.vaciarCarrito(username);
            redirectAttributes.addFlashAttribute(ATTR_MENSAJE, "Carrito vaciado correctamente");
            
        } catch (Exception e) {
            log.error("Error al vaciar carrito: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute(ATTR_ERROR, "Error al vaciar el carrito");
        }
        
        return REDIRECT_CARRITO;
    }
}
