package com.example.carsharingapp.service;

import com.example.carsharingapp.dto.user.UserRegistrationRequestDto;
import com.example.carsharingapp.dto.user.UserResponseDto;
import com.example.carsharingapp.dto.user.UserUpdateRequestDto;
import com.example.carsharingapp.exceptions.EntityNotFoundException;
import com.example.carsharingapp.exceptions.RegistrationException;
import com.example.carsharingapp.mapper.UserMapper;
import com.example.carsharingapp.model.Role;
import com.example.carsharingapp.model.User;
import com.example.carsharingapp.repository.user.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Transactional
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserResponseDto register(UserRegistrationRequestDto requestDto) {
        if (userRepository.existsByEmail(requestDto.getEmail())) {
            throw new RegistrationException("Can't register user");
        }

        User user = userMapper.toModel(requestDto);

        user.setPassword(passwordEncoder.encode(requestDto.getPassword()));

        user.setRole(Role.ROLE_CUSTOMER);

        User savedUser = userRepository.save(user);

        return userMapper.toDto(savedUser);
    }

    @Override
    public UserResponseDto updateUserRole(Long id, Role role) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        user.setRole(role);
        return userMapper.toDto(userRepository.save(user));
    }

    @Override
    public UserResponseDto getMyProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        return userMapper.toDto(user);
    }

    @Override
    public UserResponseDto updateMyProfile(String email, UserUpdateRequestDto requestDto) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        if (requestDto.getEmail() != null) {
            user.setEmail(requestDto.getEmail());
        }
        if (requestDto.getFirstName() != null) {
            user.setFirstName(requestDto.getFirstName());
        }
        if (requestDto.getLastName() != null) {
            user.setLastName(requestDto.getLastName());
        }
        if (requestDto.getNewPassword() != null) {
            if (!passwordEncoder.matches(requestDto.getOldPassword(), user.getPassword())) {
                throw new IllegalArgumentException("Old password is incorrect");
            }
            user.setPassword(passwordEncoder.encode(requestDto.getNewPassword()));
        }

        return userMapper.toDto(userRepository.save(user));
    }
}
