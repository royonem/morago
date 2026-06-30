package com.roy.morago.controller.finance;

import com.roy.morago.dto.finance.WithdrawalRejection;
import com.roy.morago.dto.finance.WithdrawalRequest;
import com.roy.morago.dto.finance.WithdrawalResponse;
import com.roy.morago.service.finance.WithdrawalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Tag(name = "09 - Withdrawals", description = "Withdrawal management endpoints")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/withdrawals")
public class WithdrawalController {
    private final WithdrawalService withdrawalService;

    @Operation(
            summary = "Create withdrawal",
            description = "Creates a new withdrawal request. **Role: Any authenticated user.**"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Withdrawal created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "409", description = "Pending withdrawal already exists or insufficient balance")
    })
    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    public WithdrawalResponse createWithdrawal(@RequestBody @Valid WithdrawalRequest request, Authentication authentication) {
        return withdrawalService.createWithdrawal(request, authentication);
    }

    @Operation(
            summary = "Get withdrawal by ID",
            description = "Returns withdrawal details. **Role: ADMIN or the withdrawal owner.**"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Withdrawal found successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Not the owner"),
            @ApiResponse(responseCode = "404", description = "Withdrawal not found")
    })
    @PreAuthorize("hasRole('ADMIN') or @securityService.isWithdrawalOwner(#id, authentication)")
    @GetMapping("/{id}")
    public WithdrawalResponse getWithdrawal(@PathVariable Long id) {
        return withdrawalService.getWithdrawal(id);
    }

    @Operation(
            summary = "Cancel withdrawal",
            description = "Cancels a pending withdrawal request. **Role: ADMIN or the withdrawal owner.**"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Withdrawal canceled successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Not the owner"),
            @ApiResponse(responseCode = "404", description = "Withdrawal not found"),
            @ApiResponse(responseCode = "409", description = "Withdrawal is not pending")
    })
    @PreAuthorize("hasRole('ADMIN') or @securityService.isWithdrawalOwner(#id, authentication)")
    @PatchMapping("/{id}/cancel")
    public void cancelWithdrawal(@PathVariable Long id) {
        withdrawalService.cancelWithdrawal(id);
    }

    @Operation(
            summary = "Reject withdrawal",
            description = "Rejects a pending withdrawal request. **Role: ADMIN only.**"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Withdrawal rejected successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires ADMIN role"),
            @ApiResponse(responseCode = "404", description = "Withdrawal not found"),
            @ApiResponse(responseCode = "409", description = "Withdrawal is not pending")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/reject")
    public void rejectWithdrawal(@PathVariable Long id, Authentication authentication, @RequestBody WithdrawalRejection rejectionDTO) {
        withdrawalService.rejectWithdrawal(id, authentication, rejectionDTO);
    }

    @Operation(
            summary = "Approve withdrawal",
            description = "Approves a pending withdrawal request. **Role: ADMIN only.**"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Withdrawal approved and processed successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires ADMIN role"),
            @ApiResponse(responseCode = "404", description = "Withdrawal not found"),
            @ApiResponse(responseCode = "409", description = "Withdrawal is not pending or insufficient balance")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/approve")
    public void approveWithdrawal(@PathVariable Long id, Authentication authentication) {
        withdrawalService.approveWithdrawal(id, authentication);
    }
}