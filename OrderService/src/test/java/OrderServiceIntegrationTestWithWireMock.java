import com.github.tomakehurst.wiremock.client.WireMock;
import com.innowise.order.OrderServiceApplication;
import com.innowise.order.dto.OrderDtoCreate;
import com.innowise.order.dto.OrderResponseDto;
import com.innowise.order.entity.Order;
import com.innowise.order.entity.OrderStatus;
import com.innowise.order.repository.OrderRepository;
import com.innowise.order.service.OrderService;
import org.junit.jupiter.api.*;
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
import org.wiremock.integrations.testcontainers.WireMockContainer;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.*;

@Testcontainers
@SpringBootTest(classes = OrderServiceApplication.class,
        properties = {
                "logging.level.com.innowise.order.client.UserServiceClient=DEBUG"
        })
@ActiveProfiles("test")
@Transactional
class OrderServiceIntegrationTestWithWireMock {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("InnoOrderDBTest")
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
        registry.add("user.service.url",
                () -> "http://localhost:" + wiremock.getFirstMappedPort());
    }

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepository;

    @BeforeAll
    static void beforeAll() {
        wiremock.start();
        postgres.start();
        redis.start();
        WireMock.configureFor("localhost", wiremock.getFirstMappedPort());
    }

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();
        WireMock.reset();
    }

    @AfterAll
    static void afterAll() {
        wiremock.stop();
        postgres.stop();
        redis.stop();
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

        stubFor(get(urlPathEqualTo("/api/v1/users/1"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(userJson))
        );

        OrderDtoCreate dto = new OrderDtoCreate();
        dto.setUserId(1L);

        // When
        OrderResponseDto result = orderService.createOrder(dto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getUserName()).isEqualTo("Ivan");
        assertThat(result.getUserSurname()).isEqualTo("Ivanov");
        verify(getRequestedFor(urlPathEqualTo("/api/v1/users/1")));
    }

    @Test
    @DisplayName("Should return fallback user info when UserService is unavailable")
    void createOrder_ShouldReturnFallback_WhenUserServiceUnavailable() {
        // Given
        stubFor(get(urlPathMatching("/api/v1/users/.*"))
                .willReturn(aResponse().withStatus(500)));

        OrderDtoCreate dto = new OrderDtoCreate();
        dto.setUserId(1L);

        // When
        OrderResponseDto result = orderService.createOrder(dto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getUserName()).isEqualTo("Unknown user name");
        assertThat(result.getUserSurname()).isEqualTo("Unknown user surname");
    }

    @Test
    @DisplayName("Should return fallback when fetching order by ID and UserService fails")
    void getOrderById_ShouldReturnFallback_WhenUserServiceFails() {
        // Given
        stubFor(get(urlPathMatching("/api/v1/users/.*"))
                .willReturn(aResponse().withStatus(500)));

        Order order = new Order();
        order.setUserId(1L);
        order.setStatus(OrderStatus.CREATED);
        order.setCreationDate(LocalDateTime.now());
        Order saved = orderRepository.save(order);

        // When
        OrderResponseDto result = orderService.getOrderById(saved.getId());

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getUserName()).isEqualTo("Unknown user name");
        assertThat(result.getUserSurname()).isEqualTo("Unknown user surname");
    }

    @Test
    @DisplayName("Should return fallback for batch-get when UserService fails")
    void getOrdersByIds_ShouldReturnFallbackList_WhenUserServiceFails() {
        // Given
        stubFor(get(urlPathMatching("/api/v1/users/.*"))
                .willReturn(aResponse().withStatus(500)));

        Order order1 = new Order();
        order1.setUserId(1L);
        order1.setStatus(OrderStatus.CREATED);
        Order saved1 = orderRepository.save(order1);

        Order order2 = new Order();
        order2.setUserId(2L);
        order2.setStatus(OrderStatus.SHIPPED);
        Order saved2 = orderRepository.save(order2);

        List<Long> ids = Arrays.asList(saved1.getId(), saved2.getId());

        // When
        List<OrderResponseDto> result = orderService.getOrdersByIds(ids);

        // Then
        assertThat(result).hasSize(2);
        for (OrderResponseDto dto : result) {
            assertThat(dto.getUserName()).isEqualTo("Unknown user name");
            assertThat(dto.getUserSurname()).isEqualTo("Unknown user surname");
        }
    }

    @Test
    @DisplayName("Should return fallback for status-based get when UserService fails")
    void getOrdersByStatuses_ShouldReturnFallbackList_WhenUserServiceFails() {
        // Given
        stubFor(get(urlPathMatching("/api/v1/users/.*"))
                .willReturn(aResponse().withStatus(500)));

        Order order1 = new Order();
        order1.setUserId(1L);
        order1.setStatus(OrderStatus.CREATED);
        orderRepository.save(order1);

        Order order2 = new Order();
        order2.setUserId(1L);
        order2.setStatus(OrderStatus.SHIPPED);
        orderRepository.save(order2);

        Order order3 = new Order();
        order3.setUserId(2L);
        order3.setStatus(OrderStatus.CREATED);
        orderRepository.save(order3);

        List<OrderStatus> statuses = Arrays.asList(OrderStatus.CREATED, OrderStatus.SHIPPED);

        // When
        List<OrderResponseDto> result = orderService.getOrdersByStatuses(statuses);

        // Then
        assertThat(result).hasSize(3);
        for (OrderResponseDto dto : result) {
            assertThat(dto.getUserName()).isEqualTo("Unknown user name");
            assertThat(dto.getUserSurname()).isEqualTo("Unknown user surname");
        }
    }

    @Test
    @DisplayName("Should return fallback for user-based get when UserService fails")
    void getOrdersByUserId_ShouldReturnFallbackPage_WhenUserServiceFails() {
        // Given
        stubFor(get(urlPathMatching("/api/v1/users/.*"))
                .willReturn(aResponse().withStatus(500)));

        Order order1 = new Order();
        order1.setUserId(1L);
        order1.setStatus(OrderStatus.CREATED);
        orderRepository.save(order1);

        Order order2 = new Order();
        order2.setUserId(1L);
        order2.setStatus(OrderStatus.SHIPPED);
        orderRepository.save(order2);

        // When
        Page<OrderResponseDto> result = orderService.getOrdersByUserId(1L, 0, 10);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        for (OrderResponseDto dto : result.getContent()) {
            assertThat(dto.getUserName()).isEqualTo("Unknown user name");
            assertThat(dto.getUserSurname()).isEqualTo("Unknown user surname");
        }
    }

    @Test
    @DisplayName("Should call fallback method when Circuit Breaker opens after repeated failures")
    void circuitBreaker_ShouldOpenAndReturnFallback_WhenRepeatedFailures() {
        // Given
        stubFor(get(urlPathMatching("/api/v1/users/.*"))
                .willReturn(aResponse().withStatus(500)));

        OrderDtoCreate dto = new OrderDtoCreate();
        dto.setUserId(1L);

        // When
        OrderResponseDto result1 = orderService.createOrder(dto);
        OrderResponseDto result2 = orderService.createOrder(dto);

        // Then
        assertThat(result1.getUserName()).isEqualTo("Unknown user name");
        assertThat(result2.getUserName()).isEqualTo("Unknown user name");
    }
}