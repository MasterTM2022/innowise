package com.innowise.order.dto;

import com.innowise.order.entity.OrderStatus;
import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode

public class OrderDtoUpdate {
    @NotNull
    private OrderStatus status;
}
