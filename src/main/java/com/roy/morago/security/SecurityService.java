package com.roy.morago.security;

import com.roy.morago.entity.user.User;
import com.roy.morago.repository.call.CallRepository;
import com.roy.morago.repository.finance.BankRepository;
import com.roy.morago.repository.finance.TransactionRepository;
import com.roy.morago.repository.finance.WalletRepository;
import com.roy.morago.repository.finance.WithdrawalRepository;
import com.roy.morago.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("securityService")
@RequiredArgsConstructor
public class SecurityService {
    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final WithdrawalRepository withdrawalRepository;
    private final BankRepository bankRepository;
    private final CallRepository callRepository;

    public boolean isCurrentTranslator(Long userId, Authentication authentication) {
        Long currentUserId = getUserId(authentication);
        if (userRepository.existsByIdAndRolesName(userId, "ROLE_TRANSLATOR")) {
            return currentUserId.equals(userId);
        }
        return false;
    }

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
        return withdrawalRepository.existsByIdAndRequesterId(withdrawalId, userId);
    }

    public boolean isBankAccountOwner(Long bankAccountId, Authentication authentication) {
        Long userId = getUserId(authentication);
        return bankRepository.existsByIdAndUserId(bankAccountId, userId);
    }

    public boolean isCurrentUser(Long userId, Authentication authentication) {
        Long currentUserId = getUserId(authentication);
        return currentUserId.equals(userId);
    }

    public boolean isCallParticipant(Long callId, Authentication authentication) {
        Long currentUserId = getUserId(authentication);
        boolean client = callRepository.existsByIdAndClientId(callId, currentUserId);
        boolean translator = callRepository.existsByIdAndTranslatorId(callId, currentUserId);
        return client || translator;
    }

    public boolean isCallClient(Long callId, Authentication authentication) {
        Long currentUserId = getUserId(authentication);
        return callRepository.existsByIdAndClientId(callId, currentUserId);
    }

    private Long getUserId(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return user.getId();
    }
}
