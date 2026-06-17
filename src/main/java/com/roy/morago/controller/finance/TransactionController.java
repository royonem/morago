package com.roy.morago.controller.finance;

import com.roy.morago.dto.finance.TransactionRequest;
import com.roy.morago.dto.finance.TransactionResponse;
import com.roy.morago.service.finance.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/transactions")
public class TransactionController {
    private final TransactionService transactionService;

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/deposits")
    @ResponseStatus(HttpStatus.CREATED)
    public TransactionResponse createDeposit(@Valid @RequestBody TransactionRequest dto, Authentication authentication) {
        return transactionService.createDepositTransaction(dto, authentication);
    }

    @PreAuthorize("hasRole('ADMIN') or @securityService.isTransactionOwner(#id, authentication)")
    @GetMapping("/{id}")
    public TransactionResponse getTransaction(@PathVariable Long id) {
        return transactionService.getTransaction(id);
    }

    @PreAuthorize("hasRole('ADMIN') or @securityService.isTransactionOwner(#id, authentication)")
    @GetMapping("/user/{id}")
    public List<TransactionResponse> getAllTransactions(@PathVariable Long id) {
        return transactionService.getAllUserTransactions(id);
    }

    @PreAuthorize("hasRole('ADMIN') or @securityService.isTransactionOwner(#id, authentication)")
    @PatchMapping("/{id}/cancel")
    public void cancelTransaction(@PathVariable Long id) {
        transactionService.cancelTransaction(id);
    }
}
