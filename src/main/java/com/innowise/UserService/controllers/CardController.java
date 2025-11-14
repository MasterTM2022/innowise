package com.innowise.UserService.controllers;

import com.innowise.UserService.dto.CardDto;
import com.innowise.UserService.mapper.CardMapper;
import org.springframework.beans.factory.annotation.Autowired;
import com.innowise.UserService.entity.Card;
import com.innowise.UserService.service.CardService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/cards")
public class CardController {

    @Autowired
    private CardService cardService;

    @Autowired
    private CardMapper cardMapper;

    @PostMapping("/user/{userId}")
    public ResponseEntity<CardDto> createCard(@PathVariable Long userId, @RequestBody CardDto cardDto) {

        Card card = cardMapper.toEntity(cardDto);
        CardDto createdCardDto = cardService.createCard(userId, card);
        return ResponseEntity.status(HttpStatus.OK).body(createdCardDto);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CardDto> getCard(@PathVariable Long id) {
        CardDto cardDto = cardService.getCardById(id);
        return ResponseEntity.status(HttpStatus.OK).body(cardDto);
    }

    @GetMapping
    public ResponseEntity<Page<CardDto>> getAllCards(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(cardService.getAllCards(page, size));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCard(@PathVariable Long id) {
        cardService.deleteCard(id);
        return ResponseEntity.noContent().build();
    }
}
