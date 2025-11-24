package com.example.carsharingapp.dto.car;

import com.example.carsharingapp.model.CarType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class CarRequestDto {
    @NotBlank(message = "Model is required")
    private String model;

    @NotBlank(message = "Brand is required")
    private String brand;

    @NotNull(message = "Car type is required")
    private CarType type;

    @NotNull(message = "Inventory is required")
    @Min(value = 0, message = "Inventory must be zero or greater")
    private Integer inventory;

    @NotNull(message = "Daily fee is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Daily fee must be greater than 0")
    private BigDecimal dailyFee;
}
