package com.roy.morago.dto.topic;

import jakarta.validation.constraints.NotNull;

public record CategoryRequest(
        @NotNull(message = "Category name is required.")
        String name,
        @NotNull
        Boolean active
) {  }
