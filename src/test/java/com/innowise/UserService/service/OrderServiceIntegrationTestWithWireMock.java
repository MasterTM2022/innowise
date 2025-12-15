package com.innowise.UserService.service;


import com.github.tomakehurst.wiremock.client.WireMock;
import com.innowise.UserService.UserServiceApplication;
import com.innowise.UserService.dto.OrderDtoCreate;
import com.innowise.UserService.entity.Order;
import com.innowise.UserService.entity.OrderStatus;
import com.innowise.UserService.entity.User;
import com.innowise.UserService.repository.OrderRepository;
import com.innowise.UserService.repository.UserRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.wiremock.integrations.testcontainers.WireMockContainer;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.*;

@Testcontainers
@SpringBootTest(classes = UserServiceApplication.class)
@ActiveProfiles("test")
public class OrderServiceIntegrationTestWithWireMock {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("InnoBaseTest")
            .withUsername("postgres")
            .withPassword("070696");

    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine")
            .withExposedPorts(6379);

    @Container
    static WireMockContainer wiremock = new WireMockContainer("wiremock/wiremock:latest")
            .withExposedPorts(8080);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("user.service.url", () -> "http://localhost:" + wiremock.getMappedPort(8080));
    }

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Should return order with user info when UserService is available")
    void createOrder_ShouldReturnOrderWithUserInfo_WhenUserServiceAvailable() {
        // Given
        String userJson = """
                {
                  "id": 1,
                  "name": "Ivan",
                  "surname": "Ivanov",
                  "birthDate": "1990-05-15",
                  "email": "ivan@example.com"
                }
                """;

        WireMock.configureFor("localhost", wiremock.getMappedPort(8080));

        WireMock.stubFor(
                WireMock.get(WireMock.urlPathEqualTo("/api/v1/users/1"))
                        .willReturn(WireMock.aResponse()
                                .withStatus(200)
                                .withHeader("Content-Type", "application/json")
                                .withBody(userJson))
        );

        OrderDtoCreate dto = new OrderDtoCreate();
        dto.setUserId(1L);
        dto.setStatus(OrderStatus.CREATED);

        // When
        OrderDtoCreate result = orderService.createOrder(dto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getUserInfo()).isNotNull();
        assertThat(result.getUserInfo().getName()).isEqualTo("Ivan");
        WireMock.verify(
                WireMock.getRequestedFor(WireMock.urlPathEqualTo("/api/v1/users/1"))
        );
    }

    @Test
    @DisplayName("Should return fallback user info when UserService is unavailable")
    void createOrder_ShouldReturnFallback_WhenUserServiceUnavailable() {
        // Given
        OrderDtoCreate dto = new OrderDtoCreate();
        dto.setUserId(1L);
        dto.setStatus(OrderStatus.CREATED);

        // When
        OrderDtoCreate result = orderService.createOrder(dto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getUserInfo()).isNotNull();
        assertThat(result.getUserInfo().getName()).isEqualTo("Unknown User");
        assertThat(result.getUserInfo().getSurname()).isEqualTo("N/A");
        assertThat(result.getUserInfo().getEmail()).isEqualTo("unknown@example.com");
        assertThat(result.getUserInfo().getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Should return fallback when fetching order by ID and UserService fails")
    void getOrderById_ShouldReturnFallback_WhenUserServiceFails() {
        // Given
        Order order = new Order();
        order.setUserId(1L);
        order.setStatus(OrderStatus.CREATED);
        order.setCreationDate(LocalDate.now().atStartOfDay());
        Order saved = orderRepository.save(order);

        // When
        OrderDtoCreate result = orderService.getOrderById(saved.getId());

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getUserInfo()).isNotNull();
        assertThat(result.getUserInfo().getName()).isEqualTo("Unknown User");
        assertThat(result.getUserInfo().getSurname()).isEqualTo("N/A");
        assertThat(result.getUserInfo().getEmail()).isEqualTo("unknown@example.com");
        assertThat(result.getUserInfo().getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Should return fallback for batch-get when UserService fails")
    void getOrdersByIds_ShouldReturnFallbackList_WhenUserServiceFails() {
        // Given
        Order order1 = new Order();
        order1.setUserId(1L);
        order1.setStatus(OrderStatus.CREATED);
        Order saved1 = orderRepository.save(order1);

        Order order2 = new Order();
        order2.setUserId(2L);
        order2.setStatus(OrderStatus.SHIPPED);
        Order saved2 = orderRepository.save(order2);

        List<Long> ids = List.of(saved1.getId(), saved2.getId());

        // When
        List<OrderDtoCreate> result = orderService.getOrdersByIds(ids);

        // Then
        assertThat(result).hasSize(2);
        for (OrderDtoCreate dto : result) {
            assertThat(dto.getUserInfo()).isNotNull();
            assertThat(dto.getUserInfo().getName()).isEqualTo("Unknown User");
            assertThat(dto.getUserInfo().getSurname()).isEqualTo("N/A");
            assertThat(dto.getUserInfo().getEmail()).isEqualTo("unknown@example.com");
        }
    }

    @Test
    @DisplayName("Should return fallback for status-based get when UserService fails")
    void getOrdersByStatuses_ShouldReturnFallbackList_WhenUserServiceFails() {
        // Given
        User user1 = new User();
        user1.setName("Ivan");
        user1.setSurname("Ivanov");
        user1.setEmail("ivan@example.com");
        user1.setBirthDate(LocalDate.of(1990, 5, 15));
        User savedUser1 = userRepository.save(user1);

        User user2 = new User();
        user2.setName("Petr");
        user2.setSurname("Petrov");
        user2.setEmail("petr@example.com");
        user2.setBirthDate(LocalDate.of(1985, 7, 20));
        User savedUser2 = userRepository.save(user2);

        Order order1 = new Order();
        order1.setUserId(savedUser1.getId());
        order1.setStatus(OrderStatus.CREATED);
        Order savedOrder1 = orderRepository.save(order1);

        Order order2 = new Order();
        order2.setUserId(savedUser1.getId());
        order2.setStatus(OrderStatus.SHIPPED);
        Order savedOrder2 = orderRepository.save(order2);

        Order order3 = new Order();
        order3.setUserId(savedUser2.getId());
        order3.setStatus(OrderStatus.CREATED);
        Order savedOrder3 = orderRepository.save(order3);

        List<OrderStatus> statuses = List.of(OrderStatus.CREATED, OrderStatus.SHIPPED);

        // When
        List<OrderDtoCreate> result = orderService.getOrdersByStatuses(statuses);

        // Then
        List<OrderDtoCreate> user1Orders = result.stream()
                .filter(dto -> dto.getUserId().equals(savedUser1.getId()))
                .collect(Collectors.toList());

        assertThat(user1Orders).hasSize(2);

        for (OrderDtoCreate dto : user1Orders) {
            assertThat(dto.getUserInfo()).isNotNull();
            assertThat(dto.getUserInfo().getName()).isEqualTo("Unknown User");
            assertThat(dto.getUserInfo().getSurname()).isEqualTo("N/A");
            assertThat(dto.getUserInfo().getEmail()).isEqualTo("unknown@example.com");
        }
    }

    @Test
    @DisplayName("Should return fallback for user-based get when UserService fails")
    void getOrdersByUserId_ShouldReturnFallbackPage_WhenUserServiceFails() {
        // Given
        Order order1 = new Order();
        order1.setUserId(1L);
        order1.setStatus(OrderStatus.CREATED);
        orderRepository.save(order1);

        Order order2 = new Order();
        order2.setUserId(1L);
        order2.setStatus(OrderStatus.SHIPPED);
        orderRepository.save(order2);

        // When
        var result = orderService.getOrdersByUserId(1L, 0, 10);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        for (OrderDtoCreate dto : result.getContent()) {
            assertThat(dto.getUserInfo()).isNotNull();
            assertThat(dto.getUserInfo().getName()).isEqualTo("Unknown User");
            assertThat(dto.getUserInfo().getSurname()).isEqualTo("N/A");
            assertThat(dto.getUserInfo().getEmail()).isEqualTo("unknown@example.com");
        }
    }

    @Test
    @DisplayName("Should call fallback method when Circuit Breaker opens after repeated failures")
    void circuitBreaker_ShouldOpenAndReturnFallback_WhenRepeatedFailures() {
        // Given
        WireMock client = new WireMock("localhost", wiremock.getMappedPort(8080));

        client.register(
                WireMock.get(WireMock.urlPathMatching("/api/v1/users/.*"))
                        .willReturn(WireMock.aResponse()
                                .withStatus(500)
                                .withBody("Internal Server Error"))
        );

        OrderDtoCreate dto = new OrderDtoCreate();
        dto.setUserId(1L);
        dto.setStatus(OrderStatus.CREATED);

        // First call: fallback immediately (because service is down)
        OrderDtoCreate result1 = orderService.createOrder(dto);

        // Second call: fallback again (circuit breaker may be OPEN now)
        OrderDtoCreate result2 = orderService.createOrder(dto);

        // Then
        assertThat(result1).isNotNull();
        assertThat(result1.getUserInfo()).isNotNull();
        assertThat(result1.getUserInfo().getName()).isEqualTo("Unknown User");

        assertThat(result2).isNotNull();
        assertThat(result2.getUserInfo()).isNotNull();
        assertThat(result2.getUserInfo().getName()).isEqualTo("Unknown User");
    }
}