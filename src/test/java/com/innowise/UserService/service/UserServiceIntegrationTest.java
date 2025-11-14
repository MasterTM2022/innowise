package com.innowise.UserService.service;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import com.innowise.UserService.dto.UserDto;
import com.innowise.UserService.entity.User;
import com.innowise.UserService.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;


import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UserServiceIntegrationTest {

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
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void createUser_ShouldSaveUserToDatabase() {
        // Given
        User user = new User();
        user.setName("Ivan");
        user.setSurname("Ivanov");
        user.setEmail("ivan@example.com");
        user.setBirthDate(LocalDate.of(1990, 5, 15));

        // When
        UserDto result = userService.createUser(user);

        // Then
        assertNotNull(result.getId());
        assertEquals("ivan@example.com", result.getEmail());

        User savedUser = userRepository.findById(result.getId()).orElse(null);
        assertNotNull(savedUser);
        assertEquals("ivan@example.com", savedUser.getEmail());
    }

    @Test
    void getUserById_ShouldReturnUser_WhenExists() {
        // Given
        User user = new User();
        user.setName("Petr");
        user.setSurname("Petrov");
        user.setEmail("petr@example.com");
        user.setBirthDate(LocalDate.of(1985, 7, 20));
        User savedUser = userRepository.save(user);

        // When
        UserDto result = userService.getUserById(savedUser.getId());

        // Then
        assertNotNull(result);
        assertEquals("petr@example.com", result.getEmail());
    }

    @Test
    void updateUser_ShouldUpdateUser_WhenExists() {
        // Given
        User user = new User();
        user.setName("Alex");
        user.setSurname("Alexeev");
        user.setEmail("alex@example.com");
        user.setBirthDate(LocalDate.of(1995, 3, 10));
        User savedUser = userRepository.save(user);

        UserDto updateDto = new UserDto(savedUser.getId(), "Alexander", "Alexeev", LocalDate.of(1995, 3, 10), "alexander@example.com");

        // When
        UserDto result = userService.updateUser(savedUser.getId(), updateDto);

        // Then
        assertEquals("Alexander", result.getName());
        assertEquals("alexander@example.com", result.getEmail());

        User updatedUser = userRepository.findById(savedUser.getId()).orElse(null);
        assertNotNull(updatedUser);
        assertEquals("Alexander", updatedUser.getName());
        assertEquals("alexander@example.com", updatedUser.getEmail());
    }

    @Test
    void deleteUser_ShouldDeleteUser_WhenExists() {
        // Given
        User user = new User();
        user.setName("Sidor");
        user.setSurname("Sidorov");
        user.setEmail("sidor@example.com");
        user.setBirthDate(LocalDate.of(1980, 1, 1));
        User savedUser = userRepository.save(user);

        // When
        userService.deleteUser(savedUser.getId());

        // Then
        assertFalse(userRepository.existsById(savedUser.getId()));
    }

    @Test
    void getAllUsers_ShouldReturnPagedUsers_WhenCalled() {
        // Given
        int page = 0;
        int size = 2;

        User user1 = new User();
        user1.setName("Ivan");
        user1.setSurname("Ivanov");
        user1.setEmail("ivan@example.com");
        user1.setBirthDate(LocalDate.of(1990, 5, 15));

        User user2 = new User();
        user2.setName("Petr");
        user2.setSurname("Petrov");
        user2.setEmail("petr@example.com");
        user2.setBirthDate(LocalDate.of(1985, 7, 20));

        User user3 = new User();
        user3.setName("Sidor");
        user3.setSurname("Sidorov");
        user3.setEmail("sidor@example.com");
        user3.setBirthDate(LocalDate.of(1995, 1, 10));

        userRepository.save(user1);
        userRepository.save(user2);
        userRepository.save(user3);

        // When
        Page<UserDto> result = userService.getAllUsers(page, size);

        // Then
        assertEquals(2, result.getContent().size()); // there are 2 users on page (size = 2)
        assertEquals(3, result.getTotalElements());   // Total 3 users
        assertEquals(2, result.getTotalPages());      // 3 / 2 = 2 pages
        assertEquals(0, result.getNumber());          // number of page
        assertTrue(result.hasNext());                 // there is next page

        List<UserDto> content = result.getContent();
        UserDto firstUser = content.get(0);
        UserDto secondUser = content.get(1);

        assertEquals("ivan@example.com", firstUser.getEmail());
        assertEquals("petr@example.com", secondUser.getEmail());

        assertFalse(content.stream()
                .anyMatch(dto -> dto.getEmail().equals("sidor@example.com")));
    }

    @Test
    void getAllUsers_ShouldReturnEmptyPage_WhenNoUsersExist() {
        // Given
        int page = 0;
        int size = 10;

        // When
        Page<UserDto> result = userService.getAllUsers(page, size);

        // Then
        assertTrue(result.isEmpty());
        assertEquals(0, result.getTotalElements());
        assertEquals(0, result.getTotalPages());
    }

    @Test
    void getAllUsers_ShouldReturnSecondPage_WhenRequested() {
        // Given
        int page = 1;
        int size = 2;

        User user1 = new User();
        user1.setName("Ivan");
        user1.setSurname("Ivanov");
        user1.setEmail("ivan@example.com");
        user1.setBirthDate(LocalDate.of(1990, 5, 15));

        User user2 = new User();
        user2.setName("Petr");
        user2.setSurname("Petrov");
        user2.setEmail("petr@example.com");
        user2.setBirthDate(LocalDate.of(1985, 7, 20));

        User user3 = new User();
        user3.setName("Sidor");
        user3.setSurname("Sidorov");
        user3.setEmail("sidor@example.com");
        user3.setBirthDate(LocalDate.of(1995, 1, 10));

        userRepository.save(user1);
        userRepository.save(user2);
        userRepository.save(user3);

        // When
        Page<UserDto> result = userService.getAllUsers(page, size);

        // Then
        assertEquals(1, result.getContent().size()); // Only 1 user on the 2nd page
        assertEquals(3, result.getTotalElements());
        assertEquals(2, result.getTotalPages());
        assertEquals(1, result.getNumber());
        assertFalse(result.hasNext()); // There are no pages
        assertTrue(result.hasPrevious());

        UserDto lastUser = result.getContent().get(0);
        assertEquals("sidor@example.com", lastUser.getEmail());
    }
}