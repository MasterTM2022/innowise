package com.innowise.UserService.service;

import com.innowise.UserService.client.UserServiceClient;
import com.innowise.UserService.dto.OrderDtoCreate;
import com.innowise.UserService.dto.OrderDtoUpdate;
import com.innowise.UserService.dto.OrderItemDto;
import com.innowise.UserService.dto.UserDto;
import com.innowise.UserService.entity.*;
import com.innowise.UserService.mapper.OrderMapper;
import com.innowise.UserService.repository.OrderRepository;
import com.innowise.UserService.repository.UserRepository;
import com.innowise.UserService.service.exception.OrderNotFoundException;
import com.innowise.UserService.service.exception.UserNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.testcontainers.junit.jupiter.Container;
import org.wiremock.integrations.testcontainers.WireMockContainer;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private UserServiceClient userServiceClient;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private OrderService orderService;

    @Container
    static WireMockContainer wiremock = new WireMockContainer("wiremock/wiremock:latest")
            .withExposedPorts(8080);

    @Test
    void createOrder_ShouldReturnOrderDto_WhenUserExists() {
        // Given
        OrderDtoCreate dto = new OrderDtoCreate();
        dto.setUserId(1L);
        dto.setStatus(OrderStatus.CREATED);

        Order order = new Order();
        order.setId(100L);
        order.setUserId(1L);
        order.setStatus(OrderStatus.CREATED);
        order.setCreationDate(LocalDateTime.now());

        OrderDtoCreate resultDto = new OrderDtoCreate();
        resultDto.setId(100L);
        resultDto.setUserId(1L);
        resultDto.setStatus(OrderStatus.CREATED);

        UserDto userDto = new UserDto(1L, "Ivan", "Ivanov", null, "ivan@example.com");

        when(orderMapper.toEntity(dto)).thenReturn(order);
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(orderMapper.toDto(order)).thenReturn(resultDto);
        when(userServiceClient.getUserById(1L)).thenReturn(userDto);

        // When
        OrderDtoCreate result = orderService.createOrder(dto);

        // Then
        assertNotNull(result);
        assertEquals(100L, result.getId());
        assertNotNull(result.getUserInfo());
        verify(orderMapper).toEntity(dto);
        verify(orderRepository).save(order);
        verify(userServiceClient).getUserById(1L);
    }

    @Test
    void createOrder_ShouldThrowUserNotFoundException_WhenUserDoesNotExist() {
        // Given
        OrderDtoCreate dto = new OrderDtoCreate();
        dto.setUserId(999L);

        when(userServiceClient.getUserById(999L))
                .thenThrow(new UserNotFoundException("User not found: 999"));

        // When / Then
        UserNotFoundException ex = assertThrows(UserNotFoundException.class, () -> {
            orderService.createOrder(dto);
        });

        assertEquals("User not found: 999", ex.getMessage());
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void getOrderById_ShouldReturnOrderDto_WhenOrderExists() {
        // Given
        Long orderId = 1L;
        Order order = new Order();
        order.setId(orderId);
        order.setUserId(1L);

        OrderDtoCreate dto = new OrderDtoCreate();
        dto.setId(orderId);
        dto.setUserId(1L);

        UserDto userDto = new UserDto(1L, "Ivan", "Ivanov", null, "ivan@example.com");

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderMapper.toDto(order)).thenReturn(dto);
        when(userServiceClient.getUserById(1L)).thenReturn(userDto);

        // When
        OrderDtoCreate result = orderService.getOrderById(orderId);

        // Then
        assertNotNull(result);
        assertEquals(orderId, result.getId());
        assertNotNull(result.getUserInfo());
        verify(orderRepository).findById(orderId);
        verify(userServiceClient).getUserById(1L);
    }

    @Test
    void getOrderById_ShouldThrowOrderNotFoundException_WhenOrderDoesNotExist() {
        // Given
        Long orderId = 999L;
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        // When / Then
        OrderNotFoundException ex = assertThrows(OrderNotFoundException.class, () -> {
            orderService.getOrderById(orderId);
        });
        verify(userServiceClient, never()).getUserById(any());
    }

    @Test
    void updateOrder_ShouldReturnUpdatedOrderDto_WhenOrderExists() {
        // Given
        Long orderId = 1L;
        OrderDtoUpdate updateDto = new OrderDtoUpdate();
        updateDto.setStatus(OrderStatus.CONFIRMED);

        Order order = new Order();
        order.setId(orderId);
        order.setUserId(1L);
        order.setStatus(OrderStatus.CREATED);

        Order updatedOrder = new Order();
        updatedOrder.setId(orderId);
        updatedOrder.setUserId(1L);
        updatedOrder.setStatus(OrderStatus.CONFIRMED);

        OrderDtoCreate resultDto = new OrderDtoCreate();
        resultDto.setId(orderId);
        resultDto.setUserId(1L);
        resultDto.setStatus(OrderStatus.CONFIRMED);

        UserDto userDto = new UserDto(1L, "Ivan", "Ivanov", null, "ivan@example.com");

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(updatedOrder);
        when(orderMapper.toDto(updatedOrder)).thenReturn(resultDto);
        when(userServiceClient.getUserById(1L)).thenReturn(userDto);

        // When
        OrderDtoCreate result = orderService.updateOrder(orderId, updateDto);

        // Then
        assertNotNull(result);
        assertEquals(OrderStatus.CONFIRMED, result.getStatus());
        verify(orderRepository).save(any(Order.class));
        verify(userServiceClient).getUserById(1L);
    }

    @Test
    void updateOrder_ShouldThrowOrderNotFoundException_WhenOrderDoesNotExist() {
        // Given
        Long orderId = 999L;
        OrderDtoUpdate updateDto = new OrderDtoUpdate();
        updateDto.setStatus(OrderStatus.CONFIRMED);
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        // When / Then
        OrderNotFoundException ex = assertThrows(OrderNotFoundException.class, () -> {
            orderService.updateOrder(orderId, updateDto);
        });
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void deleteOrder_ShouldDeleteOrder_WhenOrderExists() {
        // Given
        Long orderId = 1L;
        when(orderRepository.existsById(orderId)).thenReturn(true);

        // When
        orderService.deleteOrder(orderId);

        // Then
        verify(orderRepository).existsById(orderId);
        verify(orderRepository).deleteById(orderId);
    }

    @Test
    void deleteOrder_ShouldThrowOrderNotFoundException_WhenOrderDoesNotExist() {
        // Given
        Long orderId = 999L;
        when(orderRepository.existsById(orderId)).thenReturn(false);

        // When / Then
        OrderNotFoundException ex = assertThrows(OrderNotFoundException.class, () -> {
            orderService.deleteOrder(orderId);
        });
        verify(orderRepository).existsById(orderId);
        verify(orderRepository, never()).deleteById(any());
    }

    @Test
    void getOrdersByIds_ShouldReturnOrderDtos_WhenOrdersExist() {
        // Given
        List<Long> ids = Arrays.asList(1L, 2L);
        Order order1 = new Order();
        order1.setId(1L);
        order1.setUserId(1L);
        Order order2 = new Order();
        order2.setId(2L);
        order2.setUserId(1L);
        List<Order> orders = Arrays.asList(order1, order2);

        OrderDtoCreate dto1 = new OrderDtoCreate();
        dto1.setId(1L);
        dto1.setUserId(1L);
        OrderDtoCreate dto2 = new OrderDtoCreate();
        dto2.setId(2L);
        dto2.setUserId(1L);
        List<OrderDtoCreate> dtos = Arrays.asList(dto1, dto2);

        UserDto userDto = new UserDto(1L, "Ivan", "Ivanov", null, "ivan@example.com");

        when(orderRepository.findByIdIn(ids)).thenReturn(orders);
        when(orderMapper.toDto(order1)).thenReturn(dto1);
        when(orderMapper.toDto(order2)).thenReturn(dto2);
        when(userServiceClient.getUserById(1L)).thenReturn(userDto);

        // When
        List<OrderDtoCreate> result = orderService.getOrdersByIds(ids);

        // Then
        assertEquals(2, result.size());
        assertNotNull(result.get(0).getUserInfo());
        verify(userServiceClient, times(2)).getUserById(1L);
    }

    @Test
    void getOrdersByStatuses_ShouldReturnOrderDtos_WhenOrdersExist() {
        // Given
        List<OrderStatus> statuses = Collections.singletonList(OrderStatus.CREATED);
        Order order = new Order();
        order.setId(1L);
        order.setUserId(1L);
        order.setStatus(OrderStatus.CREATED);
        List<Order> orders = Collections.singletonList(order);

        OrderDtoCreate dto = new OrderDtoCreate();
        dto.setId(1L);
        dto.setUserId(1L);
        dto.setStatus(OrderStatus.CREATED);
        List<OrderDtoCreate> dtos = Collections.singletonList(dto);

        UserDto userDto = new UserDto(1L, "Ivan", "Ivanov", null, "ivan@example.com");

        when(orderRepository.findByStatusIn(statuses)).thenReturn(orders);
        when(orderMapper.toDto(order)).thenReturn(dto);
        when(userServiceClient.getUserById(1L)).thenReturn(userDto);

        // When
        List<OrderDtoCreate> result = orderService.getOrdersByStatuses(statuses);

        // Then
        assertEquals(1, result.size());
        assertNotNull(result.get(0).getUserInfo());
        verify(userServiceClient).getUserById(1L);
    }

    @Test
    void updateOrder_ShouldUpdateOrderItems_WhenOrderItemsProvided() {
        // Given
        Long orderId = 1L;
        OrderDtoUpdate updateDto = new OrderDtoUpdate();

        OrderItemDto itemDto = new OrderItemDto();
        itemDto.setId(1L);
        itemDto.setQuantity(2);

        updateDto.setOrderItems(Collections.singletonList(itemDto));

        Order order = new Order();
        order.setId(orderId);
        order.setUserId(1L);

        Item item = new Item();
        item.setId(10L);

        OrderItem orderItem = new OrderItem();
        orderItem.setId(1L);
        orderItem.setOrder(order);
        orderItem.setItem(item);
        orderItem.setQuantity(2);

        Order updatedOrder = new Order();
        updatedOrder.setId(orderId);
        updatedOrder.setUserId(1L);
        updatedOrder.setOrderItems(Collections.singletonList(orderItem));

        OrderDtoCreate resultDto = new OrderDtoCreate();
        resultDto.setId(orderId);
        resultDto.setUserId(1L);

        UserDto userDto = new UserDto(1L, "Ivan", "Ivanov", null, "ivan@example.com");

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderMapper.toEntity(any(OrderItemDto.class))).thenReturn(orderItem);
        when(orderRepository.save(any(Order.class))).thenReturn(updatedOrder);
        when(orderMapper.toDto(updatedOrder)).thenReturn(resultDto);
        when(userServiceClient.getUserById(1L)).thenReturn(userDto);

        // When
        OrderDtoCreate result = orderService.updateOrder(orderId, updateDto);

        // Then
        assertNotNull(result);
        verify(orderRepository).save(any(Order.class));
        verify(orderMapper, times(1)).toEntity(any(OrderItemDto.class));
    }

    @Test
    void getOrdersByUserId_ShouldReturnPagedOrders_WhenUserServiceAvailable() {
        // Given
        Long userId = 1L;

        UserDto userDto = new UserDto();
        userDto.setId(userId);
        userDto.setName("Ivan");
        userDto.setSurname("Ivanov");
        userDto.setEmail("ivan@example.com");

        when(userServiceClient.getUserById(userId)).thenReturn(userDto);

        Order order1 = new Order();
        order1.setId(100L);
        order1.setUserId(userId);
        order1.setStatus(OrderStatus.CREATED);

        Order order2 = new Order();
        order2.setId(101L);
        order2.setUserId(userId);
        order2.setStatus(OrderStatus.SHIPPED);

        List<Order> orders = Arrays.asList(order1, order2);
        Page<Order> ordersPage = new PageImpl<>(orders);

        OrderDtoCreate dto1 = new OrderDtoCreate();
        dto1.setId(100L);
        dto1.setUserId(userId);
        dto1.setStatus(OrderStatus.CREATED);

        OrderDtoCreate dto2 = new OrderDtoCreate();
        dto2.setId(101L);
        dto2.setUserId(userId);
        dto2.setStatus(OrderStatus.SHIPPED);

        when(orderRepository.findByUserId(eq(userId), any(Pageable.class))).thenReturn(ordersPage);

        when(orderMapper.toDtoWithoutItems(order1)).thenReturn(dto1);
        when(orderMapper.toDtoWithoutItems(order2)).thenReturn(dto2);

        // When
        Page<OrderDtoCreate> result = orderService.getOrdersByUserId(userId, 0, 10);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        for (OrderDtoCreate dto : result.getContent()) {
            assertThat(dto.getUserId()).isEqualTo(userId);
            assertThat(dto.getUserInfo()).isNotNull();
            assertThat(dto.getUserInfo().getName()).isEqualTo("Ivan");
        }

        verify(userServiceClient, times(2)).getUserById(userId);
    }

    @Test
    void isOwner_ShouldReturnTrue_WhenUserOwnsOrder() {
        // Given
        Long userId = 1L;
        Long orderId = 100L;

        Order order = new Order();
        order.setId(orderId);
        order.setUserId(userId);

        when(orderRepository.existsByIdAndUserId(orderId, userId)).thenReturn(true);

        // When
        boolean result = orderService.isOwner(orderId, userId);

        // Then
        assertTrue(result);

        verify(orderRepository).existsByIdAndUserId(orderId, userId);
    }

    @Test
    void isOwner_ShouldReturnFalse_WhenUserDoesNotOwnOrder() {
        // Given
        User user1 = new User();
        user1.setName("Ivan");
        user1.setId(1L);

        User user2 = new User();
        user2.setName("Petr");
        user2.setId(2L);

        Order order = new Order();
        order.setUserId(user1.getId());
        order.setId(100L);

        when(orderRepository.existsByIdAndUserId(order.getId(), user2.getId())).thenReturn(false);

        // When
        boolean result = orderService.isOwner(order.getId(), user2.getId());

        // Then
        assertFalse(result);

        verify(orderRepository).existsByIdAndUserId(order.getId(), user2.getId());
    }

    @Test
    void getOrdersByIdsForUser_ShouldReturnOrders_WhenUserOwnsAllOrders() {
        // Given
        Long userId = 1L;
        List<Long> ids = Arrays.asList(100L, 101L);

        Order order1 = new Order();
        order1.setId(100L);
        order1.setUserId(userId);

        Order order2 = new Order();
        order2.setId(101L);
        order2.setUserId(userId);

        List<Order> orders = Arrays.asList(order1, order2);

        OrderDtoCreate dto1 = new OrderDtoCreate();
        dto1.setId(100L);
        dto1.setUserId(userId);

        OrderDtoCreate dto2 = new OrderDtoCreate();
        dto2.setId(101L);
        dto2.setUserId(userId);

        when(orderRepository.findByIdIn(ids)).thenReturn(orders);
        when(orderMapper.toDto(order1)).thenReturn(dto1);
        when(orderMapper.toDto(order2)).thenReturn(dto2);

        // When
        List<OrderDtoCreate> result = orderService.getOrdersByIdsForUser(ids, userId);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).extracting(OrderDtoCreate::getUserId)
                .containsOnly(userId);

        verify(orderRepository).findByIdIn(ids);
        verify(orderMapper).toDto(order1);
        verify(orderMapper).toDto(order2);
    }

    @Test
    void getOrdersByIdsForUser_ShouldThrowAccessDenied_WhenUserAccessesForeignOrder() {
        // Given
        Long userId = 1L;
        List<Long> ids = Arrays.asList(100L, 101L);

        Order order1 = new Order();
        order1.setId(100L);
        order1.setUserId(userId);

        Order order2 = new Order();
        order2.setId(101L);
        order2.setUserId(2L);

        List<Order> orders = Arrays.asList(order1, order2);

        when(orderRepository.findByIdIn(ids)).thenReturn(orders);

        // When / Then
        AccessDeniedException exception = assertThrows(
                AccessDeniedException.class,
                () -> orderService.getOrdersByIdsForUser(ids, userId)
        );

        assertThat(exception.getMessage()).isEqualTo("You can only access your own orders");

        verify(orderRepository).findByIdIn(ids);
        verify(orderMapper, never()).toDto((Order) any());
    }
}