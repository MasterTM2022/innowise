package com.innowise.payment.service;

import com.innowise.payment.dto.PaymentCreateDto;
import com.innowise.payment.dto.PaymentResponseDto;
import com.innowise.payment.entity.Payment;
import com.innowise.payment.entity.PaymentStatus;
import com.innowise.payment.mapper.PaymentMapper;
import com.innowise.payment.repository.PaymentRepository;
import com.innowise.payment.util.RandomNumberClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Service
@Transactional
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;
    private final RandomNumberClient randomNumberClient;

    public PaymentService(PaymentRepository paymentRepository,
                          PaymentMapper paymentMapper,
                          RandomNumberClient randomNumberClient) {
        this.paymentRepository = paymentRepository;
        this.paymentMapper = paymentMapper;
        this.randomNumberClient = randomNumberClient;
    }

    public PaymentResponseDto createPayment(PaymentCreateDto dto) {
        int number = randomNumberClient.getRandomInt();
        PaymentStatus status = (number % 2 == 0) ? PaymentStatus.COMPLETED : PaymentStatus.FAILED;

        Payment payment = paymentMapper.toEntity(dto);
        payment.setStatus(status);
        payment.setTimestamp(Instant.now());

        Payment saved = paymentRepository.save(payment);

        return paymentMapper.toDto(saved);
    }

    public List<Payment> getPaymentsByOrderId(Long orderId) {
        return paymentRepository.findByOrderId(orderId);
    }

    public List<Payment> getPaymentsByUserId(Long userId) {
        return paymentRepository.findByUserId(userId);
    }

    public List<Payment> getPaymentsByStatuses(List<PaymentStatus> statuses) {
        return paymentRepository.findByStatusIn(statuses);
    }

    public BigDecimal getTotalSumForPeriod(Instant start, Instant end) {
        BigDecimal sum = paymentRepository.sumPaymentsInPeriod(start, end);
        return sum != null ? sum : BigDecimal.ZERO;
    }
}