package com.innowise.order.controllers;

import com.innowise.order.dto.OrderDtoCreate;
import com.innowise.order.dto.OrderDtoUpdate;
import com.innowise.order.dto.OrderResponseDto;
import com.innowise.order.entity.OrderStatus;
import com.innowise.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderResponseDto> createOrder(
            @Valid @RequestBody OrderDtoCreate orderDtoCreate,
            Authentication authentication) {

        JwtClaims jwtClaims = extractClaims(authentication);

        if (!jwtClaims.userId.equals(orderDtoCreate.getUserId())) {
            throw new AccessDeniedException("Cannot create order for another user");
        }

        OrderResponseDto createdOrder = orderService.createOrder(orderDtoCreate);

        return ResponseEntity.status(HttpStatus.CREATED).body(createdOrder);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponseDto> getOrderById(
            @PathVariable Long id,
            Authentication authentication) {

        JwtClaims jwtClaims = extractClaims(authentication);

        if (!orderService.isOwner(id, jwtClaims.userId) && !"ADMIN".equals(jwtClaims.role)) {
            throw new AccessDeniedException(String.format("Access denied: order #%d does not belong to user %d", id, jwtClaims.userId));
        }

        OrderResponseDto order = orderService.getOrderById(id);
        return ResponseEntity.status(HttpStatus.OK).body(order);
    }

    @GetMapping("/batch")
    public ResponseEntity<List<OrderResponseDto>> getOrdersByIds(
            @RequestParam List<Long> ids,
            Authentication authentication) {

        JwtClaims jwtClaims = extractClaims(authentication);

        Long currentUserId = jwtClaims.userId;
        List<OrderResponseDto> orders = orderService.getOrdersByIdsForUser(ids, currentUserId);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/status")
    public ResponseEntity<List<OrderResponseDto>> getOrdersByStatuses(
            @RequestParam List<String> statuses,
            Authentication authentication) {

        JwtClaims jwtClaims = extractClaims(authentication);

        if (!"ADMIN".equals(jwtClaims.role)) {
            throw new AccessDeniedException("Access denied for non-admin user");
        }

        List<OrderStatus> orderStatuses = statuses.stream()
                .map(OrderStatus::valueOf)
                .collect(Collectors.toList());
        List<OrderResponseDto> orders = orderService.getOrdersByStatuses(orderStatuses);
        return ResponseEntity.status(HttpStatus.OK).body(orders);
    }

    @PutMapping("/{id}")
    public ResponseEntity<OrderResponseDto> updateOrder(
            @PathVariable Long id,
            @Valid @RequestBody OrderDtoUpdate request,
            Authentication authentication) {

        JwtClaims jwtClaims = extractClaims(authentication);

        if (!orderService.isOwner(id, jwtClaims.userId) && !"ADMIN".equals(jwtClaims.role)) {
            throw new AccessDeniedException("Cannot change order for another user");
        }

        OrderResponseDto updatedOrder = orderService.updateOrder(id, request);
        return ResponseEntity.status(HttpStatus.OK).body(updatedOrder);
    }

    @GetMapping
    public ResponseEntity<Page<OrderResponseDto>> getMyOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {

        JwtClaims jwtClaims = extractClaims(authentication);

        Long currentUserId = jwtClaims.userId;
        Page<OrderResponseDto> orders = orderService.getOrdersByUserId(currentUserId, page, size);
        return ResponseEntity.ok(orders);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrder(
            @PathVariable Long id,
            Authentication authentication) {

        JwtClaims jwtClaims = extractClaims(authentication);

        if (!orderService.isOwner(id, jwtClaims.userId) && !"ADMIN".equals(jwtClaims.role)) {
            throw new AccessDeniedException("Cannot delete order for another user");
        }

        orderService.deleteOrder(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    private JwtClaims extractClaims(Authentication authentication) {
        if (!(authentication.getPrincipal() instanceof Jwt jwt)) {
            throw new IllegalStateException("Invalid authentication principal");
        }

        String role = jwt.getClaim("role");
        Long userId = jwt.getClaim("userId");

        if (role == null || userId == null) {
            throw new IllegalStateException("JWT missing required claims: role or userId");
        }

        return new JwtClaims(role, userId);
    }

    private record JwtClaims(String role, Long userId) {
    }
}