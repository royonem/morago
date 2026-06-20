package com.roy.morago.dto.finance;

import com.roy.morago.enums.CurrencyCode;
import com.roy.morago.enums.TransactionStatus;
import com.roy.morago.enums.TransactionType;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.LocalDateTime;

public record TransactionSearchRequest(
        Long walletUserId,
        Long callId,
        TransactionType type,
        Long amountFrom,
        Long amountTo,
        CurrencyCode currencyCode,
        TransactionStatus status,
        LocalDateTime createdFrom,
        LocalDateTime createdTo,
        LocalDateTime processedFrom,
        LocalDateTime processedTo,
        Integer page,
        Integer size,
        String sortBy,
        String sortDirection
) {
    public TransactionSearchRequest {
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
