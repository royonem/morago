package com.roy.morago.exception.finance;

public class InvalidWithdrawalStateException extends RuntimeException {
    public InvalidWithdrawalStateException(String message) {
        super(message);
    }
}
