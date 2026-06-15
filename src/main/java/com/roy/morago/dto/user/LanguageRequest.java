package com.roy.morago.dto.user;

import jakarta.validation.constraints.NotBlank;

public record LanguageRequest(@NotBlank String name) { }
