package com.roy.morago.service.finance;

import com.roy.morago.dto.finance.WalletDTO;
import com.roy.morago.entity.finance.Wallet;
import com.roy.morago.entity.user.User;
import com.roy.morago.enums.CurrencyCode;
import com.roy.morago.enums.TransactionStatus;
import com.roy.morago.enums.WalletStatus;
import com.roy.morago.exception.finance.*;
import com.roy.morago.repository.finance.TransactionRepository;
import com.roy.morago.repository.finance.WalletRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
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
        checkPending(user);
        checkActive(wallet);
        wallet.setCurrencyCode(newCode);
    }

    @Transactional
    public void addFunds(Long id, Long funds) {
        Wallet wallet = findWalletById(id);
        User user = wallet.getUser();
        checkPending(user);
        checkActive(wallet);
        checkPositiveMovingFunds(funds);
        wallet.setBalance(wallet.getBalance() + funds);
    }

    @Transactional
    public void subtractFunds(Long id, Long funds) {
        Wallet wallet = findWalletById(id);
        User user = wallet.getUser();
        checkPending(user);
        checkActive(wallet);
        checkPositiveMovingFunds(funds);
        long newBalance = wallet.getBalance() - funds;
        validateNonNegativeBalance(newBalance);
        wallet.setBalance(newBalance);
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

    private void checkPending(User user) {
        if (transactionRepository.existsByWalletUserIdAndStatus(user.getId(), TransactionStatus.PENDING)) {
            throw new ExistingTransactionException("Cannot update wallet while there is a pending transaction.");
        }
    }

    private void checkActive(Wallet wallet) {
        if (wallet.getStatus() != WalletStatus.ACTIVE) {
            throw new NonActiveWalletException("Cannot access wallet.");
        }
    }

    private void checkPositiveMovingFunds(Long funds) {
        if (funds <= 0) {
            throw new NonPositiveTransactionException("Transactions must include a positive amount.");
        }
    }

    protected void validateNonNegativeBalance(Long balance) {
        if (balance < 0L) {
            throw new DeficientFundsException("Wallet does not have enough funds");
        }
    }
}
