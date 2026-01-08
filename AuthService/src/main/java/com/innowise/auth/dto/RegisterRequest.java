package com.innowise.auth.dto;

import java.time.LocalDate;

public record RegisterRequest(String username,
                              String password,
                              String name,
                              String surname,
                              LocalDate birthDate,
                              String email,
                              Long userId) {
}
