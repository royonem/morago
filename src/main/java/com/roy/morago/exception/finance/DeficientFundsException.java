package com.roy.morago.exception.finance;

public class DeficientFundsException extends RuntimeException {
    public DeficientFundsException(String message) {
        super(message);
    }
}
