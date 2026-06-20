package com.roy.morago.dto.call;

import com.roy.morago.enums.CallStatus;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.LocalDateTime;

public record CallSearchRequest(
        Long clientId,
        Long translatorId,
        Long topicId,
        CallStatus status,
        Integer ratingFrom,
        Integer ratingTo,
        LocalDateTime acceptedFrom,
        LocalDateTime acceptedTo,
        LocalDateTime startedFrom,
        LocalDateTime startedTo,
        LocalDateTime endedFrom,
        LocalDateTime endedTo,
        LocalDateTime canceledFrom,
        LocalDateTime canceledTo,
        Integer page,
        Integer size,
        String sortBy,
        String sortDirection
) {

    public CallSearchRequest {
        if (page == null) page = 0;
        if (size == null) size = 20;
        if (sortBy == null) sortBy = "id";
        if (sortDirection == null) sortDirection = "DESC";
    }

    public Pageable toPageable() {
        return PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.fromString(sortDirection), sortBy)
        );
    }
}
