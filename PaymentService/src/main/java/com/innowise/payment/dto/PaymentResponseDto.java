package com.innowise.payment.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Setter
@Getter
@NoArgsConstructor
public class PaymentResponseDto {
    private Long id;
    private Long orderId;
    private Long userId;
    private String status;
    private Instant timestamp;
    private BigDecimal paymentAmount;

}
