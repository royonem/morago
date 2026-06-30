package com.roy.morago.dto.auth;

public interface RegisterRequest {
    String firstName();
    String lastName();
    String email();
    String phone();
    String password();
    String confirmPassword();
}
