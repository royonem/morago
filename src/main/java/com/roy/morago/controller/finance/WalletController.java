package com.roy.morago.controller.finance;

import com.roy.morago.dto.finance.WalletDTO;
import com.roy.morago.enums.CurrencyCode;
import com.roy.morago.service.finance.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/wallet")
public class WalletController {
    private final WalletService walletService;

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/suspend")
    public void suspendWallet(@PathVariable Long id) {
        walletService.suspendWallet(id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/block")
    public void blockWallet(@PathVariable Long id) {
        walletService.blockWallet(id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/activate")
    public void activateWallet(@PathVariable Long id) {
        walletService.activateWallet(id);
    }

    @PreAuthorize("hasRole('ADMIN') or @securityService.isWalletOwner(#id, authentication)")
    @GetMapping("/{id}")
    public WalletDTO getWallet(@PathVariable Long id) {
        return walletService.getWalletById(id);
    }

    @PreAuthorize("hasRole('ADMIN') or @securityService.isWalletOwner(#id, authentication)")
    @PatchMapping("/{id}/updateCurrency")
    public void updateCurrency(@PathVariable Long id, @RequestParam CurrencyCode newCode) {
        walletService.updateCurrency(id, newCode);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/addFunds")
    public void addFunds(@PathVariable Long id, @RequestParam Long funds) {
        walletService.addFunds(id, funds);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/subtractFunds")
    public void subtractFunds(@PathVariable Long id, @RequestParam Long funds) {
        walletService.subtractFunds(id, funds);
    }
}
