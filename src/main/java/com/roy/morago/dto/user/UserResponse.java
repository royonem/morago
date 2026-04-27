package com.roy.morago.dto.user;

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
public class UserResponse {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String profilePictureUrl;
    private Set<String> roles;
    private Set<String> languages;
    private String availability;
    private UserStatus status;
    private Integer topikLevel;
}
