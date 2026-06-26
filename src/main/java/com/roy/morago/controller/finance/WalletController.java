package com.roy.morago.controller.finance;

import com.roy.morago.dto.finance.WalletResponse;
import com.roy.morago.enums.CurrencyCode;
import com.roy.morago.service.finance.WalletService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "07 - Wallet", description = "Wallet management endpoints")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/wallet")
public class WalletController {
    private final WalletService walletService;

    @Operation(
            summary = "Suspend wallet",
            description = "Suspends a user's wallet. **Role: ADMIN only.**"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Wallet suspended successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires ADMIN role"),
            @ApiResponse(responseCode = "404", description = "Wallet not found")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/suspend")
    public void suspendWallet(@PathVariable Long id) {
        walletService.suspendWallet(id);
    }

    @Operation(
            summary = "Block wallet",
            description = "Blocks a user's wallet. **Role: ADMIN only.**"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Wallet blocked successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires ADMIN role"),
            @ApiResponse(responseCode = "404", description = "Wallet not found")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/block")
    public void blockWallet(@PathVariable Long id) {
        walletService.blockWallet(id);
    }

    @Operation(
            summary = "Activate wallet",
            description = "Activates a suspended or blocked wallet. **Role: ADMIN only.**"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Wallet activated successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires ADMIN role"),
            @ApiResponse(responseCode = "404", description = "Wallet not found")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/activate")
    public void activateWallet(@PathVariable Long id) {
        walletService.activateWallet(id);
    }

    @Operation(
            summary = "Get wallet",
            description = "Returns wallet details. **Role: ADMIN or the wallet owner.**"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Wallet found successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Not the owner"),
            @ApiResponse(responseCode = "404", description = "Wallet not found")
    })
    @PreAuthorize("hasRole('ADMIN') or @securityService.isWalletOwner(#id, authentication)")
    @GetMapping("/{id}")
    public WalletResponse getWallet(@PathVariable Long id) {
        return walletService.getWallet(id);
    }

    @Operation(
            summary = "Update wallet currency",
            description = "Updates the currency of a wallet. **Role: ADMIN or the wallet owner.**"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Wallet currency updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid currency code or wallet has pending transactions"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Not the owner"),
            @ApiResponse(responseCode = "404", description = "Wallet not found")
    })
    @PreAuthorize("hasRole('ADMIN') or @securityService.isWalletOwner(#id, authentication)")
    @PatchMapping("/{id}/updateCurrency")
    public void updateCurrency(@PathVariable Long id, @RequestParam CurrencyCode newCode) {
        walletService.updateCurrency(id, newCode);
    }

    @Operation(
            summary = "Add funds",
            description = "Adds funds to a wallet. **Role: ADMIN only.**"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Funds added successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid amount (must be positive)"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires ADMIN role"),
            @ApiResponse(responseCode = "404", description = "Wallet not found")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/addFunds")
    public void addFunds(@PathVariable Long id, @RequestParam Long funds) {
        walletService.addFunds(id, funds);
    }

    @Operation(
            summary = "Subtract funds",
            description = "Subtracts funds from a wallet. **Role: ADMIN only.**"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Funds subtracted successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid amount (must be positive)"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires ADMIN role"),
            @ApiResponse(responseCode = "404", description = "Wallet not found"),
            @ApiResponse(responseCode = "409", description = "Insufficient funds")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/subtractFunds")
    public void subtractFunds(@PathVariable Long id, @RequestParam Long funds) {
        walletService.subtractFunds(id, funds);
    }
}