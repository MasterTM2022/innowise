package com.innowise.order.dto;

import com.innowise.order.entity.OrderStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class OrderResponseDto implements UserEnrichable {
    private Long id;
    private Long userId;
    private String userName;
    private String userSurname;
    private List<OrderItemDto> orderItems;
    private OrderStatus status;
    private LocalDateTime creationDate;
}
