package com.example.carsharingapp.dto.payment;

import com.example.carsharingapp.model.PaymentStatus;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors
public class CancelPaymentResponseDto {
    private boolean success;
    private String message;
    private Long paymentId;
    private PaymentStatus status;
}
