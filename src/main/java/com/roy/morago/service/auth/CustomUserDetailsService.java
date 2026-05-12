package com.roy.morago.service.auth;

import com.roy.morago.entity.user.User;
import com.roy.morago.exception.UserNotFoundException;
import com.roy.morago.repository.user.UserRepository;
import com.roy.morago.security.UserPrincipal;
import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found."));
        return new UserPrincipal(user);
    }
}
