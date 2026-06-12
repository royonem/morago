package com.roy.morago.controller.finance;

import com.roy.morago.dto.finance.BankAccountDTO;
import com.roy.morago.service.finance.BankService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/bank")
public class BankController {
    private final BankService bankService;

    @PostMapping("/")
    @ResponseStatus(HttpStatus.CREATED)
    public BankAccountDTO linkBankAccount(@Valid @RequestBody BankAccountDTO dto, Authentication authentication) {
        return bankService.linkBankAccount(dto, authentication);
    }

    @PreAuthorize("hasRole('ADMIN') or @securityService.isBankAccountOwner(#id, authentication)")
    @GetMapping("/{id}")
    public BankAccountDTO getBankAccount(@PathVariable Long id) {
        return bankService.getBankAccount(id);
    }

    @PreAuthorize("hasRole('ADMIN') or @securityService.isBankAccountOwner(#id, authentication)")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unlinkBankAccount(@PathVariable Long id) {
        bankService.unlinkBankAccount(id);
    }
}
