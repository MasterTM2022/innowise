package com.innowise.UserService.controllers;

import com.innowise.UserService.dto.CardDto;
import com.innowise.UserService.mapper.CardMapper;
import lombok.RequiredArgsConstructor;
import com.innowise.UserService.entity.Card;
import com.innowise.UserService.service.CardService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/users/")
@RequiredArgsConstructor
@Validated
public class CardController {

    private final CardService cardService;
    private final CardMapper cardMapper;

    @PostMapping("/{userId}/card")
    @PreAuthorize("#userId == authentication.principal.appUser.id or hasRole('ADMIN')")
    public ResponseEntity<CardDto> createCard(@PathVariable Long userId, @RequestBody CardDto cardDto) {
        Card card = cardMapper.toEntity(cardDto);
        CardDto createdCardDto = cardService.createCard(userId, card);
        return ResponseEntity.status(HttpStatus.OK).body(createdCardDto);
    }

    @GetMapping("/card")
    @PreAuthorize("@cardService.isOwner(#id, authentication.principal.id) or hasRole('ADMIN')")
    public ResponseEntity<CardDto> getCard(@RequestParam Long id) {
        CardDto cardDto = cardService.getCardById(id);
        return ResponseEntity.status(HttpStatus.OK).body(cardDto);
    }

    @GetMapping("/cards")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<CardDto>> getAllCards(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(cardService.getAllCards(page, size));
    }

    @GetMapping("/{userId}/cards")
    @PreAuthorize("#userId == authentication.principal.appUser.id or hasRole('ADMIN')")
    public ResponseEntity<Page<CardDto>> getUserAllCards(@PathVariable Long userId,
                                                         @RequestParam(defaultValue = "0") int page,
                                                         @RequestParam(defaultValue = "10") int size) {
        Page<CardDto> cards = cardService.getCardsByUserId(userId, page, size);
        return ResponseEntity.status(HttpStatus.OK).body(cards);
    }

    @GetMapping("/cards/expired")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<CardDto>> getExpiredCards(
            @RequestParam(required = false) LocalDate date,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(cardService.getExpiredCards(date, page, size));
    }

    @GetMapping("/{userId}/cards/expired")
    @PreAuthorize("#userId == authentication.principal.appUser.id or hasRole('ADMIN') or hasRole('ADMIN')")
    public ResponseEntity<Page<CardDto>> getUserExpiredCards(
            @PathVariable Long userId,
            @RequestParam(required = false) LocalDate date,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(cardService.getUserExpiredCards(userId, date, page, size));
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

    @DeleteMapping("/deleteCard/{id}")
    @PreAuthorize("@cardService.isOwner(#id, authentication.principal.id) or hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCard(@PathVariable Long id) {
        cardService.deleteCard(id);
        return ResponseEntity.noContent().build();
    }
}