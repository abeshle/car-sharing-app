package com.example.carsharingapp.exceptions;

public class NotificationSendException extends RuntimeException {
    public NotificationSendException(String message) {
        super(message);
    }
}
