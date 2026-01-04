package com.innowise.user.dto;

import java.time.LocalDate;

public record CreateCardRequest(
        String cardNumber,
        LocalDate expiryDate,
        String cardholderName
) {}
