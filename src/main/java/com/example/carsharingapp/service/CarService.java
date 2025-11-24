package com.example.carsharingapp.service;

import com.example.carsharingapp.dto.car.CarRequestDto;
import com.example.carsharingapp.dto.car.CarResponseDto;
import com.example.carsharingapp.dto.car.UpdateCarRequestDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CarService {
    CarResponseDto create(CarRequestDto requestDto);

    Page<CarResponseDto> findAll(Pageable pageable);

    CarResponseDto getById(Long id);

    CarResponseDto update(Long id, UpdateCarRequestDto requestDto);

    void delete(Long id);
}
