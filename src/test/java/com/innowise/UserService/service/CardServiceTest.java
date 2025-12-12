package com.innowise.UserService.service;

import com.innowise.UserService.dto.CardDto;
import com.innowise.UserService.entity.Card;
import com.innowise.UserService.entity.User;
import com.innowise.UserService.mapper.CardMapper;
import com.innowise.UserService.mapper.UserMapper;
import com.innowise.UserService.repository.CardRepository;
import com.innowise.UserService.repository.UserRepository;
import com.innowise.UserService.service.exception.CardNotFoundException;
import com.innowise.UserService.service.exception.UserNotFoundException;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

    // Пример: тест метода getCardById
    @Test
    void getCardById_ShouldReturnCardDto_WhenCardExists() {
        // Given
        Long id = 1L;
        Long idCard = 100L;
        String number = "1234567890123456";
        String holder = "IVAN IVANOV";
        LocalDate expirationDate = LocalDate.of(2028, 12, 31);

        User user = new User();
        user.setId(id);

        Card card = new Card();
        card.setId(idCard);
        card.setNumber(number);
        card.setHolder(holder);
        card.setExpirationDate(expirationDate);
        card.setUser(user);

        CardDto cardDto = new CardDto(idCard, id, number, holder, expirationDate);

        when(cardRepository.findById(id)).thenReturn(Optional.of(card));
        when(cardMapper.toDto(card)).thenReturn(cardDto);

        // When
        CardDto result = cardService.getCardById(id);

        // Then
        assertEquals(cardDto, result);
        verify(cardRepository).findById(id);
        verify(cardMapper).toDto(card);
    }

    @Test
    void getCardById_ShouldThrowException_WhenCardDoesNotExist() {
        // Given
        Long id = 999L;
        when(cardRepository.findById(id)).thenReturn(Optional.empty());

        // When / Then
        assertThrows(CardNotFoundException.class, () -> cardService.getCardById(id));
        verify(cardRepository).findById(id);
    }

    @Test
    void createCard_ShouldReturnCardDto_WhenUserExists() {
        // Given
        Long userId = 1L;

        User user = new User();
        user.setId(userId);
        user.setName("Ivan");
        user.setSurname("Ivanov");
        user.setEmail("ivan@example.com");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        Card card = new Card();
        card.setNumber("1234567890123456");
        card.setHolder("IVAN IVANOV");
        card.setExpirationDate(LocalDate.of(2029, 12, 31));

        // Мокаем: при сохранении юзера — возвращаем юзера, **в котором уже есть карта**
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

        savedUser.addCard(savedCard); // ← добавляем карту в сохранённого юзера

        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(cardMapper.toDto(savedCard)).thenReturn(new CardDto(1L, savedUser.getId(), "1234567890123456", "IVAN IVANOV", LocalDate.of(2029, 12, 31)));

        // When
        CardDto result = cardService.createCard(userId, card);

        // Then
        assertNotNull(result.getId());
        assertEquals("1234567890123456", result.getNumber());

        verify(userRepository).save(user);
        verify(cardMapper).toDto(savedCard);
    }

    @Test
    void createCard_ShouldThrowException_WhenUserDoesNotExist() {
        // Given
        Long userId = 999L;
        CardDto cardDto = new CardDto(null, null, "1234", "IVAN PETROV", LocalDate.of(2029, 11, 30));

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When / Then
        assertThrows(UserNotFoundException.class, () -> cardService.createCard(userId, cardMapper.toEntity(cardDto)));
        verify(userRepository).findById(userId);
    }

    @Test
    void deleteCard_ShouldDeleteCard_WhenCardExists() {
        // Given
        Long id = 1L;

        when(cardRepository.existsById(id)).thenReturn(true);

        // When
        cardService.deleteCard(id);

        // Then
        verify(cardRepository).existsById(id);
        verify(cardRepository).deleteById(id);
    }

    @Test
    void deleteCard_ShouldThrowException_WhenCardDoesNotExist() {
        // Given
        Long id = 999L;
        when(cardRepository.existsById(id)).thenReturn(false);

        // When / Then
        assertThrows(CardNotFoundException.class, () -> cardService.deleteCard(id));
        verify(cardRepository).existsById(id);
    }

    @Test
    void getAllCards_ShouldReturnPageOfCardDtos_WhenCalled() {
        // Given
        int page = 0;
        int size = 10;

        // Подготовим объекты
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

        List<Card> cardList = Arrays.asList(card1, card2);
        Page<Card> cardPage = new PageImpl<>(cardList);

        CardDto cardDto1 = new CardDto(null, user.getId(), "1234", "IVAN IVANOV", LocalDate.of(2029, 11, 30));
        CardDto cardDto2 = new CardDto(null, user.getId(), "5678", "PETR PETROV", LocalDate.of(2030, 12, 31));

        List<CardDto> cardDtoList = Arrays.asList(cardDto1, cardDto2);
        Page<CardDto> expectedPage = new PageImpl<>(cardDtoList);

        // Мокаем поведение
        Pageable pageable = PageRequest.of(page, size);
        when(cardRepository.findAll(pageable)).thenReturn(cardPage);
        when(cardMapper.toDto(card1)).thenReturn(cardDto1);
        when(cardMapper.toDto(card2)).thenReturn(cardDto2);

        // When
        Page<CardDto> result = cardService.getAllCards(page, size);

        // Then
        assertEquals(expectedPage.getContent(), result.getContent());
        assertEquals(expectedPage.getTotalElements(), result.getTotalElements());
        assertEquals(expectedPage.getTotalPages(), result.getTotalPages());
        assertEquals(expectedPage.getNumber(), result.getNumber());
        assertEquals(expectedPage.getSize(), result.getSize());

        verify(cardRepository).findAll(pageable);
        verify(cardMapper).toDto(card1);
        verify(cardMapper).toDto(card2);
    }
}