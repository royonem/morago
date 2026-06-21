package com.roy.morago.exception.user;

public class MissingRoleException extends RuntimeException {
    public MissingRoleException(String message) {
        super(message);
    }
}
