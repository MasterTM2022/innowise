package com.innowise.order.service.exception;

public class OrderNotFoundException extends RuntimeException{
    public OrderNotFoundException (String message) {
        super(message);
    }
}
