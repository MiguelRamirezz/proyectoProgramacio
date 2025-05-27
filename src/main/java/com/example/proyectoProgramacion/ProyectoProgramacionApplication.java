/// src/main/java/com/example/proyectoProgramacion/ProyectoProgramacionApplication.java
package com.example.proyectoProgramacion;

import org.modelmapper.ModelMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.example.proyectoProgramacion.repository")
@EntityScan(basePackages = "com.example.proyectoProgramacion.model")
@ComponentScan(basePackages = {
    "com.example.proyectoProgramacion",
    "com.example.proyectoProgramacion.controller",
    "com.example.proyectoProgramacion.service",
    "com.example.proyectoProgramacion.service.interfaces",
    "com.example.proyectoProgramacion.config",
    "com.example.proyectoProgramacion.security"
})
public class ProyectoProgramacionApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProyectoProgramacionApplication.class, args);
	}

	@Bean
	public ModelMapper modelMapper() {
		return new ModelMapper();
	}
}


