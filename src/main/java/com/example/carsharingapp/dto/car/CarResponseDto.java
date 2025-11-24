package com.example.carsharingapp.dto.car;

import com.example.carsharingapp.model.CarType;
import java.math.BigDecimal;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors
public class CarResponseDto {
    private Long id;
    private String model;
    private String brand;
    private CarType type;
    private Integer inventory;
    private BigDecimal dailyFee;
}
