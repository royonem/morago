package com.roy.morago.service.finance;

import com.roy.morago.dto.finance.WalletResponse;
import com.roy.morago.entity.finance.Wallet;
import com.roy.morago.entity.user.User;
import com.roy.morago.enums.CurrencyCode;
import com.roy.morago.enums.WalletStatus;
import com.roy.morago.repository.finance.WalletRepository;
import com.roy.morago.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class WalletService {
    private final WalletRepository walletRepository;
    private final UserRepository userRepository;
    private final FinanceHelper helper;

    @Transactional
    public void createWallet(User user, CurrencyCode code) {
        log.info("Creating wallet: userId={}", user.getId());
        Wallet wallet = new Wallet();
        wallet.setUser(user);
        wallet.setBalance(0L);
        wallet.setCurrencyCode(code);
        wallet.setStatus(WalletStatus.ACTIVE);
        user.setWallet(wallet);
        userRepository.save(user);
        walletRepository.save(wallet);
        log.info("Wallet created: userId={}", user.getId());
    }

    public WalletResponse getWallet(Long id) {
        Wallet wallet = helper.findWalletById(id);
        return new WalletResponse(
                wallet.getBalance(),
                wallet.getCurrencyCode(),
                wallet.getStatus()
        );
    }

    @Transactional
    public void suspendWallet(Long id) {
        log.info("Suspending wallet: walletId={}", id);
        Wallet wallet = helper.findWalletById(id);
        wallet.setStatus(WalletStatus.SUSPENDED);
        log.info("Wallet suspended: walletId={}", id);
    }

    @Transactional
    public void blockWallet(Long id) {
        log.info("Blocking wallet: walletId={}", id);
        Wallet wallet = helper.findWalletById(id);
        wallet.setStatus(WalletStatus.BLOCKED);
        log.info("Wallet blocked: walletId={}", id);
    }

    @Transactional
    public void activateWallet(Long id) {
        log.info("Activating wallet: walletId={}", id);
        Wallet wallet = helper.findWalletById(id);
        wallet.setStatus(WalletStatus.ACTIVE);
        log.info("Wallet activated: walletId={}", id);
    }

    @Transactional
    public void updateCurrency(Long id, CurrencyCode newCode) {
        log.info("Updating wallet currency: walletId={}, currency={}", id, newCode);
        Wallet wallet = helper.findWalletById(id);
        User user = wallet.getUser();
        helper.validateNoPendingTransactions(user);
        helper.validateWalletIsActive(wallet);
        wallet.setCurrencyCode(newCode);
        log.info("Wallet currency updated: walletId={}, currency={}", id, newCode);
    }

    @Transactional
    public void addFunds(Long id, Long funds) {
        log.info("Adding funds: walletId={}, amount={}", id, funds);
        Wallet wallet = helper.findWalletById(id);
        helper.validateWalletIsActive(wallet);
        helper.validatePositiveTransaction(funds);
        wallet.setBalance(wallet.getBalance() + funds);
        log.info("Funds added: walletId={}, amount={}, newBalance={}",
                id, funds, wallet.getBalance());
    }

    @Transactional
    public void subtractFunds(Long id, Long funds) {
        log.info("Subtracting funds: walletId={}, amount={}", id, funds);
        Wallet wallet = helper.findWalletById(id);
        helper.validateWalletIsActive(wallet);
        helper.validatePositiveTransaction(funds);
        long newBalance = wallet.getBalance() - funds;
        helper.validateNonNegativeWalletBalance(newBalance);
        wallet.setBalance(newBalance);
        log.info("Funds subtracted: walletId={}, amount={}, newBalance={}",
                id, funds, wallet.getBalance());
    }
}
