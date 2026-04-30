package com.roy.morago.dto.user;

import com.roy.morago.enums.Availability;
import com.roy.morago.enums.TopikLevel;
import com.roy.morago.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRequest {
    private Set<String> languages;
    private String profilePictureUrl;
    private String firstName;
    private String lastName;
    private String phone;
    private Availability availability;
    private UserStatus status; // translators only (verified or non verified)
    private TopikLevel topikLevel;
}
