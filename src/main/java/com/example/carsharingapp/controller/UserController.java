package com.example.carsharingapp.controller;

import com.example.carsharingapp.dto.user.UserResponseDto;
import com.example.carsharingapp.dto.user.UserUpdateRequestDto;
import com.example.carsharingapp.model.Role;
import com.example.carsharingapp.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Users", description = "Endpoints for managing users and profiles")
@RequiredArgsConstructor
@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    @Operation(summary = "Update user's role",
            description = "Update user's role")
    @PreAuthorize("hasRole('ROLE_MANAGER')")
    @PutMapping("/{id}/role")
    public UserResponseDto updateUserRole(
            @PathVariable Long id,
            @RequestParam Role role) {
        return userService.updateUserRole(id, role);
    }

    @Operation(summary = "Get profile info",
            description = "Get my profile information")
    @GetMapping("/me")
    public UserResponseDto getMyProfile(Authentication authentication) {
        return userService.getMyProfile(authentication.getName());
    }

    @Operation(summary = "Update profile",
            description = "Update my profile information")
    @PutMapping("/me")
    public UserResponseDto updateMyProfile(
            Authentication authentication,
            @RequestBody UserUpdateRequestDto requestDto) {
        return userService.updateMyProfile(authentication.getName(), requestDto);
    }
}
