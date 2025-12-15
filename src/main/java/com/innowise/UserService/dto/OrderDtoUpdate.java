package com.innowise.UserService.dto;

import com.innowise.UserService.entity.OrderStatus;
import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode

public class OrderDtoUpdate {
    private Long id;

    private OrderStatus status;

    private List<OrderItemDto> orderItems;
}
