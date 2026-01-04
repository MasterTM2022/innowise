package com.innowise.user.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtLoggingFilter extends OncePerRequestFilter {
    private static final Logger log = LoggerFactory.getLogger(JwtLoggingFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            log.info("🔐 Received JWT: {} (first 50 chars: {}...)", token, token.substring(0, Math.min(50, token.length())));
        } else {
            log.warn("⚠️ No Authorization header found");
        }

        filterChain.doFilter(request, response);
    }
}
