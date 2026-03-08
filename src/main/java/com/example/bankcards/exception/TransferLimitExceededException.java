package com.example.bankcards.exception;

public class TransferLimitExceededException extends RuntimeException {
  public TransferLimitExceededException(String message) {
    super(message);
  }
}
