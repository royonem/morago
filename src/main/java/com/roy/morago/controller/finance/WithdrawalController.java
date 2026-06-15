package com.roy.morago.controller.finance;

import com.roy.morago.dto.finance.WithdrawalRejection;
import com.roy.morago.dto.finance.WithdrawalRequest;
import com.roy.morago.dto.finance.WithdrawalResponse;
import com.roy.morago.service.finance.WithdrawalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
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
    public WithdrawalResponse createWithdrawal(@RequestBody @Valid WithdrawalRequest request, Authentication authentication) {
        return withdrawalService.createWithdrawal(request, authentication);
    }

    @PreAuthorize("hasRole('ADMIN') or @securityService.isWithdrawalOwner(#id, authentication)")
    @GetMapping("/{id}")
    public WithdrawalResponse getWithdrawal(@PathVariable Long id) {
        return withdrawalService.getWithdrawal(id);
    }

    @PreAuthorize("hasRole('ADMIN') or @securityService.isWithdrawalOwner(#id, authentication)")
    @PatchMapping("/{id}/cancel")
    public void cancelWithdrawal(@PathVariable Long id) {
        withdrawalService.cancelWithdrawal(id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/reject")
    public void rejectWithdrawal(@PathVariable Long id, Authentication authentication, @RequestBody WithdrawalRejection rejectionDTO) {
        withdrawalService.rejectWithdrawal(id, authentication, rejectionDTO);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/approve")
    public void approveWithdrawal(@PathVariable Long id, Authentication authentication) {
        withdrawalService.approveWithdrawal(id, authentication);
    }
}
