package com.example.carsharingapp.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserLoginRequestDto(
        @NotBlank
        @Size(max = 255)
        String email,

        @NotBlank
        @Size(max = 255)
        String password
) {
}
