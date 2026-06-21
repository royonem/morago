package com.roy.morago.dto.user;

import com.roy.morago.enums.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.LocalDateTime;

public record UserSearchRequest(
        Long roleId,
        Long topicId,
        Long languageId,
        String firstName,
        String lastName,
        String email,
        String phone,
        Availability availability,
        UserStatus status,
        TopikLevel topikLevelFrom,
        TopikLevel topikLevelTo,
        LocalDateTime birthdateFrom,
        LocalDateTime birthdateTo,
        LocalDateTime createdFrom,
        LocalDateTime createdTo,
        Integer page,
        Integer size,
        String sortBy,
        String sortDirection
) {
    public UserSearchRequest {
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
