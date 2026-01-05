package com.innowise.auth.security.controllers;

import com.innowise.auth.client.UserServiceClient;
import com.innowise.auth.dto.*;
import com.innowise.auth.entity.AppUser;
import com.innowise.auth.repository.AppUserRepository;
import com.innowise.auth.security.jwt.JwtUtil;
import com.innowise.auth.security.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtUtil jwtUtil;
    private final AppUserRepository appUserRepository;
    private final UserServiceClient userServiceClient;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.ok(new AuthResponse(response.accessToken(), response.refreshToken()));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }


    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@RequestBody RefreshTokenRequest request) {
        AuthResponse response = authService.refreshToken(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/validate")
    public ResponseEntity<ValidateTokenResponse> validateToken(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ValidateTokenResponse(false, null));
        }

        String token = authHeader.substring(7);
        String username = jwtUtil.extractUsername(token);

        boolean valid = jwtUtil.isTokenValid(token, username);
        return ResponseEntity.ok(new ValidateTokenResponse(valid, valid ? username : null));
    }

    record ValidateTokenResponse(boolean valid, String username) {
    }
}