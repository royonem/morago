package com.roy.morago.dto.finance;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BankAccountDTO {
    @NotBlank(message = "Bank name is required.")
    private String bankName;
    @NotBlank(message = "Account number is required.")
    private String accountNumber;
}
