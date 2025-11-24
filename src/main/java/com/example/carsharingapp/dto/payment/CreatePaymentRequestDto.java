package com.example.carsharingapp.dto.payment;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors
public class CreatePaymentRequestDto {
    @NotNull(message = "Rental ID is required")
    private Long rentalId;
}
