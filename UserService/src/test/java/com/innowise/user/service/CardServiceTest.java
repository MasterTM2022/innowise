package com.innowise.user.service;

import com.innowise.user.dto.CardDto;
import com.innowise.user.entity.Card;
import com.innowise.user.entity.User;
import com.innowise.user.mapper.CardMapper;
import com.innowise.user.mapper.UserMapper;
import com.innowise.user.repository.CardRepository;
import com.innowise.user.repository.UserRepository;
import com.innowise.user.exception.CardNotFoundException;
import com.innowise.user.exception.UserNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CardServiceTest {

    @Mock
    private CardRepository cardRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CardMapper cardMapper;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private CardService cardService;

    @Test
    void getCardById_ShouldReturnCardDto_WhenCardExists() {
        // Given
        Long cardId = 100L;
        Long userId = 1L;
        String number = "1234567890123456";
        String holder = "IVAN IVANOV";
        LocalDate expirationDate = LocalDate.of(2028, 12, 31);

        User user = new User();
        user.setId(userId);

        Card card = new Card();
        card.setId(cardId);
        card.setNumber(number);
        card.setHolder(holder);
        card.setExpirationDate(expirationDate);
        card.setUser(user);

        CardDto cardDto = new CardDto(cardId, userId, number, holder, expirationDate);

        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));
        when(cardMapper.toDto(card)).thenReturn(cardDto);

        // When
        CardDto result = cardService.getCardById(cardId);

        // Then
        assertThat(result).isEqualToComparingFieldByField(cardDto);
        verify(cardRepository).findById(cardId);
        verify(cardMapper).toDto(card);
    }

    @Test
    void getCardById_ShouldThrowException_WhenCardDoesNotExist() {
        // Given
        Long cardId = 999L;
        when(cardRepository.findById(cardId)).thenReturn(Optional.empty());

        // When / Then
        CardNotFoundException ex = assertThrows(CardNotFoundException.class, () ->
                cardService.getCardById(cardId)
        );
        assertThat(ex.getMessage()).contains("Card not found");
        verify(cardRepository).findById(cardId);
    }

    @Test
    void createCard_ShouldReturnCardDto_WhenUserExists() {
        // Given
        Long userId = 1L;
        CardDto requestDto = new CardDto();
        requestDto.setNumber("1234567890123456");
        requestDto.setHolder("IVAN IVANOV");
        requestDto.setExpirationDate(LocalDate.of(2029, 12, 31));

        User user = new User();
        user.setId(userId);
        user.setName("Ivan");
        user.setSurname("Ivanov");
        user.setEmail("ivan@example.com");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        Card cardEntity = new Card();
        cardEntity.setNumber("1234567890123456");
        cardEntity.setHolder("IVAN IVANOV");
        cardEntity.setExpirationDate(LocalDate.of(2029, 12, 31));
        when(cardMapper.toEntity(requestDto)).thenReturn(cardEntity);

        User savedUser = new User();
        savedUser.setId(userId);
        savedUser.setName("Ivan");
        savedUser.setSurname("Ivanov");
        savedUser.setEmail("ivan@example.com");

        Card savedCard = new Card();
        savedCard.setId(1L);
        savedCard.setNumber("1234567890123456");
        savedCard.setHolder("IVAN IVANOV");
        savedCard.setExpirationDate(LocalDate.of(2029, 12, 31));
        savedCard.setUser(savedUser);

        savedUser.setCards(List.of(savedCard));

        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        CardDto expectedDto = new CardDto(1L, userId, "1234567890123456", "IVAN IVANOV", LocalDate.of(2029, 12, 31));
        when(cardMapper.toDto(any(Card.class))).thenReturn(expectedDto);

        // When
        CardDto result = cardService.createCard(userId, requestDto);

        // Then
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getNumber()).isEqualTo("1234567890123456");
        verify(userRepository).findById(userId);
        verify(userRepository).save(any(User.class));
        verify(cardMapper).toEntity(requestDto);
        verify(cardMapper).toDto(any(Card.class));
    }

    @Test
    void createCard_ShouldThrowException_WhenUserDoesNotExist() {
        // Given
        Long userId = 999L;
        CardDto requestDto = new CardDto();

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When / Then
        UserNotFoundException ex = assertThrows(UserNotFoundException.class, () ->
                cardService.createCard(userId, requestDto)
        );
        assertThat(ex.getMessage()).contains(String.format("User with id %d not found",  userId));
        verify(userRepository).findById(userId);
    }

    @Test
    void deleteCard_ShouldDeleteCard_WhenCardExists() {
        // Given
        Long cardId = 1L;
        when(cardRepository.existsById(cardId)).thenReturn(true);

        // When
        cardService.deleteCard(cardId);

        // Then
        verify(cardRepository).deleteById(cardId);
    }

    @Test
    void deleteCard_ShouldThrowException_WhenCardDoesNotExist() {
        // Given
        Long cardId = 999L;
        when(cardRepository.existsById(cardId)).thenReturn(false);

        // When / Then
        CardNotFoundException ex = assertThrows(CardNotFoundException.class, () ->
                cardService.deleteCard(cardId)
        );
        assertThat(ex.getMessage()).contains("Card not found");
        verify(cardRepository).existsById(cardId);
    }

    @Test
    void getAllCards_ShouldReturnPageOfCardDtos_WhenCalled() {
        // Given
        int page = 0, size = 10;
        Pageable pageable = PageRequest.of(page, size);

        User user = new User();
        user.setId(1L);

        Card card1 = new Card();
        card1.setId(100L);
        card1.setNumber("1234");
        card1.setHolder("IVAN IVANOV");
        card1.setExpirationDate(LocalDate.of(2029, 11, 30));
        card1.setUser(user);

        Card card2 = new Card();
        card2.setId(200L);
        card2.setNumber("5678");
        card2.setHolder("PETR PETROV");
        card2.setExpirationDate(LocalDate.of(2030, 12, 31));
        card2.setUser(user);

        Page<Card> cardPage = new PageImpl<>(Arrays.asList(card1, card2));

        CardDto dto1 = new CardDto(100L, 1L, "1234", "IVAN IVANOV", LocalDate.of(2029, 11, 30));
        CardDto dto2 = new CardDto(200L, 1L, "5678", "PETR PETROV", LocalDate.of(2030, 12, 31));

        when(cardRepository.findAll(pageable)).thenReturn(cardPage);
        when(cardMapper.toDto(card1)).thenReturn(dto1);
        when(cardMapper.toDto(card2)).thenReturn(dto2);

        // When
        Page<CardDto> result = cardService.getAllCards(page, size);

        // Then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(2);
        verify(cardRepository).findAll(pageable);
    }

    @Test
    void getCardsBy4Digits_ShouldReturnCards_WhenDigitsMatch() {
        // Given
        String fourDigits = "3456";
        Pageable pageable = PageRequest.of(0, 10);

        Card card = new Card();
        card.setId(100L);
        card.setNumber("1234567890123456");

        Page<Card> cardsPage = new PageImpl<>(List.of(card));

        CardDto cardDto = new CardDto(100L, 1L, "1234567890123456", "HOLDER", LocalDate.now());
        when(cardRepository.findBy4Digits(fourDigits, pageable)).thenReturn(cardsPage);
        when(cardMapper.toDto(card)).thenReturn(cardDto);

        // When
        Page<CardDto> result = cardService.getCardsBy4Digits(fourDigits, 0, 10);

        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getNumber()).endsWith("3456");
        verify(cardRepository).findBy4Digits(fourDigits, pageable);
    }

    @Test
    void getExpiredCards_ShouldReturnExpiredCards_WhenCalled() {
        // Given
        LocalDate now = LocalDate.now();
        Pageable pageable = PageRequest.of(0, 10);

        Card card = new Card();
        card.setId(100L);
        card.setNumber("1111222233334444");
        card.setExpirationDate(LocalDate.of(2024, 1, 1));

        Page<Card> cardsPage = new PageImpl<>(List.of(card));
        CardDto cardDto = new CardDto(100L, 1L, "1111222233334444", "HOLDER", LocalDate.of(2024, 1, 1));

        when(cardRepository.findExpiredCards(now, pageable)).thenReturn(cardsPage);
        when(cardMapper.toDto(card)).thenReturn(cardDto);

        // When
        Page<CardDto> result = cardService.getExpiredCards(now, 0, 10);

        // Then
        assertThat(result.getContent()).hasSize(1);
        verify(cardRepository).findExpiredCards(now, pageable);
    }

    @Test
    void isOwner_ShouldReturnTrue_WhenUserOwnsCard() {
        // Given
        Long cardId = 100L;
        Long userId = 1L;

        User user = new User();
        user.setId(userId);

        Card card = new Card();
        card.setId(cardId);
        card.setUser(user);

        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));

        // When
        boolean result = cardService.isOwner(cardId, userId);

        // Then
        assertThat(result).isTrue();
        verify(cardRepository).findById(cardId);
    }

    @Test
    void isOwner_ShouldReturnFalse_WhenUserDoesNotOwnCard() {
        // Given
        Long cardId = 100L;
        Long userId = 1L;
        Long otherUserId = 2L;

        User user = new User();
        user.setId(otherUserId);

        Card card = new Card();
        card.setId(cardId);
        card.setUser(user);

        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));

        // When
        boolean result = cardService.isOwner(cardId, userId);

        // Then
        assertThat(result).isFalse();
        verify(cardRepository).findById(cardId);
    }

    @Test
    void isOwner_ShouldReturnFalse_WhenCardNotFound() {
        // Given
        Long cardId = 100L;
        Long userId = 1L;
        when(cardRepository.findById(cardId)).thenReturn(Optional.empty());

        // When
        boolean result = cardService.isOwner(cardId, userId);

        // Then
        assertThat(result).isFalse();
        verify(cardRepository).findById(cardId);
    }

    @Test
    void getCardsByUserId_ShouldReturnPagedCards_WhenUserExists() {
        // Given
        Long userId = 1L;
        Pageable pageable = PageRequest.of(0, 10);

        Card card1 = new Card();
        card1.setId(100L);
        card1.setNumber("1111222233334444");

        Card card2 = new Card();
        card2.setId(101L);
        card2.setNumber("5555666677778888");

        Page<Card> cardsPage = new PageImpl<>(List.of(card1, card2));

        CardDto dto1 = new CardDto(100L, userId, "1111222233334444", "HOLDER", LocalDate.now());
        CardDto dto2 = new CardDto(101L, userId, "5555666677778888", "HOLDER", LocalDate.now());

        when(userRepository.existsById(userId)).thenReturn(true);
        when(cardRepository.findByUserId(userId, pageable)).thenReturn(cardsPage);
        when(cardMapper.toDto(card1)).thenReturn(dto1);
        when(cardMapper.toDto(card2)).thenReturn(dto2);

        // When
        Page<CardDto> result = cardService.getCardsByUserId(userId, 0, 10);

        // Then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent()).extracting(CardDto::getUserId)
                .containsOnly(userId);
        verify(userRepository).existsById(userId);
    }

    @Test
    void getCardsByUserId_ShouldThrowUserNotFound_WhenUserDoesNotExist() {
        // Given
        Long userId = 999L;
        when(userRepository.existsById(userId)).thenReturn(false);

        // When / Then
        UserNotFoundException ex = assertThrows(UserNotFoundException.class, () ->
                cardService.getCardsByUserId(userId, 0, 10)
        );
        assertThat(ex.getMessage()).contains("User with id 999 not found");
        verify(userRepository).existsById(userId);
        verify(cardRepository, never()).findByUserId(any(), any());
    }
}