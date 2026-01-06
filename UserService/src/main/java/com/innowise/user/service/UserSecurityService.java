package com.innowise.user.service;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class UserSecurityService {
    public boolean canManageUser(Authentication authentication, Long requestedUserId) {

        System.out.println("🔍 Authentication: " + authentication);
        System.out.println("🔍 Principal type: " + authentication.getPrincipal().getClass());
        System.out.println("🔍 Principal: " + authentication.getPrincipal());

        // Правильный способ: principal — это Jwt
        if (!(authentication.getPrincipal() instanceof Jwt jwt)) {
            System.out.println("❌ Principal is NOT Jwt!");
            return false;
        }

        // Извлекаем userId из claims
        Long currentUserId = jwt.getClaim("userId");
        System.out.println("🔍 Current userId from JWT: " + currentUserId);

        if (currentUserId == null) {
            return false;
        }

        // Проверяем роль (если нужно)
        String role = jwt.getClaim("role");
        boolean isAdmin = "ADMIN".equals(role);

        // Разрешаем, если:
        // - это его собственный профиль, ИЛИ
        // - у него роль ADMIN
        return currentUserId.equals(requestedUserId) || isAdmin;
    }
}