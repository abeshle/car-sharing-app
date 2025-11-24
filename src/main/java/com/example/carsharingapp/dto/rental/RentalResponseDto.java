package com.example.carsharingapp.dto.rental;

import java.time.LocalDate;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors
public class RentalResponseDto {
    private Long id;
    private Long carId;
    private Long userId;
    private LocalDate rentalDate;
    private LocalDate returnDate;
    private LocalDate actualReturnDate;
    private boolean active;
}
