package com.roy.morago.exception.finance;

public class NonActiveWalletException extends RuntimeException {
    public NonActiveWalletException(String message) {
        super(message);
    }
}
