package com.roy.morago.dto.user;

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
    private Set<String> topics;
    private String profilePictureUrl;
    private String firstName;
    private String lastName;
    private String phone;
    private String availability;
    private String status; // translators only (verified or non verified)
    private Integer topikLevel;
}
