package com.example.proyectoProgramacion.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                // Solo permitir el puerto 8080
                .allowedOrigins(
                        "http://localhost:8080"
                )
                // Limitar los métodos permitidos a los necesarios
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                // Especificar cabeceras permitidas en lugar de todas
                .allowedHeaders(
                        "Authorization",
                        "Content-Type",
                        "X-Requested-With",
                        "Accept",
                        "Origin"
                )
                .exposedHeaders("Authorization")
                .allowCredentials(true)
                .maxAge(3600); // 1 hora de caché para preflight
    }
}


