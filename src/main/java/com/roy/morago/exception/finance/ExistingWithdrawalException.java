package com.roy.morago.exception.finance;

public class ExistingWithdrawalException extends RuntimeException {
    public ExistingWithdrawalException(String message) {
        super(message);
    }
}
