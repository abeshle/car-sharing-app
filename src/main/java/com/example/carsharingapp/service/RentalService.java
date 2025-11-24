package com.example.carsharingapp.service;

import com.example.carsharingapp.dto.rental.CreateRentalRequestDto;
import com.example.carsharingapp.dto.rental.RentalResponseDto;
import com.example.carsharingapp.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface RentalService {
    RentalResponseDto createRental(User user, CreateRentalRequestDto requestDto);

    Page<RentalResponseDto> getUserRentals(Long userId, Boolean active, Pageable pageable);

    RentalResponseDto getById(Long rentalId, Long userId);

    RentalResponseDto returnCar(Long rentalId, Long userId);

    Page<RentalResponseDto> getAllRentals(Boolean active, Long userId, Pageable pageable);

}
