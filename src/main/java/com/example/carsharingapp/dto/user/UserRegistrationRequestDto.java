package com.example.carsharingapp.dto.user;

import com.example.carsharingapp.validation.FieldMatch;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
@FieldMatch(first = "password", second = "repeatPassword", message = "Passwords do not match")
public class UserRegistrationRequestDto {
    @NotBlank
    @Email
    @Size(max = 255, message = "Email must be at most 255 characters long")
    private String email;
    @NotBlank
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[A-Za-z\\d]{8,}$",
            message = "Password must contain at least one uppercase letter, "
                    + "one lowercase letter, and one digit, "
                    + "with a minimum length of 8 characters."
    )
    @Size(max = 255, message = "Password must be at most 255 characters long")
    private String password;
    @NotBlank
    @Size(max = 255, message = "Password must be at most 255 characters long")
    private String repeatPassword;
    @NotBlank
    @Length(min = 2, max = 255, message = "Please enter the correct name")
    private String firstName;
    @NotBlank
    @Length(min = 2, max = 255, message = "Please enter the correct last name")
    private String lastName;
}
