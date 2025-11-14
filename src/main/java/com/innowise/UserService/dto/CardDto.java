package com.innowise.UserService.dto;

import com.innowise.UserService.entity.User;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public class CardDto {

    private Long id;
    private User user;
    @NotBlank(message = "Number is required")
    private String number;
    @NotBlank(message = "Cardholder is required")
    private String holder;
    @NotNull(message = "Expiration date is required")
    @Future
    private LocalDate expirationDate;

    public CardDto(Long id, User user, String number, String holder, LocalDate expirationDate) {
        this.id = id;
        this.user = user;
        this.number = number;
        this.holder = holder;
        this.expirationDate = expirationDate;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getHolder() {
        return holder;
    }

    public void setHolder(String holder) {
        this.holder = holder;
    }

    public LocalDate getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(LocalDate expirationDate) {
        this.expirationDate = expirationDate;
    }
}
