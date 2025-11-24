package com.example.carsharingapp.dto.rental;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors
public class CreateRentalRequestDto {
    @NotNull
    private Long carId;

    @NotNull
    private LocalDate returnDate;
}
