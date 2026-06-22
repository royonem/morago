package com.roy.morago.dto.call;

import com.roy.morago.enums.CallStatus;

import java.time.LocalDateTime;

public record CallResponse(
        Long id,
        Long clientId,
        Long translatorId,
        Long topicId,
        Long cost,
        CallStatus status,
        Integer rating,
        LocalDateTime acceptedAt,
        LocalDateTime startedAt,
        LocalDateTime endedAt,
        LocalDateTime canceledAt
) { }
