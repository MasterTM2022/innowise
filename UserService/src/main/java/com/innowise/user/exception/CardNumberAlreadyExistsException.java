package com.innowise.user.exception;

public class CardNumberAlreadyExistsException extends RuntimeException {
    public CardNumberAlreadyExistsException(String message) {
        super(message);
    }
}
