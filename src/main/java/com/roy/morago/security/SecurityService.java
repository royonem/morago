package com.roy.morago.security;

import com.roy.morago.repository.finance.BankRepository;
import com.roy.morago.repository.finance.TransactionRepository;
import com.roy.morago.repository.finance.WalletRepository;
import com.roy.morago.repository.finance.WithdrawalRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("securityService")
@RequiredArgsConstructor
public class SecurityService {
    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final WithdrawalRequestRepository withdrawalRequestRepository;
    private final BankRepository bankRepository;

    public boolean isWalletOwner(Long walletId, Authentication authentication) {
        Long userId = getUserId(authentication);
        return walletRepository.existsByIdAndUserId(walletId, userId);
    }

    public boolean isTransactionOwner(Long transactionId, Authentication authentication) {
        Long userId = getUserId(authentication);
        return transactionRepository.existsByIdAndWalletUserId(transactionId, userId);
    }

    public boolean isWithdrawalOwner(Long withdrawalId, Authentication authentication) {
        Long userId = getUserId(authentication);
        return withdrawalRequestRepository.existsByIdAndRequesterId(withdrawalId, userId);
    }

    public boolean isBankAccountOwner(Long bankAccountId, Authentication authentication) {
        Long userId = getUserId(authentication);
        return bankRepository.existsByIdAndUserId(bankAccountId, userId);
    }

    public boolean isCurrentUser(Long userId, Authentication authentication) {
        Long currentUserId = getUserId(authentication);
        return currentUserId.equals(userId);
    }

    private Long getUserId(Authentication authentication) {
        com.roy.morago.entity.user.User user = (com.roy.morago.entity.user.User) authentication.getPrincipal();
        return user.getId();
    }
}
