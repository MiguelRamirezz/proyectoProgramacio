package com.example.proyectoProgramacion.service.interfaces;

import com.example.proyectoProgramacion.model.dto.producto.CategoriaDTO;
import com.example.proyectoProgramacion.model.enums.Categoria;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Validated
public interface CategoriaService {

    /**
     * Obtiene información de una categoría por su tipo
     * @param categoria El tipo de categoría
     * @return DTO con la información de la categoría
     */
    CategoriaDTO obtenerCategoriaPorTipo(@NotNull Categoria categoria);

    /**
     * Obtiene todas las categorías disponibles
     * @return Lista con todas las categorías
     */
    List<CategoriaDTO> obtenerTodasLasCategorias();
}


