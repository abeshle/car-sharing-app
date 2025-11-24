package com.example.carsharingapp.service;

import com.example.carsharingapp.dto.user.UserRegistrationRequestDto;
import com.example.carsharingapp.dto.user.UserResponseDto;
import com.example.carsharingapp.dto.user.UserUpdateRequestDto;
import com.example.carsharingapp.exceptions.RegistrationException;
import com.example.carsharingapp.model.Role;

public interface UserService {
    UserResponseDto register(UserRegistrationRequestDto requestDto) throws RegistrationException;

    UserResponseDto updateUserRole(Long id, Role role);

    UserResponseDto getMyProfile(String email);

    UserResponseDto updateMyProfile(String email, UserUpdateRequestDto requestDto);
}
