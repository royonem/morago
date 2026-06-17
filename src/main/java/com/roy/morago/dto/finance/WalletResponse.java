package com.roy.morago.dto.finance;

import com.roy.morago.enums.WalletStatus;
import com.roy.morago.enums.CurrencyCode;

public record WalletResponse(
        Long balance,
        CurrencyCode currencyCode,
        WalletStatus status
) {
}
