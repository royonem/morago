package com.roy.morago.exception;

public class InvalidWithdrawalStateException extends RuntimeException {
    public InvalidWithdrawalStateException(String message) {
        super(message);
    }
}
