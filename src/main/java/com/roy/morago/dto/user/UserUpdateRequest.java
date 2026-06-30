package com.roy.morago.dto.user;

import com.roy.morago.enums.Availability;
import com.roy.morago.enums.TopikLevel;
import com.roy.morago.enums.UserStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record UserUpdateRequest(
        Set<String> languages,

        @NotBlank
        @Size(max = 30, message = "First name cannot exceed 30 characters")
        String firstName,

        @NotBlank
        @Size(max = 30, message = "Last name cannot exceed 30 characters")
        String lastName,

        @Pattern(
                regexp = "^(\\d{3}-\\d{4}-\\d{4})$",
                message = "Phone number must be in format: 000-0000-0000"
        )
        @NotBlank
        String phone,

        @NotNull
        Availability availability,

        UserStatus status,

        TopikLevel topikLevel
) {
}
