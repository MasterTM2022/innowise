package com.innowise.order.mapper;

import com.innowise.order.dto.ItemDto;
import com.innowise.order.entity.Item;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ItemMapper {
    ItemDto toDto(Item item);
    Item toEntity(ItemDto dto);
}
