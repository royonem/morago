package com.roy.morago.dto.finance;

import com.roy.morago.enums.CurrencyCode;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record WithdrawalRequest(
        @NotNull(message = "Amount is required")
        @Positive(message = "Amount must be positive")
        Long amount,
        @NotNull
        CurrencyCode currencyCode
) { }
