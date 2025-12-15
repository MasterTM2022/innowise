package com.innowise.UserService.client;

import com.innowise.UserService.dto.UserDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class UserServiceClient {

    private final WebClient webClient;

    public UserServiceClient(@Value("${user.service.url:http://localhost:8080}") String userServiceUrl) {
        this.webClient = WebClient.builder()
                .baseUrl(userServiceUrl)
                .build();
    }

    public UserDto getUserById(Long id) {
        return webClient
                .get()
                .uri("/api/v1/users/{id}", id)
                .retrieve()
                .bodyToMono(UserDto.class)
                .block();
    }
}