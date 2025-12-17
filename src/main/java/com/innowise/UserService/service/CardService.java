package com.innowise.UserService.service;

import com.innowise.UserService.dto.CardDto;
import com.innowise.UserService.dto.CreateCardRequest;
import com.innowise.UserService.entity.Card;
import com.innowise.UserService.entity.User;
import com.innowise.UserService.mapper.CardMapper;
import com.innowise.UserService.repository.CardRepository;
import com.innowise.UserService.repository.UserRepository;
import com.innowise.UserService.service.exception.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@Transactional
@RequiredArgsConstructor
public class CardService {

    private final CardRepository cardRepository;
    private final UserRepository userRepository;
    private final CardMapper cardMapper;

    public boolean isOwner(Long cardId, Long userId) {
        return cardRepository.findById(cardId)
                .map(card -> card.getUser().getId().equals(userId))
                .orElse(false);
    }

    // Create
    @Transactional
    public CardDto createCard(Long userId, CreateCardRequest request){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        Card card = new Card();
        card.setNumber(request.cardNumber());
        card.setExpirationDate(request.expiryDate());
        card.setHolder(request.cardholderName());
        card.setUser(user);

        user.addCard(card);
        User savedUser = userRepository.save(user);

        Card savedCard = savedUser.getCards().stream()
                .filter(c -> c.getNumber().equals(card.getNumber()))
                .findFirst()
                .orElseThrow(() -> new CardNotFoundException("Card not found after saving"));

        return cardMapper.toDto(savedCard);
    }

    // Get by ID
    @Cacheable(value = "cards", key = "#id")
    public CardDto getCardById(Long id) {
        Card card = cardRepository.findById(id)
                .orElseThrow(() -> new CardNotFoundException("Card not found"));
        return cardMapper.toDto(card);
    }

    // Get all with pagination
    @Transactional
    public Page<CardDto> getAllCards(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Card> cardsPage = cardRepository.findAll(pageable);
        return cardsPage.map(cardMapper::toDto);
    }

    // Get all with pagination
    @Transactional
    public Page<CardDto> getCardsByUserId(Long userId, int page, int size) {
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException("User with id " + userId + " not found");
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<Card> cardsPage = cardRepository.findByUserId(userId, pageable);
        return cardsPage.map(cardMapper::toDto);
    }

    // Get all expired cards
    @Transactional
    public Page<CardDto> getExpiredCards(LocalDate expirationDate, int page, int size) {
        if (expirationDate == null) {
            expirationDate = LocalDate.now();
        }
        Pageable pageable = PageRequest.of(page, size);
        Page<Card> cardsPage = cardRepository.findExpiredCards(expirationDate, pageable);
        return cardsPage.map(cardMapper::toDto);
    }

    // Get all user expired cards
    @Transactional
    public Page<CardDto> getUserExpiredCards(Long userId, LocalDate expirationDate, int page, int size) {
        if (expirationDate == null) {
            expirationDate = LocalDate.now();
        }
        Pageable pageable = PageRequest.of(page, size);
        Page<Card> cardsPage = cardRepository.findExpiredCardsByUserId(userId, expirationDate, pageable);
        return cardsPage.map(cardMapper::toDto);
    }

    // Get Card by 4 digits
    @Transactional
    public Page<CardDto> getCardsBy4Digits(String fourDigits, int page, int size) {
        if (fourDigits == null ||
                fourDigits.isEmpty() ||
                !fourDigits.matches("\\d{4}")
        ) {
            throw new IllegalArgumentException("Any 4 digits must be exactly 4 numeric characters");
        }
        Pageable pageable = PageRequest.of(page, size);
        Page<Card> cardsPage = cardRepository.findByLast4Digits(fourDigits, pageable);
        return cardsPage.map(cardMapper::toDto);
    }

    // Delete
    @Transactional
    @CacheEvict(value = "cards", key = "#id")
    public void deleteCard(Long id) {
        if (!cardRepository.existsById(id)) {
            throw new CardNotFoundException("Card not found");
        }
        cardRepository.deleteById(id);
    }
}