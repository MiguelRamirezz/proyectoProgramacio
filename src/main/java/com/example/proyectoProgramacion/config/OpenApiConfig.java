package com.example.proyectoProgramacion.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "bearerAuth";
        
        return new OpenAPI()
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(
                        new Components()
                                .addSecuritySchemes(securitySchemeName,
                                        new SecurityScheme()
                                                .name(securitySchemeName)
                                                .type(SecurityScheme.Type.HTTP)
                                                .scheme("bearer")
                                                .bearerFormat("JWT")
                                )
                )
                .info(apiInfo());
    }

    private Info apiInfo() {
        return new Info()
                .title("API de Tienda Online")
                .description("""
                        ## Documentación de la API de Tienda Online
                        
                        Bienvenido a la documentación de la API de Tienda Online. Esta API permite gestionar productos, 
                        usuarios, pedidos y más para una tienda en línea.
                        
                        ### Autenticación
                        La API utiliza JWT (JSON Web Tokens) para la autenticación. Para autenticarte:
                        
                        1. Realiza una petición POST a `/api/auth/login` con tus credenciales
                        2. Usa el token recibido en el encabezado `Authorization: Bearer <token>`
                        
                        ### Códigos de estado comunes
                        - 200: OK - La petición se ha completado con éxito
                        - 201: Creado - Recurso creado exitosamente
                        - 400: Solicitud incorrecta - Los datos enviados no son válidos
                        - 401: No autorizado - Se requiere autenticación
                        - 403: Prohibido - No tienes permisos para acceder al recurso
                        - 404: No encontrado - El recurso solicitado no existe
                        - 500: Error del servidor - Ha ocurrido un error inesperado
                        
                        ### Soporte
                        Si tienes preguntas o necesitas ayuda, contacta al equipo de soporte en soporte@gellverse.com
                        """)
                .version("1.0.0")
                .contact(new Contact()
                        .name("Soporte Técnico")
                        .email("soporte@gellverse.com"))
                .license(new License()
                        .name("Licencia MIT")
                        .url("https://opensource.org/licenses/MIT"));
    }
}
