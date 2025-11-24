package com.example.carsharingapp.controller;

import com.example.carsharingapp.dto.car.CarRequestDto;
import com.example.carsharingapp.dto.car.CarResponseDto;
import com.example.carsharingapp.dto.car.UpdateCarRequestDto;
import com.example.carsharingapp.service.CarService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Car management", description = "Endpoints for managing cars inventory")
@RequiredArgsConstructor
@RestController
@RequestMapping("/cars")
@Validated
public class CarsController {
    private final CarService carService;

    @PreAuthorize("hasRole('ROLE_CUSTOMER')")
    @GetMapping
    @Operation(summary = "Get all cars",
            description = "Retrieve paginated list of available cars")
    public Page<CarResponseDto> getAll(Pageable pageable) {
        return carService.findAll(pageable);
    }

    @PreAuthorize("hasRole('ROLE_CUSTOMER')")
    @GetMapping("/{id}")
    @Operation(summary = "Get car by ID",
            description = "Retrieve detailed information about a specific car")
    public CarResponseDto getById(@PathVariable Long id) {
        return carService.getById(id);
    }

    @PreAuthorize("hasRole('ROLE_MANAGER')")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    @Operation(summary = "Create a new car", description = "Add a new car to inventory")
    public CarResponseDto createCar(@Valid @RequestBody CarRequestDto requestDto) {
        return carService.create(requestDto);
    }

    @PreAuthorize("hasRole('ROLE_MANAGER')")
    @PutMapping("/{id}")
    @Operation(summary = "Update car", description = "Update all fields of existing car")
    public CarResponseDto updateCar(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCarRequestDto requestDto
    ) {
        return carService.update(id, requestDto);
    }

    @PreAuthorize("hasRole('ROLE_MANAGER')")
    @PatchMapping("/{id}")
    @Operation(summary = "Partially update car",
            description = "Update only provided fields of a car")
    public CarResponseDto partialUpdateCar(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCarRequestDto updateDto
    ) {
        return carService.update(id, updateDto);
    }

    @PreAuthorize("hasRole('ROLE_MANAGER')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete car", description = "Soft delete a car by ID")
    public void deleteCar(@PathVariable Long id) {
        carService.delete(id);
    }
}
