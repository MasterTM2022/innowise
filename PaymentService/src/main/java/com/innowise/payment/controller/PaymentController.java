package com.innowise.payment.controller;

import com.innowise.payment.dto.PaymentCreateDto;
import com.innowise.payment.dto.PaymentResponseDto;
import com.innowise.payment.entity.Payment;
import com.innowise.payment.entity.PaymentStatus;
import com.innowise.payment.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    public ResponseEntity<PaymentResponseDto> create(@Valid @RequestBody PaymentCreateDto dto) {
        PaymentResponseDto result = paymentService.createPayment(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @GetMapping("/order/{orderId}")
    public List<Payment> byOrder(@PathVariable Long orderId) {
        return paymentService.getPaymentsByOrderId(orderId);
    }

    @GetMapping("/user/{userId}")
    public List<Payment> byUser(@PathVariable Long userId) {
        return paymentService.getPaymentsByUserId(userId);
    }

    @GetMapping("/status")
    public List<Payment> byStatus(@RequestParam List<String> statuses) {
        List<PaymentStatus> enums = statuses.stream()
                .map(PaymentStatus::valueOf)
                .toList();
        return paymentService.getPaymentsByStatuses(enums);
    }

    @GetMapping("/total")
    public BigDecimal totalSum(
            @RequestParam String start,
            @RequestParam String end) {
        Instant s = Instant.parse(start);
        Instant e = Instant.parse(end);
        return paymentService.getTotalSumForPeriod(s, e);
    }
}