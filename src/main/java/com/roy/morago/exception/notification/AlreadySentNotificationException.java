package com.roy.morago.exception.notification;

public class AlreadySentNotificationException extends RuntimeException {
    public AlreadySentNotificationException(String message) {
        super(message);
    }
}
