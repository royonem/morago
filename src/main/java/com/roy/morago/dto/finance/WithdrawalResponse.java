package com.roy.morago.dto.finance;

import com.roy.morago.enums.CurrencyCode;
import com.roy.morago.enums.WithdrawalStatus;
import java.time.LocalDateTime;

public record WithdrawalResponse(
         Long id,
         Long amount,
         CurrencyCode currencyCode,
         WithdrawalStatus status,
         String rejectionReason,
         LocalDateTime createdAt
) { }

