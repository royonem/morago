package com.roy.morago.service.auth;

import com.roy.morago.dto.auth.*;
import com.roy.morago.entity.auth.RefreshToken;
import com.roy.morago.entity.user.Role;
import com.roy.morago.entity.user.User;
import com.roy.morago.enums.Availability;
import com.roy.morago.enums.CurrencyCode;
import com.roy.morago.enums.UserStatus;
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
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.email(),
                            loginRequest.password()
                    )
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
            return createLoginResponse(userHelper.findUserWithAuthentication(authentication));
        } catch (AuthenticationException e) {
            throw new InvalidCredentialsException("Invalid username or password");
        }
    }

    @Transactional
    public LoginResponse refresh(RefreshRequest refreshRequest) {
        String tokenString = refreshRequest.refreshToken();
        RefreshToken refreshToken = refreshTokenService.validateRefreshToken(tokenString);
        refreshTokenService.revokeRefreshToken(refreshToken);
        return createLoginResponse(refreshToken.getUser());
    }

    @Transactional
    public void logout(LogoutRequest logoutRequest, Authentication authentication) {
        RefreshToken refreshToken = refreshTokenService.validateRefreshToken(logoutRequest.refreshToken());
        refreshTokenService.validateRefreshTokenOwner(refreshToken, userHelper.findUserWithAuthentication(authentication));
        refreshTokenService.revokeRefreshToken(refreshToken);
    }

    @Transactional
    public void registerClient(ClientRegisterRequest dto) {
        User client = userMapper.createEntityFromRequest(dto);
        register(client, dto.password(), dto.confirmPassword(), roleService.getClientRole());
    }

    @Transactional
    public void registerTranslator(TranslatorRegisterRequest dto) {
        User translator = userMapper.createEntityFromRequest(dto);
        register(translator, dto.password(), dto.confirmPassword(), roleService.getTranslatorRole());
    }

    // Helper Methods
    private LoginResponse createLoginResponse(User user) {
        String accessToken = jwtProvider.generateToken(user);
        String refreshToken = refreshTokenService.createRefreshToken(user).getToken();
        return new LoginResponse(accessToken, refreshToken);
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
        user.setAvailability(Availability.OFFLINE);
        user.setStatus(UserStatus.UNVERIFIED);
        User savedUser = userRepository.save(user);
        walletService.createWallet(savedUser, CurrencyCode.KRW);
    }
}
