package com.roy.morago.dto.topic;

public record TopicResponse (
        Long id,
        Long categoryId,
        Long iconId,
        String name,
        Boolean active
) {
}
