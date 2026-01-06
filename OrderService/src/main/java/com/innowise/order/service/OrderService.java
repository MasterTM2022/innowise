package com.innowise.order.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.innowise.common.dto.UserDto;
import com.innowise.order.client.UserServiceClient;
import com.innowise.order.dto.OrderDtoCreate;
import com.innowise.order.dto.OrderDtoUpdate;
import com.innowise.order.dto.OrderResponseDto;
import com.innowise.order.dto.UserEnrichable;
import com.innowise.order.entity.Order;
import com.innowise.order.entity.OrderStatus;
import com.innowise.order.mapper.OrderMapper;
import com.innowise.order.repository.OrderRepository;
import com.innowise.order.service.exception.OrderNotFoundException;
import com.innowise.order.service.exception.UserNotFoundException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final UserServiceClient userServiceClient;
    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    public boolean isOwner(Long orderId, Long userId) {
        return orderRepository.existsByIdAndUserId(orderId, userId);
    }

    // Create
    @CircuitBreaker(name = "userService", fallbackMethod = "getDefaultUser")
    public OrderResponseDto createOrder(OrderDtoCreate dto) {

        Order order = orderMapper.toEntity(dto);
        Order savedOrder = orderRepository.save(order);

        OrderResponseDto orderResponseDto = orderMapper.toResponseDto(savedOrder);
        enrichWithUserInfo(orderResponseDto);
        return orderResponseDto;
    }

    // Get by ID
    @CircuitBreaker(name = "userService", fallbackMethod = "getDefaultUserInfoById")
    public OrderResponseDto getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("Order not found"));

        OrderResponseDto dto = orderMapper.toResponseDto(order);
        enrichWithUserInfo(dto);
        return dto;
    }

    // Get by IDs
    @CircuitBreaker(name = "userService", fallbackMethod = "getDefaultUserInfoListForIds")
    public List<OrderResponseDto> getOrdersByIds(List<Long> ids) {
        List<Order> orders = orderRepository.findByIdIn(ids);
        return orders.stream()
                .map(orderMapper::toResponseDto)
                .peek(this::enrichWithUserInfo)
                .collect(Collectors.toList());
    }

    // Get by statuses
    @CircuitBreaker(name = "userService", fallbackMethod = "getDefaultUserInfoListForStatuses")
    public List<OrderResponseDto> getOrdersByStatuses(List<OrderStatus> statuses) {
        List<Order> orders = orderRepository.findByStatusIn(statuses);
        return orders.stream()
                .map(orderMapper::toResponseDto)
                .peek(this::enrichWithUserInfo)
                .collect(Collectors.toList());
    }

    // Get by user ID with pagination
    @CircuitBreaker(name = "userService", fallbackMethod = "getDefaultUserInfoListForUserId")
    public Page<OrderResponseDto> getOrdersByUserId(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Order> ordersPage = orderRepository.findByUserId(userId, pageable);

        return ordersPage.map(order -> {
            OrderResponseDto dto = orderMapper.toResponseDto(order);
            enrichWithUserInfo(dto);
            return dto;
        });
    }

    // Update
    @Transactional
    @CircuitBreaker(name = "userService", fallbackMethod = "getDefaultUserForUpdate")
    public OrderResponseDto updateOrder(Long id, OrderDtoUpdate request) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("Order not found"));

        order.setStatus(request.getStatus());

        Order savedOrder = orderRepository.save(order);
        OrderResponseDto dto = orderMapper.toResponseDto(savedOrder);
        enrichWithUserInfo(dto);
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

    // Get by IDs for User
    @CircuitBreaker(name = "userService", fallbackMethod = "getDefaultUserInfoListForUser")
    public List<OrderResponseDto> getOrdersByIdsForUser(List<Long> ids, Long userId) {
        List<Order> orders = orderRepository.findByIdIn(ids);

        for (Order order : orders) {
            if (!order.getUserId().equals(userId)) {
                throw new AccessDeniedException("You can only access your own orders");
            }
        }

        return orders.stream()
                .map(orderMapper::toResponseDto)
//                .peek(this::enrichWithUserInfo)
                .collect(Collectors.toList());
    }

    private void enrichWithUserInfo(UserEnrichable dto) {
        try {
        UserDto userDto = userServiceClient.getUserById(dto.getUserId());
        dto.setUserName(userDto.getName());
        dto.setUserSurname(userDto.getSurname());
        } catch (Exception e) {
            log.warn("Failed to enrich user info for userId={}: {}", dto.getUserId(), e.getMessage());
            dto.setUserName("Unknown user name");
            dto.setUserSurname("Unknown user surname");
        }
    }

// Fallback methods

    private OrderResponseDto getDefaultUser(OrderDtoCreate dto, Exception ex) {
        Order order = orderMapper.toEntity(dto);
        Order savedOrder = orderRepository.save(order);

        OrderResponseDto result = orderMapper.toResponseDto(savedOrder);

        result.setUserName("Unknown user name");
        result.setUserSurname("Unknown user surname");
        return result;
    }

    private OrderResponseDto getDefaultUserInfoById(Long id, Throwable ex) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("Order not found"));
        OrderResponseDto dto = orderMapper.toResponseDto(order);
        dto.setUserName("Unknown user name");
        dto.setUserSurname("Unknown user surname");
        return dto;
    }

    private List<OrderResponseDto> getDefaultUserInfoListForIds(List<Long> ids, Exception ex) {
        List<Order> orders = orderRepository.findByIdIn(ids);
        return orders.stream()
                .map(orderMapper::toResponseDto)
                .peek(dto -> {
                    dto.setUserName("Unknown user name");
                    dto.setUserSurname("Unknown user surname");
                })
                .collect(Collectors.toList());
    }

    private List<OrderResponseDto> getDefaultUserInfoListForStatuses(List<OrderStatus> statuses, Exception ex) {
        List<Order> orders = orderRepository.findByStatusIn(statuses);
        return orders.stream()
                .map(orderMapper::toResponseDto)
                .peek(dto -> {
                    dto.setUserName("Unknown user name");
                    dto.setUserSurname("Unknown user surname");
                })
                .collect(Collectors.toList());
    }

    private Page<OrderResponseDto> getDefaultUserInfoListForUserId(Long userId, int page, int size, Exception ex) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Order> ordersPage = orderRepository.findByUserId(userId, pageable);

        Page<OrderResponseDto> responsePage = ordersPage.map(order -> {
            OrderResponseDto dto = orderMapper.toResponseDto(order);
            dto.setUserName("Unknown user name");
            dto.setUserSurname("Unknown user surname");
            return dto;
        });

        return responsePage;
    }
}