package com.roy.morago.dto.finance;

import com.roy.morago.enums.CurrencyCode;
import com.roy.morago.enums.TransactionType;
import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TransactionDTO {
    @NotNull(message = "Transaction type must be specified")
    private TransactionType type;
    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private Long amount;
    @NotNull(message = "Currency type must be specified")
    private CurrencyCode currencyCode;
    private String reference;
    private String description;
}
