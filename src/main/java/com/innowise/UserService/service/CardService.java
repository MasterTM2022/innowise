package com.innowise.UserService.service;

import com.innowise.UserService.dto.CardDto;
import com.innowise.UserService.entity.Card;
import com.innowise.UserService.entity.User;
import com.innowise.UserService.mapper.CardMapper;
import com.innowise.UserService.repository.CardRepository;
import com.innowise.UserService.repository.UserRepository;
import com.innowise.UserService.service.exception.*;
import jakarta.transaction.Transactional;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.Optional;

@Service
@Transactional
public class CardService {

    @Autowired
    private CardRepository cardRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CardMapper cardMapper;

    // Create
    @Transactional
    public CardDto createCard(Long userId, Card card) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

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
    public Page<CardDto> getAllCards(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Card> cardsPage = cardRepository.findAll(pageable);
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