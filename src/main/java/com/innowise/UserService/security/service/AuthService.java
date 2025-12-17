package com.innowise.UserService.security.service;

import com.innowise.UserService.dto.AuthResponse;
import com.innowise.UserService.dto.LoginRequest;
import com.innowise.UserService.dto.RefreshTokenRequest;
import com.innowise.UserService.dto.RegisterRequest;
import com.innowise.UserService.entity.AppUser;
import com.innowise.UserService.repository.AppUserRepository;
import com.innowise.UserService.security.jwt.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final AppUserRepository appUserRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );

        AppUser appUser = appUserRepository.findByUsername(request.username())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        String accessToken = jwtUtil.generateAccessToken(appUser);
        String refreshToken = jwtUtil.generateRefreshToken(appUser);

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
        appUserRepository.save(appUser);

        String accessToken = jwtUtil.generateAccessToken(appUser);
        String refreshToken = jwtUtil.generateRefreshToken(appUser);

        return new AuthResponse(accessToken, refreshToken);
    }

    public AuthResponse refreshToken(RefreshTokenRequest request) {
        String refreshToken = request.refreshToken();
        String username = jwtUtil.extractUsername(refreshToken);

        if (jwtUtil.isTokenValid(refreshToken, username)) {

            AppUser appUser = appUserRepository.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            String newAccessToken = jwtUtil.generateAccessToken(appUser);
            String newRefreshToken = jwtUtil.generateRefreshToken(appUser);

            return new AuthResponse(newAccessToken, newRefreshToken);
        } else {
            throw new IllegalArgumentException("Invalid refresh token");
        }
    }
}