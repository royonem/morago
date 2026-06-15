package com.roy.morago.service.finance;

import com.roy.morago.entity.finance.BankAccount;
import com.roy.morago.entity.finance.Transaction;
import com.roy.morago.entity.finance.Wallet;
import com.roy.morago.entity.finance.Withdrawal;
import com.roy.morago.entity.user.User;
import com.roy.morago.enums.TransactionStatus;
import com.roy.morago.enums.TransactionType;
import com.roy.morago.enums.WalletStatus;
import com.roy.morago.enums.WithdrawalStatus;
import com.roy.morago.exception.finance.*;
import com.roy.morago.repository.finance.BankRepository;
import com.roy.morago.repository.finance.TransactionRepository;
import com.roy.morago.repository.finance.WalletRepository;
import com.roy.morago.repository.finance.WithdrawalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class FinanceHelper {
    private final TransactionRepository transactionRepository;
    private final WithdrawalRepository withdrawalRepository;
    private final WalletRepository walletRepository;
    private final BankRepository bankRepository;

    // Find Helpers
    protected Transaction findTransactionById(Long id) {
        return transactionRepository.findById(id)
                .orElseThrow(() -> new TransactionNotFoundException("Transaction not found"));
    }

    protected Withdrawal findWithdrawalById(Long id) {
        return withdrawalRepository.findById(id)
                .orElseThrow(() -> new WithdrawalNotFoundException("Withdrawal not found"));
    }

    protected Wallet findWalletById(Long id) {
        return walletRepository.findById(id)
                .orElseThrow(() -> new WalletNotFoundException("Wallet not found"));
    }

    protected BankAccount findBankAccountById(Long id) {
        return bankRepository.findById(id).orElseThrow(()
                -> new BankNotFoundException("Bank account not found"));
    }

    // Validation Helpers
    protected void validateNoPendingTransactions(User user) {
        if (transactionRepository.existsByWalletUserIdAndStatus(user.getId(), TransactionStatus.PENDING)) {
            throw new ExistingTransactionException("Pending transaction already exists.");
        }
    }

    protected void validateNoOtherPendingTransactions(User user, Long transactionId) {
        if (transactionRepository.existsByWalletUserIdAndStatusAndIdNot
                (user.getId(), TransactionStatus.PENDING, transactionId)) {
            throw new ExistingTransactionException("ending transaction already exists.");
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

    protected void validatePositiveTransaction(Long amount) {
        if (amount <= 0) {
            throw new NonPositiveTransactionException("Transactions must include a positive amount.");
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

    protected void validateNoPendingWithdrawals(User user) {
        if (withdrawalRepository.existsByRequesterAndStatus(user, WithdrawalStatus.PENDING)) {
            throw new ExistingWithdrawalException("Pending withdrawal request already exists.");
        }
    }

    protected void validateSufficientWalletBalance(Long withdrawalAmount, Wallet wallet) {
        if (withdrawalAmount > wallet.getBalance()) {
            throw new DeficientFundsException("Request amount exceeds balance.");
        }
    }

    protected void validateWithdrawalIsPending(Withdrawal withdrawal) {
        if (withdrawal.getStatus() != WithdrawalStatus.PENDING) {
            throw new InvalidWithdrawalStateException("Withdrawal state is invalid.");
        } else if (withdrawal.getTransaction().getStatus() != TransactionStatus.PENDING) {
            throw new InvalidTransactionStateException("Transaction is not pending.");
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
