package com.innowise.order.mapper;

import com.innowise.order.dto.OrderDtoCreate;
import com.innowise.order.dto.OrderItemDto;
import com.innowise.order.dto.OrderResponseDto;
import com.innowise.order.entity.Order;
import com.innowise.order.entity.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", uses = {ItemMapper.class})
public interface OrderMapper {

    OrderDtoCreate toCreateDto(Order order);

    OrderResponseDto toResponseDto(Order order);

    Order toEntity(OrderDtoCreate orderDtoCreate);

    @Mapping(target = "item.id", source = "itemId")
    @Mapping(target = "order", ignore = true)
    OrderItem toEntity(OrderItemDto dto);

    @Mapping(target = "orderItems", ignore = true)
    @Mapping(target = "userId", ignore = true)
    OrderDtoCreate toDtoWithoutItems(Order order);

    OrderItemDto toCreateDto(OrderItem orderItem);

    void updateOrderFromDto(OrderDtoCreate orderDtoCreate, @MappingTarget Order order);
}