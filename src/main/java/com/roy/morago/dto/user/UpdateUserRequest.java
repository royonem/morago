package com.roy.morago.dto.user;

import com.roy.morago.enums.Availability;
import com.roy.morago.enums.TopikLevel;
import com.roy.morago.enums.UserStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.util.Set;

public record UpdateUserRequest(
        Set<String> languages,
        @NotBlank String firstName,
        @NotBlank String lastName,
        @Pattern(regexp = "^\\+?[0-9]{7,15}$",
                message = "Invalid phone number format")
        @NotBlank String phone,
        @NotNull Availability availability,
        UserStatus status,
        TopikLevel topikLevel
) {
}
