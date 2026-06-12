package com.roy.morago.dto.user;

import com.roy.morago.enums.Availability;
import com.roy.morago.enums.TopikLevel;
import com.roy.morago.enums.UserStatus;

import java.util.Set;

public record UserResponse(
        Long id,
        String firstName,
        String lastName,
        String email,
        String profilePictureUrl,
        Set<String> roles,
        Set<String> languages,
        Availability availability,
        UserStatus status,
        TopikLevel topikLevel
) { }
