package com.example.proyectoProgramacion.mapper;

import com.example.proyectoProgramacion.model.dto.producto.ProductoDTO;
import com.example.proyectoProgramacion.model.entity.Producto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

/**
 * Mapper for the entity {@link Producto} and its DTO {@link ProductoDTO}.
 */
@Mapper(componentModel = "spring", uses = {})
@org.springframework.stereotype.Component
public interface ProductoMapper {

    ProductoMapper INSTANCE = Mappers.getMapper(ProductoMapper.class);

    /**
     * Converts a Producto entity to a ProductoDTO.
     *
     * @param producto the entity to convert
     * @return the DTO
     */
    ProductoDTO toDto(Producto producto);

    /**
     * Converts a ProductoDTO to a Producto entity.
     *
     * @param productoDTO the DTO to convert
     * @return the entity
     */
    Producto toEntity(ProductoDTO productoDTO);

    /**
     * Updates a Producto entity with values from ProductoDTO.
     *
     * @param productoDTO the DTO with new values
     * @param producto the entity to update
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "itemsCarrito", ignore = true)
    @Mapping(target = "itemsOrden", ignore = true)
    void updateEntityFromDto(ProductoDTO productoDTO, @MappingTarget Producto producto);
}
