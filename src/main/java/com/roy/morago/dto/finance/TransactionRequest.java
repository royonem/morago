package com.roy.morago.dto.finance;

import com.roy.morago.enums.CurrencyCode;
import com.roy.morago.enums.TransactionType;
import jakarta.validation.constraints.*;

public record TransactionRequest(
        @NotNull(message = "Transaction type must be specified")
        TransactionType type,
        @NotNull(message = "Amount is required")
        @Positive(message = "Amount must be positive")
        Long amount,
        @NotNull(message = "Currency type must be specified")
        CurrencyCode currencyCode
) {
}
