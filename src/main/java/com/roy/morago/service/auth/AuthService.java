package com.roy.morago.service.auth;

import com.roy.morago.dto.auth.RegisterClientRequest;
import com.roy.morago.dto.auth.LoginRequest;
import com.roy.morago.dto.auth.LoginResponse;
import com.roy.morago.dto.auth.RegisterTranslatorRequest;
import com.roy.morago.entity.user.Role;
import com.roy.morago.entity.user.User;
import com.roy.morago.enums.CurrencyCode;
import com.roy.morago.exception.auth.InvalidCredentialsException;
import com.roy.morago.exception.auth.DuplicateEmailException;
import com.roy.morago.exception.auth.PasswordMismatchException;
import com.roy.morago.mapper.UserMapper;
import com.roy.morago.repository.user.UserRepository;
import com.roy.morago.security.JwtProvider;
import com.roy.morago.security.UserPrincipal;
import com.roy.morago.service.finance.WalletService;
import com.roy.morago.service.user.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final WalletService walletService;
    private final RoleService roleService;

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

    @Transactional
    public void registerClient(RegisterClientRequest dto) {
        User client = userMapper.createUserFromDto(dto);
        register(client, dto.getPassword(), dto.getConfirmPassword(), roleService.getClientRole());
    }

    @Transactional
    public void registerTranslator(RegisterTranslatorRequest dto) {
        User translator = userMapper.createUserFromDto(dto);
        register(translator, dto.getPassword(), dto.getConfirmPassword(), roleService.getTranslatorRole());
    }

    private void register(User user, String password, String confirmPassword, Role role) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new DuplicateEmailException("Email already in use.");
        }
        if (!password.equals(confirmPassword)) {
            throw new PasswordMismatchException("Passwords do not match.");
        }
        user.getRoles().add(role);
        user.setPasswordHash(passwordEncoder.encode(password));
        User savedUser = userRepository.save(user);
        walletService.createWallet(savedUser, CurrencyCode.KRW);
    }
}
