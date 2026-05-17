package com.roy.morago.controller.finance;

import com.roy.morago.dto.finance.RejectWithdrawalDTO;
import com.roy.morago.dto.finance.WithdrawalRequestDTO;
import com.roy.morago.dto.finance.WithdrawalRequestResponse;
import com.roy.morago.service.finance.WithdrawalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/withdrawals")
public class WithdrawalController {
    private final WithdrawalService withdrawalService;

    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    public WithdrawalRequestResponse createWithdrawalRequest(@RequestBody @Valid WithdrawalRequestDTO dto, Authentication authentication) {
        return withdrawalService.createWithdrawalRequest(dto, authentication);
    }

    @GetMapping("/{id}")
    public WithdrawalRequestResponse getWithdrawalRequest(@PathVariable Long id) {
        return withdrawalService.getWithdrawalRequestByTransactionId(id);
    }

    @PatchMapping("/{id}/cancel")
    public void cancelWithdrawalRequest(@PathVariable Long id) {
        withdrawalService.cancelWithdrawalRequest(id);
    }

    @PatchMapping("/{id}/reject")
    public void rejectWithdrawalRequest(@PathVariable Long id, Authentication authentication, @RequestBody RejectWithdrawalDTO rejectionDTO) {
        withdrawalService.rejectWithdrawalRequest(id, authentication, rejectionDTO);
    }

    @PatchMapping("/{id}/approve")
    public void approveWithdrawalRequest(@PathVariable Long id, Authentication authentication) {
        withdrawalService.approveWithdrawalRequest(id, authentication);
    }
}
