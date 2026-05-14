package com.roy.morago.dto.finance;

import com.roy.morago.enums.WalletStatus;
import com.roy.morago.enums.CurrencyCode;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WalletDTO {
    private Long balance;
    private CurrencyCode currencyCode;
    private WalletStatus status;
}
