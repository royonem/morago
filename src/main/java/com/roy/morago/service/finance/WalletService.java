package com.roy.morago.service.finance;

import com.roy.morago.dto.finance.WalletDTO;
import com.roy.morago.entity.finance.Wallet;
import com.roy.morago.entity.user.User;
import com.roy.morago.enums.CurrencyCode;
import com.roy.morago.enums.WalletStatus;
import com.roy.morago.repository.finance.WalletRepository;
import com.roy.morago.repository.user.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class WalletService {
    private final WalletRepository walletRepository;
    private final UserRepository userRepository;
    private final FinanceHelper helper;

    @Transactional
    public void createWallet(User user, CurrencyCode code) {
        Wallet wallet = new Wallet();
        wallet.setUser(user);
        wallet.setBalance(0L);
        wallet.setCurrencyCode(code);
        wallet.setStatus(WalletStatus.ACTIVE);
        user.setWallet(wallet);
        userRepository.save(user);
        walletRepository.save(wallet);
    }

    public WalletDTO getWallet(Long id) {
        Wallet wallet = helper.findWalletById(id);
        WalletDTO walletDTO = new WalletDTO();
        walletDTO.setBalance(wallet.getBalance());
        walletDTO.setCurrencyCode(wallet.getCurrencyCode());
        walletDTO.setStatus(wallet.getStatus());
        return walletDTO;
    }

    @Transactional
    public void suspendWallet(Long id) {
        Wallet wallet = helper.findWalletById(id);
        wallet.setStatus(WalletStatus.SUSPENDED);
    }

    @Transactional
    public void blockWallet(Long id) {
        Wallet wallet = helper.findWalletById(id);
        wallet.setStatus(WalletStatus.BLOCKED);
    }

    @Transactional
    public void activateWallet(Long id) {
        Wallet wallet = helper.findWalletById(id);
        wallet.setStatus(WalletStatus.ACTIVE);
    }

    @Transactional
    public void updateCurrency(Long id, CurrencyCode newCode) {
        Wallet wallet = helper.findWalletById(id);
        User user = wallet.getUser();
        helper.validateNoPendingTransactions(user);
        helper.validateWalletIsActive(wallet);
        wallet.setCurrencyCode(newCode);
    }

    @Transactional
    public void addFunds(Long id, Long funds) {
        Wallet wallet = helper.findWalletById(id);
        User user = wallet.getUser();

        helper.validateNoPendingTransactions(user);
        helper.validateWalletIsActive(wallet);
        helper.validatePositiveTransaction(funds);
        wallet.setBalance(wallet.getBalance() + funds);
    }

    @Transactional
    public void subtractFunds(Long id, Long funds) {
        Wallet wallet = helper.findWalletById(id);
        User user = wallet.getUser();
        helper.validateNoPendingTransactions(user);
        helper.validateWalletIsActive(wallet);
        helper.validatePositiveTransaction(funds);
        long newBalance = wallet.getBalance() - funds;
        helper.validateNonNegativeWalletBalance(newBalance);
        wallet.setBalance(newBalance);
    }
}
