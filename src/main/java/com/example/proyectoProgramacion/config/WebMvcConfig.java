package com.example.proyectoProgramacion.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

import java.util.Locale;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // Mapeo de rutas a vistas
        registry.addViewController("/").setViewName("web/index");
        registry.addViewController("/index").setViewName("web/index");
        registry.addViewController("/home").setViewName("web/index");
        
        // Rutas de autenticación
        registry.addViewController("/login").setViewName("auth/login");
        registry.addViewController("/auth/login").setViewName("auth/login");
        registry.addViewController("/auth/register").setViewName("auth/register");
        registry.addViewController("/auth/forgot-password").setViewName("auth/forgot-password");
    }

    @Bean(name = "customLocaleResolver")
    public LocaleResolver customLocaleResolver() {
        SessionLocaleResolver slr = new SessionLocaleResolver();
        slr.setDefaultLocale(Locale.forLanguageTag("es"));
        return slr;
    }

    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor() {
        LocaleChangeInterceptor lci = new LocaleChangeInterceptor();
        lci.setParamName("lang");
        return lci;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(localeChangeInterceptor());
    }
    
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Configuración para recursos estáticos
        registry.addResourceHandler(
                "/static/**",
                "/css/**",
                "/js/**",
                "/images/**",
                "/webjars/**")
                .addResourceLocations(
                        "classpath:/static/",
                        "classpath:/static/css/",
                        "classpath:/static/js/",
                        "classpath:/static/images/",
                        "classpath:/META-INF/resources/webjars/")
                .setCachePeriod(0);
    }
}
