package com.innowise.auth.security.service;

import com.innowise.auth.client.UserServiceClient;
import com.innowise.auth.dto.*;
import com.innowise.auth.entity.AppUser;
import com.innowise.auth.exeption.ServiceUnavailableException;
import com.innowise.auth.repository.AppUserRepository;
import com.innowise.auth.security.jwt.JwtUtil;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final AppUserRepository appUserRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final UserServiceClient userServiceClient;
    private final CircuitBreakerFactory circuitBreakerFactory;
    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );

        AppUser appUser = appUserRepository.findByUsername(request.username())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        Long userId = appUser.getUserId();
        if (userId == null) {
            throw new IllegalStateException("User profile not created for " + request.username());
        }
        String accessToken = jwtUtil.generateAccessToken(appUser, userId);
        String refreshToken = jwtUtil.generateRefreshToken(appUser, userId);

        return new AuthResponse(accessToken, refreshToken);
    }

    public AuthResponse register(RegisterRequest request) {
        if (appUserRepository.findByUsername(request.username()).isPresent()) {
            throw new IllegalArgumentException("Username already exists");
        }

        AppUser appUser = new AppUser();
        appUser.setUsername(request.username());
        appUser.setPassword(passwordEncoder.encode(request.password()));
        appUser.setRole(AppUser.Role.USER);

        AppUser savedAppUser = appUserRepository.save(appUser);

        CreateUserRequest profileRequest = new CreateUserRequest(
                request.name(),
                request.surname(),
                request.birthDate(),
                request.email()
        );

        UserDto userDto = circuitBreakerFactory.create("userService")
                .run(() -> {
                    try {
                        return userServiceClient.createUserProfile(profileRequest);
                    } catch (FeignException feignEx) {

                        if (feignEx.status() >= 400 && feignEx.status() < 500) {
                            throw feignEx;
                        }

                        throw new RuntimeException("UserService technical error", feignEx);
                    }
                }, throwable -> {

                    Throwable cause = throwable;
                    if (throwable instanceof java.util.concurrent.ExecutionException) {
                        cause = throwable.getCause();
                    }

                    if (cause instanceof FeignException feignEx && feignEx.status() >= 400 && feignEx.status() < 500) {
                        throw feignEx;
                    }

                    appUserRepository.delete(savedAppUser);
                    throw new ServiceUnavailableException("User profile service is temporarily unavailable");
                });

        Long userId = userDto.getId();

        savedAppUser.setUserId(userId);
        appUserRepository.save(savedAppUser);

        String accessToken = jwtUtil.generateAccessToken(appUser, userId);
        String refreshToken = jwtUtil.generateRefreshToken(appUser, userId);

        return new AuthResponse(accessToken, refreshToken);
    }

    private UserDto fallbackCreateUserProfile(CreateUserRequest request, AppUser appUser, Throwable throwable) {
        log.error("UserService is down during registration — rolling back auth user", throwable);
        appUserRepository.delete(appUser);
        throw new ServiceUnavailableException("User profile service is temporarily unavailable");
    }

    public AuthResponse refreshToken(RefreshTokenRequest request) {
        String refreshToken = request.refreshToken();
        String username = jwtUtil.extractUsername(refreshToken);

        if (jwtUtil.isTokenValid(refreshToken, username)) {

            AppUser appUser = appUserRepository.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            Long userId = appUser.getUserId();
            String newAccessToken = jwtUtil.generateAccessToken(appUser, userId);
            String newRefreshToken = jwtUtil.generateRefreshToken(appUser, userId);

            return new AuthResponse(newAccessToken, newRefreshToken);
        } else {
            throw new BadCredentialsException("Invalid refresh token");
        }
    }
}