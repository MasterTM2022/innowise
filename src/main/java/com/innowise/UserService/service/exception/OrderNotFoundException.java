package com.innowise.UserService.service.exception;

public class OrderNotFoundException extends RuntimeException{
    public OrderNotFoundException (String message) {
        super(message);
    }
}
