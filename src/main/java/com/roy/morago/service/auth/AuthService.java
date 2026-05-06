package com.roy.morago.service.auth;

import com.roy.morago.dto.auth.ClientRegisterRequest;
import com.roy.morago.dto.auth.LoginRequest;
import com.roy.morago.dto.auth.LoginResponse;
import com.roy.morago.entity.user.Role;
import com.roy.morago.entity.user.User;
import com.roy.morago.exception.DuplicateEmailException;
import com.roy.morago.exception.InvalidCredentialsException;
import com.roy.morago.exception.RoleNotFoundException;
import com.roy.morago.mapper.UserMapper;
import com.roy.morago.repository.user.RoleRepository;
import com.roy.morago.repository.user.UserRepository;
import com.roy.morago.security.JwtProvider;
import com.roy.morago.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;
    private final UserMapper userMapper;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public LoginResponse login(LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getPassword()
                    )
            );
            return createLoginResponse(authentication);
        } catch (AuthenticationException e) {
            throw new InvalidCredentialsException("Invalid username or password");
        }
    }

    private LoginResponse createLoginResponse(Authentication authentication) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        Set<String> roleNames = principal.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());
        String token = jwtProvider.generateToken(principal.getUsername(), roleNames);
        return new LoginResponse(token);
    }

    public void register(ClientRegisterRequest dto) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new DuplicateEmailException("Email already in use.");
        }
        User user = userMapper.createUserFromDto(dto);

        Role defaultRole = roleRepository.findByName("ROLE_CLIENT")
                .orElseThrow(() -> new RoleNotFoundException("Client role not found"));
        if (user.getRoles().isEmpty()) {
            user.getRoles().add(defaultRole);
        }
        user.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        userRepository.save(user);
    }
}
