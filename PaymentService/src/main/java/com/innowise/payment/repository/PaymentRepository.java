package com.innowise.payment.repository;

import com.innowise.payment.entity.Payment;
import com.innowise.payment.entity.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findByOrderId(Long orderId);

    List<Payment> findByUserId(Long userId);

    List<Payment> findByStatusIn(List<PaymentStatus> statuses);

    @Query("SELECT SUM(p.paymentAmount) FROM Payment p " +
            "WHERE p.timestamp BETWEEN :start AND :end")
    BigDecimal sumPaymentsInPeriod(@Param("start") Instant start, @Param("end") Instant end);
}