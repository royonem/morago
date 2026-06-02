package com.roy.morago.exception.finance;

public class NonPositiveTransactionException extends RuntimeException {
    public NonPositiveTransactionException(String message) {
        super(message);
    }
}
