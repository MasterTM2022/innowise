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

public class OrderDtoCreate {
    private Long id;

    @NotNull(message = "User ID is required")
    private Long userId;

    private OrderStatus status;

    private LocalDateTime creationDate;

    private List<OrderItemDto> orderItems;

    private UserDto userInfo;
}
