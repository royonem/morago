package com.roy.morago.controller;

import com.roy.morago.dto.auth.*;
import com.roy.morago.service.auth.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Tag(name = "01 - Authentication", description = "Login and Registration APIs")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    @Operation(
            summary = "Login",
            description = "Authenticates a user and returns JWT access token and refresh token."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login successful - Returns access and refresh tokens"),
            @ApiResponse(responseCode = "401", description = "Invalid credentials - Wrong email or password")
    })
    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @Operation(
            summary = "Logout",
            description = "Revokes the current refresh token. User must be authenticated."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Logged out successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid token")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@Valid @RequestBody LogoutRequest logoutRequest, Authentication authentication) {
        authService.logout(logoutRequest, authentication);
    }

    @Operation(
            summary = "Refresh token",
            description = "Exchanges a valid refresh token for a new access token."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "New access token generated"),
            @ApiResponse(responseCode = "401", description = "Invalid or expired refresh token")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/refresh")
    public LoginResponse refresh(@Valid @RequestBody RefreshRequest request) {
        return authService.refresh(request);
    }

    @Operation(
            summary = "Register client",
            description = "Creates a new client account. Email must be unique and password must match confirmation."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Client registered successfully"),
            @ApiResponse(responseCode = "409", description = "Conflict - Email already in use"),
            @ApiResponse(responseCode = "400", description = "Bad request - Password mismatch or invalid data")
    })
    @PostMapping("/register/client")
    @ResponseStatus(HttpStatus.CREATED)
    public void registerClient(@Valid @RequestBody ClientRegisterRequest dto) {
        authService.registerClient(dto);
    }

    @Operation(
            summary = "Register translator",
            description = "Creates a new translator account. Email must be unique and password must match confirmation."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Translator registered successfully"),
            @ApiResponse(responseCode = "409", description = "Conflict - Email already in use"),
            @ApiResponse(responseCode = "400", description = "Bad request - Password mismatch or invalid data")
    })
    @PostMapping("/register/translator")
    @ResponseStatus(HttpStatus.CREATED)
    public void registerTranslator(@Valid @RequestBody TranslatorRegisterRequest dto) {
        authService.registerTranslator(dto);
    }
}
