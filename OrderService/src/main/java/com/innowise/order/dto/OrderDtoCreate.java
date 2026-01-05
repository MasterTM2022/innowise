package com.innowise.order.dto;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.validation.constraints.NotNull;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
public class OrderDtoCreate implements UserEnrichable {

    private Long id;

    @NotNull(message = "User ID is required")
    private Long userId;

    private String userName;
    private String userSurname;

    private List<OrderItemDto> orderItems;

    @Override
    public void setUserName(String name) {
    }

    @Override
    public void setUserSurname(String surname) {
    }
}