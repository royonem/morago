package com.roy.morago.service.finance;

import com.roy.morago.dto.finance.WalletResponse;
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
        log.info("Creating wallet for user={}", user.getId());
        Wallet wallet = new Wallet();
        wallet.setUser(user);
        wallet.setBalance(0L);
        wallet.setCurrencyCode(code);
        wallet.setStatus(WalletStatus.ACTIVE);
        user.setWallet(wallet);
        userRepository.save(user);
        walletRepository.save(wallet);
        log.info("Wallet created for user={}", user.getId());
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
        Wallet wallet = helper.findWalletById(id);
        log.info("Suspending wallet for user={}", wallet.getUser().getId());
        wallet.setStatus(WalletStatus.SUSPENDED);
        log.info("Wallet suspended for user={}", wallet.getUser().getId());
    }

    @Transactional
    public void blockWallet(Long id) {
        Wallet wallet = helper.findWalletById(id);
        log.info("Blocking wallet for user={}", wallet.getUser().getId());
        wallet.setStatus(WalletStatus.BLOCKED);
        log.info("Wallet blocked for user={}", wallet.getUser().getId());
    }

    @Transactional
    public void activateWallet(Long id) {
        Wallet wallet = helper.findWalletById(id);
        log.info("Activating wallet for user={}", wallet.getUser().getId());
        wallet.setStatus(WalletStatus.ACTIVE);
        log.info("Wallet activated for user={}", wallet.getUser().getId());
    }

    @Transactional
    public void updateCurrency(Long id, CurrencyCode newCode) {
        Wallet wallet = helper.findWalletById(id);
        User user = wallet.getUser();
        log.info("Updating wallet currency for user={} with previous currency={}", user.getId(), wallet.getCurrencyCode());
        helper.validateNoPendingTransactions(user);
        helper.validateWalletIsActive(wallet);
        wallet.setCurrencyCode(newCode);
        log.info("Wallet updated for user={} with currency={}", user.getId(), newCode);
    }

    @Transactional
    public void addFunds(Long id, Long funds) {
        Wallet wallet = helper.findWalletById(id);
        log.info("Adding funds: userId={}, amount={}", wallet.getUser().getId(), funds);
        helper.validateWalletIsActive(wallet);
        helper.validatePositiveTransaction(funds);
        wallet.setBalance(wallet.getBalance() + funds);
        log.info("Funds added: userId={}, amount={}, newBalance={}",
                wallet.getUser().getId(), funds, wallet.getBalance());
    }

    @Transactional
    public void subtractFunds(Long id, Long funds) {
        Wallet wallet = helper.findWalletById(id);
        log.info("Subtracting funds: userId={}, amount={}", wallet.getUser().getId(), funds);
        helper.validateWalletIsActive(wallet);
        helper.validatePositiveTransaction(funds);
        long newBalance = wallet.getBalance() - funds;
        helper.validateNonNegativeWalletBalance(newBalance);
        wallet.setBalance(newBalance);
        log.info("Funds subtracted: userId={}, amount={}, newBalance={}",
                wallet.getUser().getId(), funds, wallet.getBalance());
    }
}
