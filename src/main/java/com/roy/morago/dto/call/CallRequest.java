package com.roy.morago.dto.call;

import jakarta.validation.constraints.NotNull;

public record CallRequest(
        @NotNull(message = "clientId is required")
        Long clientId,
        @NotNull(message = "translatorId is required")
        Long translatorId,
        Long topicId
) {
}
