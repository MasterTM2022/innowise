package com.innowise.UserService.mapper;

import com.innowise.UserService.dto.ItemDto;
import com.innowise.UserService.entity.Item;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ItemMapper {
    ItemDto toDto(Item item);
    Item toEntity(ItemDto dto);
}
