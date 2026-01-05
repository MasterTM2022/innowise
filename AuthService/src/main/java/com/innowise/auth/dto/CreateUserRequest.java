package com.innowise.auth.dto;

import java.time.LocalDate;

public record CreateUserRequest(
        String name,
        String surname,
        LocalDate birthDate,
        String email) {
}