package com.example.carsharingapp.exceptions;

public class PaymentProcessingException extends RuntimeException {

    public PaymentProcessingException(String message) {
        super(message);
    }

    public PaymentProcessingException(String message,Throwable ex) {
        super(message,ex);
    }
}
