package com.example.bankcards.exception;

public class InvalidCardStatusException extends RuntimeException {
    public InvalidCardStatusException(String status) {
        super("Неверный статус карты: " + status + ". " +
                "Допустимые значения: ACTIVE, BLOCKED, EXPIRED, BLOCK_REQUESTED");
    }
}
