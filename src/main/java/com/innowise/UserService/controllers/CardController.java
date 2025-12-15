package com.innowise.UserService.controllers;

import com.innowise.UserService.dto.CardDto;
import com.innowise.UserService.dto.CreateCardRequest;
import com.innowise.UserService.entity.AppUser;
import com.innowise.UserService.entity.User;
import com.innowise.UserService.mapper.CardMapper;
import com.innowise.UserService.security.service.AppUserDetails;
import com.innowise.UserService.security.utils.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import com.innowise.UserService.entity.Card;
import com.innowise.UserService.service.CardService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/cards")
@RequiredArgsConstructor
@Validated
public class CardController {

    private final CardService cardService;
    private final CardMapper cardMapper;
    private final SecurityUtils securityUtils;


    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CardDto> createCard(@Valid @RequestBody CreateCardRequest request) {
        Long currentUserId = securityUtils.getCurrentUserId();
        CardDto createdCard = cardService.createCard(currentUserId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdCard);
    }

    @GetMapping("/{id}")
    @PreAuthorize("@cardSecurityService.canViewCard(authentication, #id)")
    public ResponseEntity<CardDto> getCard(@PathVariable Long id) {
        CardDto cardDto = cardService.getCardById(id);
        return ResponseEntity.status(HttpStatus.OK).body(cardDto);
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<CardDto>> getCards(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        boolean isAdmin = securityUtils.isAdmin();

        Page<CardDto> cards;
        if (isAdmin) {
            cards = cardService.getAllCards(page, size); // все карты
        } else {
            Long userId = securityUtils.getCurrentUserId();
            cards = cardService.getCardsByUserId(userId, page, size); // только свои
        }

        return ResponseEntity.ok(cards);
    }

    @GetMapping("/expired")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<CardDto>> getExpiredCards(
            @RequestParam(required = false) LocalDate date,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        boolean isAdmin = securityUtils.isAdmin();

        LocalDate expDate = date != null ? date : LocalDate.now();

        Page<CardDto> cards;
        if (isAdmin) {
            cards = cardService.getExpiredCards(expDate, page, size);
        } else {
            Long userId = securityUtils.getCurrentUserId();
            cards = cardService.getUserExpiredCards(userId, expDate, page, size);
        }

        return ResponseEntity.ok(cards);
    }

    @GetMapping("/search")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Page<CardDto>> getCardsByLast4Digits(
            @RequestParam String searchFourDigits,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        if (!searchFourDigits.matches("\\d{4}")) {
            throw new IllegalArgumentException("Parameter 'searchFourDigits' must be exactly 4 digits (e.g. '1234')");
        }

        Page<CardDto> cards = cardService.getCardsBy4Digits(searchFourDigits, page, size);
        return ResponseEntity.ok(cards);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@cardSecurityService.canDeleteCard(authentication, #id)")
    public ResponseEntity<Void> deleteCard(@PathVariable Long id) {
        cardService.deleteCard(id);
        return ResponseEntity.noContent().build();
    }

}