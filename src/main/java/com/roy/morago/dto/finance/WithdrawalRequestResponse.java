package com.roy.morago.dto.finance;

import com.roy.morago.enums.CurrencyCode;
import com.roy.morago.enums.WithdrawalStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WithdrawalRequestResponse {
    private Long transactionId;
    private Long amount;
    private CurrencyCode currencyCode;
    private WithdrawalStatus status;
    private String rejectionReason;
    private LocalDateTime createdAt;
}
