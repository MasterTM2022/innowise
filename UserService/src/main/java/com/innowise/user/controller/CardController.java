package com.innowise.user.controller;

import com.innowise.user.dto.CardDto;
import lombok.RequiredArgsConstructor;
import com.innowise.user.service.CardService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/cards/")
@RequiredArgsConstructor
@Validated
public class CardController {

    private final CardService cardService;

    @PostMapping()
    public ResponseEntity<CardDto> createCard(
            @RequestBody CardDto cardDto,
            Authentication authentication) {

        JwtClaims jwtClaims = extractClaims(authentication);

        if (!"ADMIN".equals(jwtClaims.role) && !jwtClaims.userId.equals(cardDto.getUserId())) {
            throw new AccessDeniedException("Cannot create card for another user");
        }

        CardDto createdCard = cardService.createCard(cardDto.getUserId(), cardDto);
        return ResponseEntity.ok(createdCard);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CardDto> getCard(
            @PathVariable Long id,
            Authentication authentication) {

        JwtClaims jwtClaims = extractClaims(authentication);

        CardDto card = cardService.getCardById(id);
        if (!"ADMIN".equals(jwtClaims.role) && !jwtClaims.userId.equals(card.getUserId())) {
            throw new AccessDeniedException("Access denied");
        }

        return ResponseEntity.ok(card);
    }

    @GetMapping()
    public ResponseEntity<Page<CardDto>> getAllCards(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {

        JwtClaims jwtClaims = extractClaims(authentication);

        if ("ADMIN".equals(jwtClaims.role)) {
            Page<CardDto> cards = cardService.getAllCards(page, size);
            return ResponseEntity.ok(cards);
        } else {
            Page<CardDto> cards = cardService.getCardsByUserId(jwtClaims.userId, page, size);
            return ResponseEntity.ok(cards);
        }
    }

    @GetMapping("/expired")
    public ResponseEntity<Page<CardDto>> getExpiredCards(
            @RequestParam(required = false) LocalDate date,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {

        JwtClaims jwtClaims = extractClaims(authentication);

        if ("ADMIN".equals(jwtClaims.role)) {
            Page<CardDto> cards = cardService.getExpiredCards(date, page, size);
            return ResponseEntity.ok(cards);
        } else {
            Page<CardDto> cards = cardService.getUserExpiredCards(jwtClaims.userId, date, page, size);
            return ResponseEntity.ok(cards);
        }
    }

    @GetMapping("/cards/search")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<CardDto>> getCardsByLast4Digits(
            @RequestParam(required = false) String fourDigits,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<CardDto> cards = cardService.getCardsBy4Digits(fourDigits, page, size);
        return ResponseEntity.ok(cards);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@cardService.isOwner(#id, authentication.principal.id) or hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCard(@PathVariable Long id) {
        cardService.deleteCard(id);
        return ResponseEntity.noContent().build();
    }

    private JwtClaims extractClaims(Authentication authentication) {
        if (!(authentication.getPrincipal() instanceof Jwt jwt)) {
            throw new IllegalStateException("Invalid authentication principal");
        }

        String role = jwt.getClaim("role");
        Long userId = jwt.getClaim("userId");

        if (role == null || userId == null) {
            throw new IllegalStateException("JWT missing required claims: role or userId");
        }

        return new JwtClaims(role, userId);
    }

    private record JwtClaims(String role, Long userId) {}

}