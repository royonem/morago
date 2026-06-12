package com.roy.morago.service.finance;

import com.roy.morago.dto.finance.TransactionRequest;
import com.roy.morago.entity.finance.Transaction;
import com.roy.morago.entity.finance.Wallet;
import com.roy.morago.entity.finance.WithdrawalRequest;
import com.roy.morago.entity.user.User;
import com.roy.morago.enums.TransactionStatus;
import com.roy.morago.enums.TransactionType;
import com.roy.morago.enums.WalletStatus;
import com.roy.morago.exception.finance.*;
import com.roy.morago.mapper.TransactionMapper;
import com.roy.morago.repository.finance.TransactionRepository;
import com.roy.morago.repository.finance.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class FinanceHelper {
    private final TransactionRepository transactionRepository;
    private final TransactionMapper transactionMapper;
    private final WalletRepository walletRepository;

    // Find Helpers
    protected Transaction findTransaction(Long id) {
        return transactionRepository.findById(id)
                .orElseThrow(() -> new TransactionNotFoundException("Transaction not found"));
    }

    protected Wallet findWalletById(Long id) {
        return walletRepository.findById(id)
                .orElseThrow(() -> new WalletNotFoundException("Wallet not found"));
    }

    // Transaction Helpers
    protected Transaction createTransactionEntity(User user, TransactionRequest dto) {
        validateNoPendingTransactions(user);
        Transaction transaction = transactionMapper.createTransactionFromDto(dto);
        transaction.setWallet(user.getWallet());
        setBalanceBeforeAndAfter(transaction);
        transaction.setStatus(TransactionStatus.PENDING);
        transaction.setReference(generateTransactionReference(dto.type()));
        transaction.setDescription(generateTransactionDescription(dto.type(), dto.amount()));
        return transaction;
    }

    protected Transaction createWithdrawalTransaction(WithdrawalRequest request, User user) {
        Transaction transaction = new Transaction();
        transaction.setType(TransactionType.WITHDRAWAL);
        Wallet wallet = user.getWallet();
        transaction.setWallet(wallet);
        transaction.setAmount(request.getAmount());
        transaction.setCurrencyCode(request.getCurrencyCode());
        setBalanceBeforeAndAfter(transaction);

        transaction.setStatus(TransactionStatus.PENDING);
        transaction.setReference(generateTransactionReference(TransactionType.WITHDRAWAL));
        transaction.setDescription(generateTransactionDescription(TransactionType.WITHDRAWAL, request.getAmount()));

        transaction.setWithdrawalRequest(request);
        request.setTransaction(transaction);
        return transaction;
    }

    protected void processTransaction(Transaction transaction) {
        validateTransactionIsPending(transaction, "Error processing non-pending transaction.");

        setBalanceBeforeAndAfter(transaction);
        Wallet wallet = transaction.getWallet();
        wallet.setBalance(transaction.getBalanceAfter());
        walletRepository.save(wallet);
        transaction.setStatus(TransactionStatus.PAID);
        transaction.setProcessedAt(LocalDateTime.now());
    }

    protected void failTransaction(Transaction transaction) {
        transaction.setStatus(TransactionStatus.FAILED);
    }

    protected void setBalanceBeforeAndAfter(Transaction transaction) {
        Wallet wallet = transaction.getWallet();
        Long balanceBefore = wallet.getBalance();
        validateNonNegativeWalletBalance(balanceBefore);
        transaction.setBalanceBefore(balanceBefore);
        switch (transaction.getType()) {
            case DEPOSIT, CALL_EARNING ->
                    transaction.setBalanceAfter(balanceBefore + transaction.getAmount());
            case WITHDRAWAL, CALL_CHARGE ->
                    transaction.setBalanceAfter(balanceBefore - transaction.getAmount());
        }
        validateWalletBalanceAfter(transaction.getBalanceAfter());
    }

    // Validation Helpers
    protected void validateNoPendingTransactions(User user) {
        if (transactionRepository.existsByWalletUserIdAndStatus(user.getId(), TransactionStatus.PENDING)) {
            throw new ExistingTransactionException("Pending transaction already exists.");
        }
    }

    protected void validateTransactionIsPending(Transaction transaction, String message) {
        if (transaction.getStatus() != TransactionStatus.PENDING) {
            throw new InvalidTransactionStateException(message);
        }
    }

    protected void validateTransactionIsPaid(Transaction transaction) {
        if (transaction.getStatus() != TransactionStatus.PAID) {
            throw new InvalidTransactionStateException("Error validating transaction.");
        }
    }

    protected void validateWalletBalanceAfter(Long balanceAfter) {
        if (balanceAfter < 0L) {
            throw new DeficientFundsException("Not enough funds to complete transaction");
        }
    }

    protected void validateNonNegativeWalletBalance(Long balance) {
        if (balance < 0L) {
            throw new DeficientFundsException("Wallet does not have enough funds");
        }
    }

    protected void validateWalletIsActive(Wallet wallet) {
        if (wallet.getStatus() != WalletStatus.ACTIVE) {
            throw new NonActiveWalletException("Cannot access wallet.");
        }
    }

    protected void validatePositiveTransaction(Long amount) {
        if (amount <= 0) {
            throw new NonPositiveTransactionException("Transactions must include a positive amount.");
        }
    }

    // Generation Helpers
    protected String generateTransactionReference(TransactionType type) {
        String random = UUID.randomUUID().toString();
        return type + "-" + random;
    }

    protected String generateTransactionDescription(TransactionType type, Long amount) {
        return type + " transaction of " + amount;
    }

}
