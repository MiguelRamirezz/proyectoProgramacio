package com.example.proyectoProgramacion.controller.web;

import com.example.proyectoProgramacion.model.dto.auth.LoginRequestDTO;
import com.example.proyectoProgramacion.model.dto.auth.RegistroUsuarioDTO;
import com.example.proyectoProgramacion.service.interfaces.AdminRegistrationService;
import com.example.proyectoProgramacion.service.interfaces.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Slf4j
@Controller
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthUnifiedController {

    private final AuthService authService;
    private final AdminRegistrationService adminRegistrationService;

    @GetMapping("/login")
    public String mostrarLogin(
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "logout", required = false) String logout,
            @RequestParam(value = "redirect", required = false) String redirectUrl,
            Model model,
            HttpSession session) {
        
        // Si ya está autenticado, redirigir a la página principal
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !(auth.getPrincipal() instanceof String)) {
            return "redirect:/";
        }
        
        if (error != null) {
            model.addAttribute("error", "Usuario o contraseña incorrectos");
        }
        
        if (logout != null) {
            model.addAttribute("mensaje", "Has cerrado sesión correctamente");
        }
        
        return "auth/login";
    }

    @GetMapping("/register")
    public String mostrarRegistro(Model model) {
        log.info("Mostrando formulario de registro");
        
        if (!model.containsAttribute("usuario")) {
            model.addAttribute("usuario", new RegistroUsuarioDTO());
        }
        
        // Mostrar el campo de admin para todos
        model.addAttribute("mostrarCampoAdmin", true);
        log.info("Mostrando formulario de registro con opción de admin");
        
        return "auth/register";
    }

    @PostMapping("/register")
    public String registrarUsuario(
            @ModelAttribute("usuario") @Valid RegistroUsuarioDTO registroDTO,
            BindingResult result,
            @RequestParam(value = "esAdmin", required = false) String esAdminParam,
            @RequestParam(value = "adminAuthKey", required = false) String adminAuthKey,
            @RequestParam(value = "confirmPassword", required = false) String confirmPassword,
            @RequestParam(value = "terminosAceptados", required = false) String terminosAceptados,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        
        log.debug("Iniciando registro de usuario: {}", registroDTO.getUsername());
        
        // Validar que las contraseñas coincidan
        if (confirmPassword == null || !confirmPassword.equals(registroDTO.getPassword())) {
            result.rejectValue("password", "error.password.mismatch", "Las contraseñas no coinciden");
            log.warn("Error en registro: Las contraseñas no coinciden");
        }
        
        // Verificar si se marcó el checkbox de administrador
        boolean esAdmin = "on".equals(esAdminParam);
        registroDTO.setEsAdmin(esAdmin);
        
        // Validar términos y condiciones
        boolean terminosAceptadosBool = "on".equals(terminosAceptados);
        registroDTO.setTerminosAceptados(terminosAceptadosBool);
        
        // Validar campos obligatorios
        if (registroDTO.getUsername() == null || registroDTO.getUsername().trim().isEmpty()) {
            result.rejectValue("username", "error.username", "El nombre de usuario es obligatorio");
        }
        
        if (registroDTO.getPassword() == null || registroDTO.getPassword().trim().isEmpty()) {
            result.rejectValue("password", "error.password", "La contraseña es obligatoria");
        } else if (confirmPassword == null || confirmPassword.trim().isEmpty()) {
            result.rejectValue("confirmPassword", "error.confirmPassword", "Debes confirmar la contraseña");
        } else if (!registroDTO.getPassword().equals(confirmPassword)) {
            result.rejectValue("confirmPassword", "error.confirmPassword", "Las contraseñas no coinciden");
        }
        
        if (registroDTO.getEmail() == null || registroDTO.getEmail().trim().isEmpty()) {
            result.rejectValue("email", "error.email", "El correo electrónico es obligatorio");
        }
        
        // Validar clave de administrador si es necesario
        if (esAdmin) {
            if (adminAuthKey == null || adminAuthKey.trim().isEmpty()) {
                result.rejectValue("adminAuthKey", "error.adminAuthKey", 
                    "Se requiere una clave de autenticación para registrarse como administrador");
            } else if (!adminRegistrationService.validarClaveAdmin(adminAuthKey)) {
                result.rejectValue("adminAuthKey", "error.adminAuthKey", 
                    "Clave de autenticación inválida");
            } else {
                registroDTO.setAdminAuthKey(adminAuthKey);
            }
        }
        
        // Validar términos y condiciones
        if (!terminosAceptadosBool) {
            result.rejectValue("terminosAceptados", "error.terminosAceptados", 
                "Debes aceptar los términos y condiciones");
        }
        
        if (result.hasErrors()) {
            log.warn("Errores de validación en el registro: {}", result.getAllErrors());
            redirectAttributes.addFlashAttribute(
                "org.springframework.validation.BindingResult.usuario", 
                result
            );
            redirectAttributes.addFlashAttribute("usuario", registroDTO);
            redirectAttributes.addFlashAttribute("esAdmin", esAdmin);
            return "redirect:/auth/register";
        }
        
        try {
            // Registrar el usuario o administrador según corresponda
            if (esAdmin) {
                adminRegistrationService.registrarAdmin(registroDTO);
            } else {
                authService.registrarUsuario(registroDTO);
            }
            
            String mensajeExito = String.format(
                "¡Registro de %s exitoso! Por favor inicia sesión", 
                esAdmin ? "administrador" : "usuario"
            );
            redirectAttributes.addFlashAttribute("mensaje", mensajeExito);
            
            // Redirigir a la URL guardada o a la página de inicio
            String redirectUrl = (String) session.getAttribute("redirectAfterLogin");
            if (redirectUrl != null && !redirectUrl.trim().isEmpty()) {
                session.removeAttribute("redirectAfterLogin");
                return "redirect:" + redirectUrl;
            }
            
            return "redirect:/auth/login";
            
        } catch (Exception e) {
            log.error("Error al registrar usuario: {}", e.getMessage(), e);
            String mensajeError = e.getMessage();
            if (e.getCause() != null) {
                mensajeError += ": " + e.getCause().getMessage();
            }
            redirectAttributes.addFlashAttribute("error", 
                "Error al registrar el usuario: " + mensajeError);
            redirectAttributes.addFlashAttribute("usuario", registroDTO);
            redirectAttributes.addFlashAttribute("esAdmin", esAdmin);
            return "redirect:/auth/register";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpServletRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            new SecurityContextLogoutHandler().logout(request, null, auth);
        }
        return "redirect:/auth/login?logout=true";
    }

    @GetMapping("/denied")
    public String accesoDenegado() {
        return "error/403";
    }

    @GetMapping("/acceso-denegado")
    public String accesoDenegadoAlternativo() {
        return "error/403";
    }
    
    /**
     * Verifica si el usuario actual tiene el rol de administrador
     * @return true si el usuario es administrador, false en caso contrario
     */
    private boolean hasAdminRole() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role -> role.equals("ROLE_ADMIN"));
    }
    
    @GetMapping("/forgot-password")
    public String mostrarOlvidePassword(Model model) {
        model.addAttribute("pageTitle", "Recuperar Contraseña | Gellverse");
        return "auth/forgot-password";
    }
    
    @PostMapping("/forgot-password")
    public String procesarOlvidePassword(
            @RequestParam("email") String email,
            RedirectAttributes redirectAttributes) {
        try {
            // Aquí iría la lógica para enviar el correo de recuperación
            // authService.enviarCorreoRecuperacion(email);
            
            redirectAttributes.addFlashAttribute("success", 
                "Si el correo está registrado, recibirás un enlace para restablecer tu contraseña.");
            return "redirect:/auth/forgot-password";
        } catch (Exception e) {
            log.error("Error al procesar la recuperación de contraseña", e);
            redirectAttributes.addFlashAttribute("error", 
                "Ocurrió un error al procesar la solicitud. Por favor, inténtalo de nuevo.");
            return "redirect:/auth/forgot-password";
        }
    }
    
    @GetMapping("/reset-password")
    public String mostrarResetPassword(
            @RequestParam("token") String token,
            Model model) {
        // Aquí podrías validar el token
        model.addAttribute("token", token);
        model.addAttribute("pageTitle", "Restablecer Contraseña | Gellverse");
        return "auth/reset-password";
    }
    
    @PostMapping("/reset-password")
    public String procesarResetPassword(
            @RequestParam("token") String token,
            @RequestParam("password") String password,
            @RequestParam("confirmPassword") String confirmPassword,
            RedirectAttributes redirectAttributes) {
        
        if (!password.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("error", "Las contraseñas no coinciden");
            return "redirect:/auth/reset-password?token=" + token;
        }
        
        try {
            // Aquí iría la lógica para actualizar la contraseña
            // authService.actualizarPassword(token, password);
            
            redirectAttributes.addFlashAttribute("success", 
                "Tu contraseña ha sido actualizada correctamente. Ahora puedes iniciar sesión.");
            return "redirect:/auth/login";
        } catch (Exception e) {
            log.error("Error al restablecer la contraseña", e);
            redirectAttributes.addFlashAttribute("error", 
                "El enlace de restablecimiento es inválido o ha expirado. Por favor, solicita uno nuevo.");
            return "redirect:/auth/forgot-password";
        }
    }
}
