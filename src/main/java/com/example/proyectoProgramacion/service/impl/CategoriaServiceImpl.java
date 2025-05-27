package com.example.proyectoProgramacion.service.impl;

import com.example.proyectoProgramacion.model.dto.producto.CategoriaDTO;
import com.example.proyectoProgramacion.model.enums.Categoria;
import com.example.proyectoProgramacion.service.interfaces.CategoriaService;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.constraints.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Validated
public class CategoriaServiceImpl implements CategoriaService {

    @Override
    public CategoriaDTO obtenerCategoriaPorTipo(@NotNull Categoria categoria) {
        CategoriaDTO dto = new CategoriaDTO();
        dto.setCategoria(categoria);
        dto.setDescripcion(obtenerDescripcionCategoria(categoria));
        return dto;
    }

    @Override
    public List<CategoriaDTO> obtenerTodasLasCategorias() {
        return Arrays.stream(Categoria.values())
                .map(categoria -> {
                    CategoriaDTO dto = new CategoriaDTO();
                    dto.setCategoria(categoria);
                    dto.setDescripcion(obtenerDescripcionCategoria(categoria));
                    return dto;
                })
                .collect(Collectors.toList());
    }

    private String obtenerDescripcionCategoria(Categoria categoria) {
        return switch (categoria) {
            case ANIME -> "Ropa estilo anime";
            case VIDEOJUEGOS -> "Ropa estilo videojuegos";
        };
    }
}


