package com.roy.morago.service.auth;

import com.roy.morago.entity.user.User;
import com.roy.morago.exception.user.UserNotFoundException;
import com.roy.morago.repository.user.UserRepository;
import com.roy.morago.security.UserPrincipal;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Slf4j
@AllArgsConstructor
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) {
        log.info("Loading user by email: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("User not found with email: {}", email);
                    return new UserNotFoundException("User not found.");
                });
        log.info("User loaded successfully: {} (ID: {})", email, user.getId());
        return new UserPrincipal(user);
    }
}
