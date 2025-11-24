package com.example.carsharingapp.dto.car;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class UpdateCarRequestDto {

    @NotBlank(message = "Model is required")
    private String model;

    @NotBlank(message = "Brand is required")
    private String brand;

    @NotBlank(message = "Type is required")
    private String type;

    @NotNull(message = "Inventory is required")
    @Min(value = 0, message = "Inventory must be zero or greater")
    private Integer inventory;

    @NotNull(message = "Price per day is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price per day must be greater than 0")
    private Double pricePerDay;
}
