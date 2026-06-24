package com.roy.morago.dto.topic;

import jakarta.validation.constraints.NotBlank;

public record TopicRequest(
        Long categoryId,
        Long iconId,
        @NotBlank
        String name,
        Boolean active
) {
}
