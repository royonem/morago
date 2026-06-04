package com.roy.morago.dto.finance;

import com.roy.morago.enums.CurrencyCode;
import com.roy.morago.enums.TransactionStatus;
import com.roy.morago.enums.TransactionType;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponse {
    private Long id;
    private TransactionType type;
    private Long amount;
    private CurrencyCode currencyCode;
    private TransactionStatus status;
    private LocalDateTime createdAt;
}
