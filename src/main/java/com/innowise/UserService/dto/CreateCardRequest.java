package com.innowise.UserService.dto;

import java.time.LocalDate;

public record CreateCardRequest(
        String cardNumber,
        LocalDate expiryDate,
        String cardholderName
) {}
