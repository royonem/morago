package com.roy.morago.dto.finance;

import jakarta.validation.constraints.NotBlank;

public record BankAccountResponse(
        Long id,
        @NotBlank(message = "Bank name is required.")
        String bankName,
        @NotBlank(message = "Account number is required.")
        String accountNumber
) {
}
