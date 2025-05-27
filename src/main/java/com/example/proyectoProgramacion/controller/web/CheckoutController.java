package com.example.proyectoProgramacion.controller.web;

import com.example.proyectoProgramacion.model.dto.carrito.CarritoDTO;
import com.example.proyectoProgramacion.model.dto.direccion.DireccionDTO;
import com.example.proyectoProgramacion.model.dto.orden.OrdenDTO;
import com.example.proyectoProgramacion.model.dto.orden.OrdenRequestDTO;
import com.example.proyectoProgramacion.model.dto.pago.PagoRequestDTO;
import com.example.proyectoProgramacion.model.dto.pago.PagoResponseDTO;
import com.example.proyectoProgramacion.exception.BusinessException;
import com.example.proyectoProgramacion.model.enums.EstadoOrden;
import com.example.proyectoProgramacion.service.interfaces.CarritoService;
import com.example.proyectoProgramacion.service.interfaces.OrdenService;
import com.example.proyectoProgramacion.service.interfaces.PagoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/checkout")
@PreAuthorize("isAuthenticated()")
public class CheckoutController {

    private final CarritoService carritoService;
    private final OrdenService ordenService;
    private final PagoService pagoService;

    @Autowired
    public CheckoutController(CarritoService carritoService, 
                            OrdenService ordenService,
                            PagoService pagoService) {
        this.carritoService = carritoService;
        this.ordenService = ordenService;
        this.pagoService = pagoService;
    }

    @GetMapping
    public String mostrarCheckout(Model model, Authentication authentication) {
        String username = authentication.getName();

        // Validar estado del carrito antes del checkout
        CarritoDTO carrito = carritoService.obtenerCarritoPorUsuario(username);
        if (carrito == null || carrito.getItems() == null || carrito.getItems().isEmpty()) {
            model.addAttribute("error", "No hay productos en el carrito para procesar");
            return "redirect:/carrito";
        }

        // Verificar stock disponible para todos los productos
        try {
            // Validar stock para cada ítem en el carrito
            carrito.getItems().forEach(item -> {
                if (item.getCantidad() > item.getStockDisponible()) {
                    throw new BusinessException("No hay suficiente stock para el producto: " + 
                        item.getNombreProducto());
                }
            });
        } catch (BusinessException e) {
            model.addAttribute("error", e.getMessage());
            return "redirect:/carrito";
        }

        // Obtener información del usuario
        model.addAttribute("direcciones", List.of()); // Lista vacía temporal
        model.addAttribute("carrito", carrito);
        model.addAttribute("nuevaDireccion", new DireccionDTO());
        model.addAttribute("ordenRequest", new OrdenRequestDTO());

        return "checkout/datos";
    }

    @PostMapping("/direccion")
    public String guardarDireccion(
            @Valid @ModelAttribute("nuevaDireccion") DireccionDTO direccion,
            BindingResult result,
            Authentication authentication,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (result.hasErrors()) {
            String username = authentication.getName();
            model.addAttribute("direcciones", List.of());
            model.addAttribute("carrito", carritoService.obtenerCarritoPorUsuario(username));
            model.addAttribute("ordenRequest", new OrdenRequestDTO());
            return "checkout/datos";
        }

        try {
            String username = authentication.getName();
            // Nota: Necesitarás implementar la gestión de direcciones en UsuarioService
            // usuarioService.agregarDireccion(username, direccion);
            redirectAttributes.addFlashAttribute("mensaje", "Dirección guardada exitosamente");
            return "redirect:/checkout";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al guardar dirección: " + e.getMessage());
            return "redirect:/checkout";
        }
    }

