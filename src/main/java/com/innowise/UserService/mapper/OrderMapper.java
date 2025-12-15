package com.innowise.UserService.mapper;

import com.innowise.UserService.dto.ItemDto;
import com.innowise.UserService.dto.OrderDtoCreate;
import com.innowise.UserService.dto.OrderItemDto;
import com.innowise.UserService.entity.Item;
import com.innowise.UserService.entity.Order;
import com.innowise.UserService.entity.OrderItem;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import java.util.List;

@Mapper(componentModel = "spring", uses = {ItemMapper.class})
public interface OrderMapper {

    @Mapping(target = "userInfo", ignore = true) // заполняется отдельно в сервисе
    OrderDtoCreate toDto(Order order);

    Order toEntity(OrderDtoCreate orderDtoCreate);

    @Mapping(target = "item.id", source = "itemId")
    @Mapping(target = "order", ignore = true)
    OrderItem toEntity(OrderItemDto dto);

    @Mapping(target = "orderItems", ignore = true)
    @Mapping(target = "userInfo", ignore = true)
    OrderDtoCreate toDtoWithoutItems(Order order);

    OrderItemDto toDto(OrderItem orderItem);

    void updateOrderFromDto(OrderDtoCreate orderDtoCreate, @MappingTarget Order order);
}