import com.innowise.common.dto.UserDto;
import com.innowise.order.client.UserServiceClient;
import com.innowise.order.dto.*;
import com.innowise.order.entity.Order;
import com.innowise.order.entity.OrderStatus;
import com.innowise.order.mapper.OrderMapper;
import com.innowise.order.repository.OrderRepository;
import com.innowise.order.service.OrderService;
import com.innowise.order.service.exception.OrderNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private UserServiceClient userServiceClient;

    @InjectMocks
    private OrderService orderService;

    @Test
    void createOrder_ShouldReturnOrderResponseDto_WhenUserExists() {
        // Given
        OrderDtoCreate requestDto = new OrderDtoCreate();
        requestDto.setUserId(1L);

        Order order = new Order();
        order.setId(100L);
        order.setUserId(1L);
        order.setStatus(OrderStatus.CREATED);
        order.setCreationDate(LocalDateTime.now());

        OrderResponseDto resultDto = new OrderResponseDto();
        resultDto.setId(100L);
        resultDto.setUserId(1L);
        resultDto.setStatus(OrderStatus.CREATED);
        resultDto.setCreationDate(LocalDateTime.now());

        UserDto userDto = new UserDto(1L, "Ivan", "Ivanov", null, "ivan@example.com");

        when(orderMapper.toEntity(requestDto)).thenReturn(order);
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(orderMapper.toResponseDto(order)).thenReturn(resultDto);
        when(userServiceClient.getUserById(1L)).thenReturn(userDto);

        // When
        OrderResponseDto result = orderService.createOrder(requestDto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(100L);
        assertThat(result.getUserName()).isEqualTo("Ivan");
        assertThat(result.getUserSurname()).isEqualTo("Ivanov");
        verify(orderMapper).toEntity(requestDto);
        verify(orderRepository).save(order);
        verify(userServiceClient).getUserById(1L);
    }

    @Test
    void createOrder_ShouldReturnUnknownUser_WhenUserServiceFails() {
        // Given
        OrderDtoCreate requestDto = new OrderDtoCreate();
        requestDto.setUserId(1L);

        Order order = new Order();
        order.setId(100L);
        order.setUserId(1L);
        order.setStatus(OrderStatus.CREATED);
        order.setCreationDate(LocalDateTime.now());

        OrderResponseDto resultDto = new OrderResponseDto();
        resultDto.setId(100L);
        resultDto.setUserId(1L);
        resultDto.setStatus(OrderStatus.CREATED);
        resultDto.setCreationDate(LocalDateTime.now());

        when(orderMapper.toEntity(requestDto)).thenReturn(order);
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(orderMapper.toResponseDto(order)).thenReturn(resultDto);
        when(userServiceClient.getUserById(1L)).thenThrow(new RuntimeException("Service unavailable"));

        // When
        OrderResponseDto result = orderService.createOrder(requestDto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getUserName()).isEqualTo("Unknown user name");
        verify(userServiceClient).getUserById(1L);
    }

    @Test
    void getOrderById_ShouldReturnOrderResponseDto_WhenOrderExists() {
        // Given
        Long orderId = 1L;
        Order order = new Order();
        order.setId(orderId);
        order.setUserId(1L);

        OrderResponseDto dto = new OrderResponseDto();
        dto.setId(orderId);
        dto.setUserId(1L);

        UserDto userDto = new UserDto(1L, "Ivan", "Ivanov", null, "ivan@example.com");

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderMapper.toResponseDto(order)).thenReturn(dto);
        when(userServiceClient.getUserById(1L)).thenReturn(userDto);

        // When
        OrderResponseDto result = orderService.getOrderById(orderId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(orderId);
        assertThat(result.getUserName()).isEqualTo("Ivan");
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
    void updateOrder_ShouldReturnUpdatedOrderResponseDto_WhenOrderExists() {
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

        OrderResponseDto resultDto = new OrderResponseDto();
        resultDto.setId(orderId);
        resultDto.setUserId(1L);
        resultDto.setStatus(OrderStatus.CONFIRMED);

        UserDto userDto = new UserDto(1L, "Ivan", "Ivanov", null, "ivan@example.com");

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(updatedOrder);
        when(orderMapper.toResponseDto(updatedOrder)).thenReturn(resultDto);
        when(userServiceClient.getUserById(1L)).thenReturn(userDto);

        // When
        OrderResponseDto result = orderService.updateOrder(orderId, updateDto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
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
    void getOrdersByIds_ShouldReturnOrderResponseDtos_WhenOrdersExist() {
        // Given
        List<Long> ids = Arrays.asList(1L, 2L);
        Order order1 = new Order();
        order1.setId(1L);
        order1.setUserId(1L);
        Order order2 = new Order();
        order2.setId(2L);
        order2.setUserId(1L);
        List<Order> orders = Arrays.asList(order1, order2);

        OrderResponseDto dto1 = new OrderResponseDto();
        dto1.setId(1L);
        dto1.setUserId(1L);
        OrderResponseDto dto2 = new OrderResponseDto();
        dto2.setId(2L);
        dto2.setUserId(1L);

        UserDto userDto = new UserDto(1L, "Ivan", "Ivanov", null, "ivan@example.com");

        when(orderRepository.findByIdIn(ids)).thenReturn(orders);
        when(orderMapper.toResponseDto(order1)).thenReturn(dto1);
        when(orderMapper.toResponseDto(order2)).thenReturn(dto2);
        when(userServiceClient.getUserById(eq(1L))).thenReturn(userDto);

        // When
        List<OrderResponseDto> result = orderService.getOrdersByIds(ids);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getUserName()).isEqualTo("Ivan");
        verify(userServiceClient, times(2)).getUserById(1L);
    }

    @Test
    void getOrdersByStatuses_ShouldReturnOrderResponseDtos_WhenOrdersExist() {
        // Given
        List<OrderStatus> statuses = Collections.singletonList(OrderStatus.CREATED);
        Order order = new Order();
        order.setId(1L);
        order.setUserId(1L);
        order.setStatus(OrderStatus.CREATED);
        List<Order> orders = Collections.singletonList(order);

        OrderResponseDto dto = new OrderResponseDto();
        dto.setId(1L);
        dto.setUserId(1L);
        dto.setStatus(OrderStatus.CREATED);

        UserDto userDto = new UserDto(1L, "Ivan", "Ivanov", null, "ivan@example.com");

        when(orderRepository.findByStatusIn(statuses)).thenReturn(orders);
        when(orderMapper.toResponseDto(order)).thenReturn(dto);
        when(userServiceClient.getUserById(1L)).thenReturn(userDto);

        // When
        List<OrderResponseDto> result = orderService.getOrdersByStatuses(statuses);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUserName()).isEqualTo("Ivan");
        verify(userServiceClient).getUserById(1L);
    }

    @Test
    void getOrdersByUserId_ShouldReturnPagedOrders_WhenUserServiceAvailable() {
        // Given
        Long userId = 1L;
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

        OrderResponseDto dto1 = new OrderResponseDto();
        dto1.setId(100L);
        dto1.setUserId(userId);
        dto1.setStatus(OrderStatus.CREATED);

        OrderResponseDto dto2 = new OrderResponseDto();
        dto2.setId(101L);
        dto2.setUserId(userId);
        dto2.setStatus(OrderStatus.SHIPPED);

        UserDto userDto = new UserDto(1L, "Ivan", "Ivanov", null, "ivan@example.com");

        when(orderRepository.findByUserId(eq(userId), any(Pageable.class))).thenReturn(ordersPage);
        when(orderMapper.toResponseDto(order1)).thenReturn(dto1);
        when(orderMapper.toResponseDto(order2)).thenReturn(dto2);
        when(userServiceClient.getUserById(userId)).thenReturn(userDto);

        // When
        Page<OrderResponseDto> result = orderService.getOrdersByUserId(userId, 0, 10);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0).getUserName()).isEqualTo("Ivan");
        verify(userServiceClient, times(2)).getUserById(userId);
    }

    @Test
    void isOwner_ShouldReturnTrue_WhenUserOwnsOrder() {
        // Given
        Long userId = 1L;
        Long orderId = 100L;
        when(orderRepository.existsByIdAndUserId(orderId, userId)).thenReturn(true);

        // When
        boolean result = orderService.isOwner(orderId, userId);

        // Then
        assertThat(result).isTrue();
        verify(orderRepository).existsByIdAndUserId(orderId, userId);
    }

    @Test
    void isOwner_ShouldReturnFalse_WhenUserDoesNotOwnOrder() {
        // Given
        Long userId = 1L;
        Long orderId = 100L;
        when(orderRepository.existsByIdAndUserId(orderId, userId)).thenReturn(false);

        // When
        boolean result = orderService.isOwner(orderId, userId);

        // Then
        assertThat(result).isFalse();
        verify(orderRepository).existsByIdAndUserId(orderId, userId);
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

        OrderResponseDto dto1 = new OrderResponseDto();
        dto1.setId(100L);
        dto1.setUserId(userId);
        OrderResponseDto dto2 = new OrderResponseDto();
        dto2.setId(101L);
        dto2.setUserId(userId);

        when(orderRepository.findByIdIn(ids)).thenReturn(orders);
        when(orderMapper.toResponseDto(order1)).thenReturn(dto1);
        when(orderMapper.toResponseDto(order2)).thenReturn(dto2);

        // When
        List<OrderResponseDto> result = orderService.getOrdersByIdsForUser(ids, userId);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).extracting(OrderResponseDto::getUserId)
                .containsOnly(userId);
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
        order2.setUserId(2L); // Чужой заказ!
        List<Order> orders = Arrays.asList(order1, order2);

        when(orderRepository.findByIdIn(ids)).thenReturn(orders);

        // When / Then
        AccessDeniedException exception = assertThrows(
                AccessDeniedException.class,
                () -> orderService.getOrdersByIdsForUser(ids, userId)
        );

        assertThat(exception.getMessage()).contains("You can only access your own orders");
        verify(orderMapper, never()).toResponseDto(any(Order.class));
    }
}