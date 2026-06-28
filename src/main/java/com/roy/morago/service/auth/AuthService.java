package com.roy.morago.service.auth;

import com.roy.morago.dto.auth.*;
import com.roy.morago.entity.auth.RefreshToken;
import com.roy.morago.entity.user.Role;
import com.roy.morago.entity.user.User;
import com.roy.morago.enums.Availability;
import com.roy.morago.enums.CurrencyCode;
import com.roy.morago.enums.UserStatus;
import com.roy.morago.exception.auth.DuplicatePhoneException;
import com.roy.morago.exception.auth.InvalidCredentialsException;
import com.roy.morago.exception.auth.DuplicateEmailException;
import com.roy.morago.exception.auth.PasswordMismatchException;
import com.roy.morago.mapper.UserMapper;
import com.roy.morago.repository.user.UserRepository;
import com.roy.morago.security.JwtProvider;
import com.roy.morago.service.finance.WalletService;
import com.roy.morago.service.user.RoleService;
import com.roy.morago.service.user.UserHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
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
    private final RefreshTokenService refreshTokenService;
    private final UserHelper userHelper;

    @Transactional
    public LoginResponse login(LoginRequest loginRequest) {
        log.info("Login attempt for email: {}", loginRequest.email());
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.email(),
                            loginRequest.password()
                    )
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
            User user = userHelper.findUserByEmail(loginRequest.email());
            LoginResponse response = createLoginResponse(user);
            log.info("Login successful for user ID: {}", user.getId());
            return response;
        } catch (AuthenticationException e) {
            log.warn("Login FAILED for email: {} - Invalid credentials", loginRequest.email());
            throw new InvalidCredentialsException("Invalid username or password");
        }
    }

    @Transactional
    public LoginResponse refresh(RefreshRequest refreshRequest) {
        log.info("Refresh token attempt received");
        String tokenString = refreshRequest.refreshToken();
        RefreshToken refreshToken = refreshTokenService.validateRefreshToken(tokenString);
        refreshTokenService.revokeRefreshToken(refreshToken);
        log.info("Refresh token successful for user ID: {}", refreshToken.getUser().getId());
        return createLoginResponse(refreshToken.getUser());
    }

    @Transactional
    public void logout(LogoutRequest logoutRequest, Authentication authentication) {
        User user = userHelper.findUserWithAuthentication(authentication);
        log.info("Logout attempt for user ID: {}", user.getId());
        RefreshToken refreshToken = refreshTokenService.validateRefreshToken(logoutRequest.refreshToken());
        refreshTokenService.validateRefreshTokenOwner(refreshToken, user);
        refreshTokenService.revokeRefreshToken(refreshToken);
        log.info("Logout successful for user ID: {}", user.getId());
    }

    @Transactional
    public void registerClient(ClientRegisterRequest dto) {
        log.info("Client registration attempt with email: {}", dto.email());
        validateRegistration(dto);
        User client = userMapper.toEntity(dto);
        register(client, dto, roleService.getClientRole());
    }

    @Transactional
    public void registerTranslator(TranslatorRegisterRequest dto) {
        log.info("Translator registration attempt with email: {}", dto.email());
        validateRegistration(dto);
        User translator = userMapper.toEntity(dto);
        register(translator, dto, roleService.getTranslatorRole());
    }

    // Helper Methods
    private LoginResponse createLoginResponse(User user) {
        String accessToken = jwtProvider.generateToken(user);
        String refreshToken = refreshTokenService.createRefreshToken(user).getToken();
        return new LoginResponse(accessToken, refreshToken);
    }

    private void register(User user, RegisterRequest dto, Role role) {
        user.getRoles().add(role);
        user.setPasswordHash(passwordEncoder.encode(dto.password()));
        user.setAvailability(Availability.OFFLINE);
        user.setStatus(UserStatus.UNVERIFIED);
        User savedUser = userRepository.save(user);
        walletService.createWallet(savedUser, CurrencyCode.KRW);
        log.info("User registered successfully: userId={}, userName={}, role={}, email={}, walletId={}", savedUser.getId(), savedUser.getFullName(), role.getName(), savedUser.getEmail(), savedUser.getWallet().getId());
    }

    private void validateRegistration(RegisterRequest dto) {
        if (userRepository.existsByEmail(dto.email())) {
            log.warn("Registration FAILED - Email already in use: {}", dto.email());
            throw new DuplicateEmailException("Email already in use.");
        }
        if (!dto.password().equals(dto.confirmPassword())) {
            log.warn("Registration FAILED - Password mismatch for email: {}", dto.email());
            throw new PasswordMismatchException("Passwords do not match.");
        }
        if (userRepository.existsByPhone(dto.phone())) {
            log.warn("Registration FAILED - Phone number already in use: {}", dto.phone());
            throw new DuplicatePhoneException("Phone number already in use.");
        }
    }
}
