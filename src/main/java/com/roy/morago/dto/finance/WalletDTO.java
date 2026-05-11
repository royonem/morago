package com.roy.morago.dto.finance;

import com.roy.morago.enums.CurrencyCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WalletDTO {
    private Integer balance;
    private CurrencyCode currencyCode;
    private String status;
}