    @PostMapping("/confirmar")
    @Transactional
    public String confirmarOrden(
            @Valid @ModelAttribute("ordenRequest") OrdenRequestDTO ordenRequest,
            BindingResult result,
            Authentication authentication,
            RedirectAttributes redirectAttributes,
            Model model) {

        String username = authentication.getName();

        if (result.hasErrors()) {
            model.addAttribute("direcciones", List.of());
            model.addAttribute("carrito", carritoService.obtenerCarritoPorUsuario(username));
            return "checkout/datos";
        }

        try {
            // Validar que el carrito no esté vacío
            CarritoDTO carrito = carritoService.obtenerCarritoPorUsuario(username);
            if (carrito == null || carrito.getItems() == null || carrito.getItems().isEmpty()) {
                throw new BusinessException("El carrito está vacío");
            }

            // Crear la orden usando el método de OrdenService
            String direccionEnvio = ordenRequest.getDireccionEnvio();
            OrdenDTO orden = ordenService.crearOrden(username, direccionEnvio);

            // Redireccionar a la página de pago
            return "redirect:/checkout/pago/" + orden.getId();
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/checkout";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al procesar la orden: " + e.getMessage());
            return "redirect:/checkout";
        }
    }

    @GetMapping("/pago/{ordenId}")
    public String mostrarPago(@PathVariable Long ordenId, Model model, Authentication authentication) {
        String username = authentication.getName();

        try {
            // Obtener la orden por ID
            OrdenDTO orden = ordenService.obtenerOrdenPorId(ordenId);
            
            // Verificar que la orden pertenece al usuario actual
            if (!ordenService.verificarPropiedadOrden(ordenId, username)) {
                throw new BusinessException("No tiene permiso para ver esta orden");
            }

            model.addAttribute("orden", orden);
            
            // Lista temporal de métodos de pago
            model.addAttribute("metodosPago", List.of("TARJETA_CREDITO", "PAYPAL"));

            return "checkout/pago";
        } catch (BusinessException e) {
            model.addAttribute("error", e.getMessage());
            return "redirect:/usuario/ordenes";
        }
    }

    @PostMapping("/pago/{ordenId}")
    @Transactional
    public String procesarPago(
            @PathVariable Long ordenId,
            @RequestParam String metodoPago,
            @RequestParam String numeroTarjeta,
            @RequestParam String fechaExpiracion,
            @RequestParam String cvv,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        String username = authentication.getName();

        try {
            // 1. Obtener la orden por ID
            OrdenDTO orden = ordenService.obtenerOrdenPorId(ordenId);
            
            // 2. Verificar que la orden pertenece al usuario actual
            if (!ordenService.verificarPropiedadOrden(ordenId, username)) {
                throw new BusinessException("No tiene permiso para pagar esta orden");
            }

            // 3. Crear DTO de pago
            PagoRequestDTO pagoRequest = new PagoRequestDTO();
            pagoRequest.setNumeroTarjeta(numeroTarjeta);
            pagoRequest.setFechaExpiracion(fechaExpiracion);
            pagoRequest.setCvv(cvv);
            pagoRequest.setMetodoPago(metodoPago);
            pagoRequest.setMonto(orden.getTotal());
            
            // 4. Procesar el pago
            PagoResponseDTO pagoResponse = pagoService.procesarPago(pagoRequest, username);
            
            // 5. Actualizar el estado de la orden a PAGADA
            ordenService.actualizarEstadoOrden(ordenId, EstadoOrden.PAGADA);
            
            // 6. Retornar respuesta exitosa
            redirectAttributes.addFlashAttribute("mensaje", 
                "Pago procesado exitosamente. Referencia: " + pagoResponse.getId()
            );
            return "redirect:/checkout/confirmacion/" + ordenId;
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/checkout/pago/" + ordenId;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al procesar el pago: " + e.getMessage());
            return "redirect:/checkout/pago/" + ordenId;
        }
    }

    @GetMapping("/confirmacion/{ordenId}")
    public String mostrarConfirmacion(@PathVariable Long ordenId, Model model, Authentication authentication) {
        String username = authentication.getName();

        try {
            // Obtener la orden por ID
            OrdenDTO orden = ordenService.obtenerOrdenPorId(ordenId);
            
            // Verificar que la orden pertenece al usuario actual
            if (!ordenService.verificarPropiedadOrden(ordenId, username)) {
                throw new BusinessException("No tiene permiso para ver esta orden");
            }

            model.addAttribute("orden", orden);
            return "checkout/confirmacion";
        } catch (BusinessException e) {
            model.addAttribute("error", e.getMessage());
            return "redirect:/usuario/ordenes";
        }
    }
}
