package com.innowise.UserService.service;

import com.innowise.UserService.client.UserServiceClient;
import com.innowise.UserService.dto.OrderDtoCreate;
import com.innowise.UserService.dto.OrderDtoUpdate;
import com.innowise.UserService.dto.UserDto;
import com.innowise.UserService.entity.Order;
import com.innowise.UserService.entity.OrderItem;
import com.innowise.UserService.entity.OrderStatus;
import com.innowise.UserService.mapper.OrderMapper;
import com.innowise.UserService.repository.OrderRepository;
import com.innowise.UserService.service.exception.OrderNotFoundException;
import com.innowise.UserService.service.exception.UserNotFoundException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.jaxb.SpringDataJaxb;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final UserServiceClient userServiceClient;

    public boolean isOwner(Long orderId, Long userId) {
        return orderRepository.existsByIdAndUserId(orderId, userId);
    }

    // Create
    @CircuitBreaker(name = "userService", fallbackMethod = "getDefaultUser")
    public OrderDtoCreate createOrder(OrderDtoCreate dto) {
        UserDto userDto = userServiceClient.getUserById(dto.getUserId());

        Order order = orderMapper.toEntity(dto);
        Order savedOrder = orderRepository.save(order);

        OrderDtoCreate result = orderMapper.toDto(savedOrder);
        result.setUserInfo(userDto);
        return result;
    }

    // Get by ID
    @CircuitBreaker(name = "userService", fallbackMethod = "getDefaultUserInfoById")
    public OrderDtoCreate getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("Order not found"));

        UserDto userDto = userServiceClient.getUserById(order.getUserId());
        OrderDtoCreate result = orderMapper.toDto(order);
        result.setUserInfo(userDto);
        return result;
    }

    // Get by IDs
    @CircuitBreaker(name = "userService", fallbackMethod = "getDefaultUserInfoListForIds")
    public List<OrderDtoCreate> getOrdersByIds(List<Long> ids) {
        List<Order> orders = orderRepository.findByIdIn(ids);
        return orders.stream()
                .map(orderMapper::toDto)
                .map(dto -> {
                    UserDto user = userServiceClient.getUserById(dto.getUserId());
                    dto.setUserInfo(user);
                    return dto;
                })
                .collect(Collectors.toList());
    }

    // Get by statuses
    @CircuitBreaker(name = "userService", fallbackMethod = "getDefaultUserInfoListForStatuses")
    public List<OrderDtoCreate> getOrdersByStatuses(List<OrderStatus> statuses) {
        List<Order> orders = orderRepository.findByStatusIn(statuses);
        return orders.stream()
                .map(orderMapper::toDto)
                .map(dto -> {
                    UserDto user = userServiceClient.getUserById(dto.getUserId());
                    dto.setUserInfo(user);
                    return dto;
                })
                .collect(Collectors.toList());
    }

    // Get by user ID with pagination
    @CircuitBreaker(name = "userService", fallbackMethod = "getDefaultUserInfoListForUserId")
    public Page<OrderDtoCreate> getOrdersByUserId(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Order> ordersPage = orderRepository.findByUserId(userId, pageable);

        return ordersPage.map(order -> {
            if (order == null) {
                throw new IllegalStateException("Order is null in the page result");
            }

            OrderDtoCreate dto = orderMapper.toDtoWithoutItems(order);
            if (dto == null) { // ← строка 115
                throw new IllegalStateException("OrderMapper.toDtoWithoutItems returned null");
            }

            UserDto user = null;
            try {
                user = userServiceClient.getUserById(order.getUserId());
            } catch (Exception e) {
                user = new UserDto();
                user.setId(order.getUserId());
                user.setName("Unknown User");
                user.setSurname("N/A");
                user.setEmail("unknown@example.com");
            }

            dto.setUserInfo(user);
            return dto;
        });
    }

    // Update
    @Transactional
    public OrderDtoCreate updateOrder(Long id, OrderDtoUpdate request) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("Order not found"));

        order.setStatus(request.getStatus());

        if (request.getOrderItems() != null) {
            List<OrderItem> orderItems = request.getOrderItems().stream()
                    .map(orderMapper::toEntity)
                    .collect(Collectors.toList());

            for (OrderItem item : orderItems) {
                item.setOrder(order);
            }
            order.setOrderItems(orderItems);
        }

        Order savedOrder = orderRepository.save(order);
        OrderDtoCreate dto = orderMapper.toDto(savedOrder);
        dto.setUserInfo(userServiceClient.getUserById(order.getUserId()));
        return dto;
    }

    // Delete
    @Transactional
    public void deleteOrder(Long id) {
        if (!orderRepository.existsById(id)) {
            throw new OrderNotFoundException("Order not found");
        }
        orderRepository.deleteById(id);
    }

    // Private methods
    private void validateUserExists(Long userId) {
        try {
            userServiceClient.getUserById(userId);
        } catch (Exception e) {
            throw new UserNotFoundException("User not found: " + userId);
        }
    }

    public List<OrderDtoCreate> getOrdersByIdsForUser(List<Long> ids, Long userId) {
        List<Order> orders = orderRepository.findByIdIn(ids);

        for (Order order : orders) {
            if (!order.getUserId().equals(userId)) {
                throw new AccessDeniedException("You can only access your own orders");
            }
        }

        return orders.stream()
                .map(orderMapper::toDto)
                .map(this::enrichWithUserInfo)
                .collect(Collectors.toList());
    }

    private OrderDtoCreate enrichWithUserInfo(OrderDtoCreate orderDtoCreate) {
        try {
            UserDto userDto = userServiceClient.getUserById(orderDtoCreate.getUserId());
            orderDtoCreate.setUserInfo(userDto);
        } catch (Exception e) {
            UserDto fallbackUser = new UserDto();
            fallbackUser.setId(orderDtoCreate.getUserId());
            fallbackUser.setName("Unknown User");
            fallbackUser.setSurname("N/A");
            fallbackUser.setEmail("unknown@example.com");
            fallbackUser.setBirthDate(null);
            orderDtoCreate.setUserInfo(fallbackUser);
        }
        return orderDtoCreate;
    }

    // Fallback methods
    private OrderDtoCreate getDefaultUser(OrderDtoCreate dto, Exception ex) {

        Order order = orderMapper.toEntity(dto);
        Order savedOrder = orderRepository.save(order);

        UserDto fallbackUser = new UserDto();
        fallbackUser.setId(dto.getUserId());
        fallbackUser.setName("Unknown User");
        fallbackUser.setSurname("N/A");
        fallbackUser.setEmail("unknown@example.com");
        fallbackUser.setBirthDate(LocalDate.of(1900, 1, 1)); // или LocalDate.now() если нужно

        OrderDtoCreate result = orderMapper.toDto(savedOrder);
        result.setUserInfo(fallbackUser);

        return result;
    }

    private OrderDtoCreate getDefaultUserInfoById(Long id, Exception ex) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("Order not found"));

        OrderDtoCreate result = orderMapper.toDtoWithoutItems(order);

        UserDto fallbackUser = new UserDto();
        fallbackUser.setId(order.getUserId());
        fallbackUser.setName("Unknown User");
        fallbackUser.setSurname("N/A");
        fallbackUser.setBirthDate(LocalDate.of(1900, 1, 1));
        fallbackUser.setEmail("unknown@example.com");

        result.setUserInfo(fallbackUser);
        return result;
    }

    private List<OrderDtoCreate> getDefaultUserInfoListForIds(List<Long> ids, Exception ex) {
        List<Order> orders = orderRepository.findByIdIn(ids);
        return orders.stream()
                .map(order -> {
                    OrderDtoCreate dto = orderMapper.toDtoWithoutItems(order);

                    UserDto fallbackUser = new UserDto();
                    fallbackUser.setId(order.getUserId());
                    fallbackUser.setName("Unknown User");
                    fallbackUser.setSurname("N/A");
                    fallbackUser.setBirthDate(LocalDate.of(1900, 1, 1));
                    fallbackUser.setEmail("unknown@example.com");

                    dto.setUserInfo(fallbackUser);
                    return dto;
                })
                .collect(Collectors.toList());
    }

    private List<OrderDtoCreate> getDefaultUserInfoListForStatuses(List<OrderStatus> statuses, Exception ex) {
        List<Order> orders = orderRepository.findByStatusIn(statuses);
        return orders.stream()
                .map(order -> {
                    OrderDtoCreate dto = orderMapper.toDtoWithoutItems(order);

                    UserDto fallbackUser = new UserDto();
                    fallbackUser.setId(order.getUserId());
                    fallbackUser.setName("Unknown User");
                    fallbackUser.setSurname("N/A");
                    fallbackUser.setBirthDate(LocalDate.of(1900, 1, 1));
                    fallbackUser.setEmail("unknown@example.com");

                    dto.setUserInfo(fallbackUser);
                    return dto;
                })
                .collect(Collectors.toList());
    }

    private Page<OrderDtoCreate> getDefaultUserInfoListForUserId(Long userId, int page, int size, Exception ex) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Order> ordersPage = orderRepository.findByUserId(userId, pageable);

        return ordersPage.map(order -> {
            OrderDtoCreate dto = orderMapper.toDtoWithoutItems(order); // ← Новый метод без orderItems
            UserDto fallbackUser = new UserDto();
            fallbackUser.setId(order.getUserId());
            fallbackUser.setName("Unknown User");
            fallbackUser.setSurname("N/A");
            fallbackUser.setBirthDate(LocalDate.of(1900, 01, 01));
            fallbackUser.setEmail("unknown@example.com");
            dto.setUserInfo(fallbackUser);
            return dto;
        });
    }
}