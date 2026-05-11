package com.roy.morago.dto.finance;

import com.roy.morago.enums.CurrencyCode;
import com.roy.morago.enums.TransactionType;
import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TransactionDTO {
    private TransactionType type;
    private Integer coinAmount;
    private BigDecimal cashAmount;
    private CurrencyCode currencyCode;
    private String reference;
    private String description;
}
