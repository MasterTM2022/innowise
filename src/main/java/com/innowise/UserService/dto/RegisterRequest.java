package com.innowise.UserService.dto;

public record RegisterRequest(String username, String password, Long userId) {
}
