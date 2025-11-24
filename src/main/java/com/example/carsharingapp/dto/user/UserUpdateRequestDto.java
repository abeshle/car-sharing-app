package com.example.carsharingapp.dto.user;

import lombok.Data;

@Data
public class UserUpdateRequestDto {
    private String email;
    private String firstName;
    private String lastName;
    private String oldPassword;
    private String newPassword;
}
