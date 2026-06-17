package com.roy.morago.controller;

import com.roy.morago.dto.auth.*;
import com.roy.morago.service.auth.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@Valid @RequestBody LogoutRequest logoutRequest, Authentication authentication) {
        authService.logout(logoutRequest, authentication);
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/refresh")
    public LoginResponse refresh(@Valid @RequestBody RefreshRequest request) {
        return authService.refresh(request);
    }

    @PostMapping("/register/client")
    @ResponseStatus(HttpStatus.CREATED)
    public void registerClient(@Valid @RequestBody RegisterClientRequest dto) {
        authService.registerClient(dto);
    }

    @PostMapping("/register/translator")
    @ResponseStatus(HttpStatus.CREATED)
    public void registerTranslator(@Valid @RequestBody RegisterTranslatorRequest dto) {
        authService.registerTranslator(dto);
    }
}
