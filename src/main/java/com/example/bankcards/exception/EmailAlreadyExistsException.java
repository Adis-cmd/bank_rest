package com.example.bankcards.exception;

public class EmailAlreadyExistsException extends RuntimeException {
    public EmailAlreadyExistsException(String email) {
        super("Email уже занят: " + email);
    }
}
