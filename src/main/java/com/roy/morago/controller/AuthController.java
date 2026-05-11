package com.roy.morago.controller;

import com.roy.morago.dto.auth.RegisterClientRequest;
import com.roy.morago.dto.auth.LoginRequest;
import com.roy.morago.dto.auth.LoginResponse;
import com.roy.morago.dto.auth.RegisterTranslatorRequest;
import com.roy.morago.service.auth.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request) {
        return authService.login(request);
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
