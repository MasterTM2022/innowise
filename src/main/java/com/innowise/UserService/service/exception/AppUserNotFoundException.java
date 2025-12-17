package com.innowise.UserService.service.exception;

public class AppUserNotFoundException extends RuntimeException {
    public AppUserNotFoundException(String message) {super(message);}
}