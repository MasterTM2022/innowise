package com.innowise.payment.mapper;

import com.innowise.payment.dto.PaymentCreateDto;
import com.innowise.payment.dto.PaymentResponseDto;
import com.innowise.payment.entity.Payment;
import com.innowise.payment.entity.PaymentStatus;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import java.math.BigDecimal;

@Mapper(componentModel = "spring")
public interface PaymentMapper {

    // Из DTO → Entity
    Payment toEntity(PaymentCreateDto dto);

    // Из Entity → Response DTO
    @Mapping(target = "status", source = "status", qualifiedByName = "statusToString")
    PaymentResponseDto toDto(Payment entity);

    @Named("statusToString")
    default String mapStatusToString(PaymentStatus status) {
        return status != null ? status.name() : null;
    }
}