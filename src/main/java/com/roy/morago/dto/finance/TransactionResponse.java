package com.roy.morago.dto.finance;

import com.roy.morago.enums.CurrencyCode;
import com.roy.morago.enums.TransactionStatus;
import com.roy.morago.enums.TransactionType;
import java.time.LocalDateTime;

public record TransactionResponse(
     Long id,
     TransactionType type,
     Long amount,
     CurrencyCode currencyCode,
     TransactionStatus status,
     LocalDateTime createdAt,
     LocalDateTime processedAt
) { }
