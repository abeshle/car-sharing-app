package com.example.carsharingapp.controller;

import com.example.carsharingapp.dto.rental.CreateRentalRequestDto;
import com.example.carsharingapp.dto.rental.RentalResponseDto;
import com.example.carsharingapp.model.User;
import com.example.carsharingapp.service.RentalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Rentals Management", description = "Endpoints for managing rentals")
@RequiredArgsConstructor
@RestController
@RequestMapping("/rentals")
public class RentalsController {

    private final RentalService rentalService;

    @PreAuthorize("hasRole('ROLE_CUSTOMER') or hasRole('ROLE_MANAGER')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a rental",
            description =
                    "Create a new rental for the authenticated user. Decreases car inventory by 1.")
    public RentalResponseDto createRental(Authentication authentication,
                                          @RequestBody @Valid CreateRentalRequestDto requestDto) {
        User user = (User) authentication.getPrincipal();
        return rentalService.createRental(user, requestDto);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping
    @Operation(summary = "Get rentals",
            description = """
                For regular users: returns only user's rentals.
                For admins: can return all rentals or by userId if provided.
                """)
    public Page<RentalResponseDto> getRentals(
            @RequestParam Boolean active,
            @RequestParam(required = false) Long userId,
            Authentication authentication,
            Pageable pageable
    ) {
        User user = (User) authentication.getPrincipal();

        boolean isAdmin = user.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_MANAGER"));

        if (!isAdmin) {
            return rentalService.getUserRentals(user.getId(), active, pageable);
        }

        return rentalService.getAllRentals(active, userId, pageable);
    }

    @Operation(summary = "Get rental by ID",
            description = "Retrieve a specific rental if it belongs to the authenticated user.")
    @PreAuthorize("hasRole('ROLE_CUSTOMER') or hasRole('ROLE_MANAGER')")
    @GetMapping("/{id}")
    public RentalResponseDto getRentalById(@PathVariable Long id,
                                           Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return rentalService.getById(id, user.getId());
    }

    @PreAuthorize("hasRole('ROLE_CUSTOMER') or hasRole('ROLE_MANAGER')")
    @PostMapping("/{id}/return")
    @Operation(summary = "Return a rented car",
            description =
                    "Marks rental as finished and increases car inventory by 1.")
    public RentalResponseDto returnCar(@PathVariable Long id,
                                       Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return rentalService.returnCar(id, user.getId());
    }
}
