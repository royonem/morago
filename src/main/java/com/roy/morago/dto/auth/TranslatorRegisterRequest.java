package com.roy.morago.dto.auth;

import com.roy.morago.enums.TopikLevel;
import jakarta.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public record TranslatorRegisterRequest(
        @NotBlank(message = "First name is required")
        @Size(max = 30, message = "First name cannot exceed 30 characters")
        String firstName,

        @NotBlank(message = "Last name is required")
        @Size(max = 30, message = "Last name cannot exceed 30 characters")
        String lastName,

        @NotBlank(message = "Password is required")
        @Size(min = 8, max = 50, message = "Password must be between 8 and 50 characters")
        @Pattern(
                regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$",
                message = "Password must contain at least one letter and one number"
        )
        String password,

        @NotBlank(message = "Please confirm your password")
        String confirmPassword,

        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        String email,

        @NotBlank(message = "Phone number is required")
        @Pattern(
                regexp = "^(\\d{3}-\\d{4}-\\d{4})$",
                message = "Phone number must be in format: 000-0000-0000"
        )
        String phone,

        @NotNull(message = "TOPIK level is required")
        TopikLevel topikLevel,

        @NotNull(message = "Birthdate is required")
        @DateTimeFormat(pattern = "yyyy-MM-dd")
        LocalDate birthdate
) implements RegisterRequest {
}