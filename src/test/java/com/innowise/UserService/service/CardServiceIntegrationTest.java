package com.innowise.UserService.service;

import com.innowise.UserService.dto.CardDto;
import com.innowise.UserService.entity.Card;
import com.innowise.UserService.entity.User;
import com.innowise.UserService.repository.CardRepository;
import com.innowise.UserService.repository.UserRepository;
import com.innowise.UserService.service.exception.CardNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@SpringBootTest
@ActiveProfiles("test")  // ← Указываем использовать application-test.yaml
@Transactional
class CardServiceIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("InnoBaseTest")
            .withUsername("postgresTest")
            .withPassword("070696Test");

    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine")
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.redis.host", redis::getHost);
        registry.add("spring.redis.port", () -> redis.getMappedPort(6379));
    }

    @Autowired
    private CardService cardService;

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private UserRepository userRepository;

//    @Autowired
//    private TestEntityManager entityManager; // ← Для сброса кэша в тестах

    @Test
    void createCard_ShouldSaveCardToDatabase_WhenUserExists() {
        // Given
        User user = new User();
        user.setName("Ivan");
        user.setSurname("Ivanov");
        user.setEmail("ivan@example.com");
        user.setBirthDate(LocalDate.of(1990, 5, 15));
        User savedUser = userRepository.save(user);

        Card card = new Card();
        card.setNumber("1234567890123456");
        card.setHolder("IVAN IVANOV");
        card.setExpirationDate(LocalDate.of(2029, 12, 31));

        // When
        CardDto result = cardService.createCard(savedUser.getId(), card);

        // Then
        assertNotNull(result.getId()); // ← Теперь не null
        assertEquals("1234567890123456", result.getNumber());

        Card savedCard = cardRepository.findById(result.getId()).orElse(null);
        assertNotNull(savedCard);
        assertEquals("1234567890123456", savedCard.getNumber());
        assertEquals(savedUser.getId(), savedCard.getUser().getId());
    }

    @Test
    void getCardById_ShouldReturnCardDto_WhenCardExists() {
        // Given
        User user = new User();
        user.setName("Petr");
        user.setSurname("Petrov");
        user.setEmail("petr@example.com");
        user.setBirthDate(LocalDate.of(1985, 7, 20));
        User savedUser = userRepository.save(user);

        Card card = new Card();
        card.setNumber("1111222233334444");
        card.setHolder("PETR PETROV");
        card.setExpirationDate(LocalDate.of(2030, 11, 30));
        card.setUser(savedUser);
        Card savedCard = cardRepository.save(card);

        // When
        CardDto result = cardService.getCardById(savedCard.getId());

        // Then
        assertNotNull(result);
        assertEquals("1111222233334444", result.getNumber());
        assertEquals("PETR PETROV", result.getHolder());
    }

    @Test
    void getCardById_ShouldThrowException_WhenCardDoesNotExist() {
        // Given
        Long nonExistentId = 999L;

        // When / Then
        assertThrows(CardNotFoundException.class, () -> cardService.getCardById(nonExistentId));
    }

    @Test
    void getAllCards_ShouldReturnPagedCards_WhenCalled() {
        // Given
        User user = new User();
        user.setName("Sidor");
        user.setSurname("Sidorov");
        user.setEmail("sidor@example.com");
        user.setBirthDate(LocalDate.of(1980, 1, 1));
        User savedUser = userRepository.save(user);

        Card card1 = new Card();
        card1.setNumber("5555666677778888");
        card1.setHolder("SIDOR SIDOROV");
        card1.setExpirationDate(LocalDate.of(2028, 10, 15));
        card1.setUser(savedUser);

        Card card2 = new Card();
        card2.setNumber("9999888877776666");
        card2.setHolder("SIDOR SIDOROV");
        card2.setExpirationDate(LocalDate.of(2029, 12, 31));
        card2.setUser(savedUser);

        cardRepository.save(card1);
        cardRepository.save(card2);

        int page = 0;
        int size = 2;

        // When
        Page<CardDto> result = cardService.getAllCards(page, size);

        // Then
        assertEquals(2, result.getContent().size());
        assertEquals(2, result.getTotalElements());
        assertEquals(1, result.getTotalPages());
        assertEquals(0, result.getNumber());

        List<CardDto> content = result.getContent();
        assertTrue(content.stream().anyMatch(dto -> "5555666677778888".equals(dto.getNumber())));
        assertTrue(content.stream().anyMatch(dto -> "9999888877776666".equals(dto.getNumber())));
    }

    @Test
    void deleteCard_ShouldDeleteCard_WhenCardExists() {
        // Given
        User user = new User();
        user.setName("Alex");
        user.setSurname("Alexeev");
        user.setEmail("alex@example.com");
        user.setBirthDate(LocalDate.of(1995, 3, 10));
        User savedUser = userRepository.save(user);

        Card card = new Card();
        card.setNumber("1234123412341234");
        card.setHolder("ALEX ALEXEEV");
        card.setExpirationDate(LocalDate.of(2031, 1, 1));
        card.setUser(savedUser);
        Card savedCard = cardRepository.save(card);

        // When
        cardService.deleteCard(savedCard.getId());

        // Then
        assertFalse(cardRepository.existsById(savedCard.getId()));
    }

    @Test
    void deleteCard_ShouldThrowException_WhenCardDoesNotExist() {
        // Given
        Long nonExistentId = 999L;

        // When / Then
        assertThrows(CardNotFoundException.class, () -> cardService.deleteCard(nonExistentId));
    }
}
