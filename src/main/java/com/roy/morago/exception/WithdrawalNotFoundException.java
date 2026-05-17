package com.roy.morago.exception;

public class WithdrawalNotFoundException extends RuntimeException {
    public WithdrawalNotFoundException(String message) {
        super(message);
    }
}
