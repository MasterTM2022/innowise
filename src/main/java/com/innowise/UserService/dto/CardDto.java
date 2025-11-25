package com.innowise.UserService.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDate;

@Getter
@Setter
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

//    public String getUserEmail() {
//        return userEmail;
//    }
//    public void setUserEmail(String userEmail) {
//        this.userEmail = userEmail;
//    }
//    public Long getId() {
//        return id;
//    }
//
//    public void setId(Long id) {
//        this.id = id;
//    }
//
//    public Long getUserId() {
//        return userId;
//    }
//
//    public void setUserId(Long userId) {
//        this.userId = userId;
//    }
//
//    public String getNumber() {
//        return number;
//    }
//
//    public void setNumber(String number) {
//        this.number = number;
//    }
//
//    public String getHolder() {
//        return holder;
//    }
//
//    public void setHolder(String holder) {
//        this.holder = holder;
//    }
//
//    public LocalDate getExpirationDate() {
//        return expirationDate;
//    }
//
//    public void setExpirationDate(LocalDate expirationDate) {
//        this.expirationDate = expirationDate;
//    }
}
