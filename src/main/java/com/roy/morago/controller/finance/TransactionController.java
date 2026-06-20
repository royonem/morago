package com.roy.morago.controller.finance;

import com.roy.morago.dto.finance.TransactionRequest;
import com.roy.morago.dto.finance.TransactionResponse;
import com.roy.morago.dto.finance.TransactionSearchRequest;
import com.roy.morago.service.finance.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/transactions")
public class TransactionController {
    private final TransactionService transactionService;

    @PostMapping("/deposits")
    @PreAuthorize("isAuthenticated()")
    @ResponseStatus(HttpStatus.CREATED)
    public TransactionResponse createDeposit(@Valid @RequestBody TransactionRequest dto, Authentication authentication) {
        return transactionService.createDepositTransaction(dto, authentication);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @securityService.isTransactionOwner(#id, authentication)")
    public TransactionResponse getTransaction(@PathVariable Long id) {
        return transactionService.getTransaction(id);
    }

    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasRole('ADMIN') or @securityService.isTransactionOwner(#id, authentication)")
    public void cancelTransaction(@PathVariable Long id) {
        transactionService.cancelTransaction(id);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Page<TransactionResponse> getAllTransactions(@PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        return transactionService.getAllTransactions(pageable);
    }

    @GetMapping("/by-user/{userId}")
    @PreAuthorize("hasRole('ADMIN') or @securityService.isTransactionOwner(#userId, authentication)")
    public Page<TransactionResponse> getUserTransactions(@PathVariable Long userId, @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        return transactionService.getTransactionsByUserId(userId, pageable);
    }

    @PostMapping("/search")
    @PreAuthorize("hasRole('ADMIN')")
    public Page<TransactionResponse> searchTransactions(@RequestBody TransactionSearchRequest request) {
        return transactionService.searchTransactions(request);
    }

    @PostMapping("/search/{userId}")
    @PreAuthorize("hasRole('ADMIN') or @securityService.isTransactionOwner(#userId, authentication)")
    public Page<TransactionResponse> searchUserTransactions(@PathVariable Long userId, @RequestBody TransactionSearchRequest request) {
        return transactionService.searchTransactionsByUserId(userId, request);
    }
}
