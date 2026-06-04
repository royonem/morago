package com.roy.morago.exception.finance;

public class WithdrawalNotFoundException extends RuntimeException {
    public WithdrawalNotFoundException(String message) {
        super(message);
    }
}
