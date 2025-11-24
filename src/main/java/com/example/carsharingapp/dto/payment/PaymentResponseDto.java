package com.example.carsharingapp.dto.payment;

import com.example.carsharingapp.model.PaymentStatus;
import com.example.carsharingapp.model.PaymentType;
import java.math.BigDecimal;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors
public class PaymentResponseDto {
    private Long id;
    private PaymentStatus status;
    private PaymentType type;
    private Long rentalId;
    private BigDecimal amount;
    private String sessionUrl;
}
