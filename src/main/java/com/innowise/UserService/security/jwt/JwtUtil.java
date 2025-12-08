package com.innowise.UserService.security.jwt;

import com.innowise.UserService.entity.AppUser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
@RequiredArgsConstructor
public class JwtUtil {

    @Value("${app.jwt.secret}")
    private String secret;

    @Value("${JWT_ACCESS_EXPIRATION:900000}") // 15 минут по умолчанию
    private long accessExpiration;

    @Value("${JWT_REFRESH_EXPIRATION:86400000}") // 24 часа по умолчанию
    private long refreshExpiration;


    private byte[] decodedSecret;

    @PostConstruct
    public void init() {
        this.decodedSecret = Decoders.BASE64.decode(secret);
    }

    public String generateAccessToken(AppUser appUser) {
        return Jwts.builder()
                .setSubject(appUser.getUsername())
                .claim("role", appUser.getRole().name())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + accessExpiration))
                .signWith(SignatureAlgorithm.HS512, secret)
                .compact();
    }

    public String generateRefreshToken(AppUser appUser) {
        return Jwts.builder()
                .setSubject(appUser.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + refreshExpiration))
                .signWith(SignatureAlgorithm.HS512, secret)
                .compact();
    }

    public String extractUsername(String token) {
        return Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody().getSubject();
    }

    public boolean isTokenValid(String token, String username) {
        final String extractedUsername = extractUsername(token);
        return (extractedUsername.equals(username)) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody().getExpiration().before(new Date());
    }
}