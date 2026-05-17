package com.roy.morago.controller.finance;

import com.roy.morago.dto.finance.TransactionDTO;
import com.roy.morago.dto.finance.TransactionResponse;
import com.roy.morago.service.finance.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/transactions")
public class TransactionController {
    private final TransactionService transactionService;

    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    public TransactionResponse createTransaction(@Valid @RequestBody TransactionDTO dto, Authentication authentication) {
        return transactionService.createTransaction(dto, authentication);
    }

    @GetMapping("/{id}")
    public TransactionResponse getTransaction(@PathVariable Long id) {
        return transactionService.getTransaction(id);
    }

    @PatchMapping("/{id}/cancel")
    public void cancelTransaction(@PathVariable Long id) {
        transactionService.cancelTransaction(id);
    }
}
