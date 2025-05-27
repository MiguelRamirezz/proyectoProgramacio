package com.example.proyectoProgramacion.controller.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletRequest;

@Controller
public class LanguageController {

    @GetMapping("/change-language")
    public String changeLanguage(@RequestParam String lang, 
                                HttpServletRequest request,
                                RedirectAttributes redirectAttributes) {
        // La lógica de cambio de idioma se maneja automáticamente por LocaleChangeInterceptor
        String referer = request.getHeader("Referer");
        return "redirect:" + (referer != null ? referer : "/");
    }
}
