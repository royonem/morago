package com.roy.morago.exception.finance;

public class ExistingWithdrawalRequestException extends RuntimeException {
    public ExistingWithdrawalRequestException(String message) {
        super(message);
    }
}
