package com.innowise.UserService.controllers;

import com.innowise.UserService.dto.OrderDtoCreate;
import com.innowise.UserService.dto.OrderDtoUpdate;
import com.innowise.UserService.entity.OrderStatus;
import com.innowise.UserService.security.service.AppUserDetails;
import com.innowise.UserService.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<OrderDtoCreate> createOrder(@Valid @RequestBody OrderDtoCreate orderDtoCreate) {
        OrderDtoCreate createdOrder = orderService.createOrder(orderDtoCreate);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdOrder);
    }

    @GetMapping("/{id}")
    @PreAuthorize("@orderService.isOwner(#id, authentication.principal.id) or hasRole('ADMIN')")
    public ResponseEntity<OrderDtoCreate> getOrderById(@PathVariable Long id) {
        OrderDtoCreate order = orderService.getOrderById(id);
        return ResponseEntity.status(HttpStatus.OK).body(order);
    }

    @GetMapping("/batch")
    public ResponseEntity<List<OrderDtoCreate>> getOrdersByIds(
            @RequestParam List<Long> ids,
            @AuthenticationPrincipal AppUserDetails userDetails) {

        Long currentUserId = userDetails.getId();
        List<OrderDtoCreate> orders = orderService.getOrdersByIdsForUser(ids, currentUserId);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<OrderDtoCreate>> getOrdersByStatuses(@RequestParam List<String> statuses) {
        List<OrderStatus> orderStatuses = statuses.stream()
                .map(OrderStatus::valueOf)
                .collect(Collectors.toList());
        List<OrderDtoCreate> orders = orderService.getOrdersByStatuses(orderStatuses);
        return ResponseEntity.status(HttpStatus.OK).body(orders);
    }

    @PutMapping("/{id}")
    @PreAuthorize("@orderService.isOwner(#id, authentication.principal.id) or hasRole('ADMIN')")
    public ResponseEntity<OrderDtoCreate> updateOrder(
            @PathVariable Long id,
            @Valid @RequestBody OrderDtoUpdate request) {

        OrderDtoCreate updatedOrder = orderService.updateOrder(id, request);
        return ResponseEntity.status(HttpStatus.OK).body(updatedOrder);
    }

    @GetMapping
    public ResponseEntity<Page<OrderDtoCreate>> getMyOrders(
            @AuthenticationPrincipal AppUserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Long currentUserId = userDetails.getId();
        Page<OrderDtoCreate> orders = orderService.getOrdersByUserId(currentUserId, page, size);
        return ResponseEntity.ok(orders);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@orderService.isOwner(#id, authentication.principal.id) or hasRole('ADMIN')")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long id) {
        orderService.deleteOrder(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}