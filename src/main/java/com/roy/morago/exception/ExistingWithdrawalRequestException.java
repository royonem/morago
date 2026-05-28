package com.roy.morago.exception;

public class ExistingWithdrawalRequestException extends RuntimeException {
    public ExistingWithdrawalRequestException(String message) {
        super(message);
    }
}
