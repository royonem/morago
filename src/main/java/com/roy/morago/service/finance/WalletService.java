package com.roy.morago.service.finance;

import com.roy.morago.dto.finance.WalletDTO;
import com.roy.morago.entity.finance.Wallet;
import com.roy.morago.entity.user.User;
import com.roy.morago.enums.CurrencyCode;
import com.roy.morago.enums.TransactionStatus;
import com.roy.morago.enums.WalletStatus;
import com.roy.morago.exception.DeficientFundsException;
import com.roy.morago.exception.InvalidTransactionStateException;
import com.roy.morago.exception.WalletNotFoundException;
import com.roy.morago.repository.finance.TransactionRepository;
import com.roy.morago.repository.finance.WalletRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class WalletService {
    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;

    @Transactional
    public void createWallet(User user, CurrencyCode code) {
        Wallet wallet = new Wallet();
        wallet.setUser(user);
        wallet.setBalance(0L);
        wallet.setCurrencyCode(code);
        wallet.setStatus(WalletStatus.ACTIVE);
        walletRepository.save(wallet);
    }

    @Transactional
    public void suspendWallet(Long id) {
        Wallet wallet = findWalletById(id);
        wallet.setStatus(WalletStatus.SUSPENDED);
    }

    @Transactional
    public void blockWallet(Long id) {
        Wallet wallet = findWalletById(id);
        wallet.setStatus(WalletStatus.BLOCKED);
    }

    @Transactional
    public void activateWallet(Long id) {
        Wallet wallet = findWalletById(id);
        wallet.setStatus(WalletStatus.ACTIVE);
    }

    @Transactional
    public void updateCurrency(Long id, CurrencyCode newCode) {
        Wallet wallet = findWalletById(id);
        User user = wallet.getUser();

        if (transactionRepository.existsByWalletUserIdAndStatus(user.getId(), TransactionStatus.PENDING)) {
            throw new InvalidTransactionStateException("Cannot update wallet while there is a pending transaction.");
        }
        wallet.setCurrencyCode(newCode);
    }

    public WalletDTO getWalletById(Long id) {
        Wallet wallet = findWalletById(id);
        WalletDTO walletDTO = new WalletDTO();
        walletDTO.setBalance(wallet.getBalance());
        walletDTO.setCurrencyCode(wallet.getCurrencyCode());
        walletDTO.setStatus(wallet.getStatus());
        return walletDTO;
    }

    private Wallet findWalletById(Long id) {
        return walletRepository.findById(id)
                .orElseThrow(() -> new WalletNotFoundException("Wallet not found"));
    }

    protected void validateNonNegativeBalance(Long balance) {
        if (balance < 0L) {
            throw new DeficientFundsException("Wallet does not have enough funds");
        }
    }
}
