package com.innowise.UserService.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
public class CardDto implements Serializable {

    private Long id;
    private Long userId;
    @NotBlank(message = "Number is required")
    private String number;
    @NotBlank(message = "Cardholder is required")
    private String holder;
    @NotNull(message = "Expiration date is required")
    @Future
    private LocalDate expirationDate;
    private String userEmail;


    public CardDto(Long id, Long userId, String number, String holder, LocalDate expirationDate) {
        this.id = id;
        this.userId = userId;
        this.number = number;
        this.holder = holder;
        this.expirationDate = expirationDate;
    }
}
