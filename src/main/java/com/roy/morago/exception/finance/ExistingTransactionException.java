package com.roy.morago.exception.finance;

public class ExistingTransactionException extends RuntimeException {
    public ExistingTransactionException(String message) {
        super(message);
    }
}
