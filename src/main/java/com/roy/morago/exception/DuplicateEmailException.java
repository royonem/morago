package com.roy.morago.exception;

public class DuplicateEmailException extends RuntimeException {
    public DuplicateEmailException() {
        super();
    }
    public DuplicateEmailException(String message) {
        super(message);
    }
    public DuplicateEmailException(String message, Throwable cause) {
        super(message, cause);
    }
}
