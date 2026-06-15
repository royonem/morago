package com.roy.morago.service.finance;

import com.roy.morago.entity.finance.Transaction;
import com.roy.morago.entity.finance.Wallet;
import com.roy.morago.entity.finance.Withdrawal;
import com.roy.morago.entity.user.User;
import com.roy.morago.enums.*;
import com.roy.morago.repository.finance.TransactionRepository;
import com.roy.morago.repository.finance.WithdrawalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Component
public class VerificationHelper {
    @Autowired
    private FinanceHelper financeHelper;
    @Autowired
    private WithdrawalRepository withdrawalRepository;

    // Transaction
    protected void verifyTransaction(Transaction transaction, TransactionType type, Long amount, User testClient, Wallet testWallet) {
        assertNotNull(transaction.getId());
        assertThat(transaction.getAmount()).isEqualTo(amount);
        assertThat(transaction.getBalanceBefore()).isEqualTo(1000L);
        if (type == TransactionType.WITHDRAWAL || type == TransactionType.CALL_CHARGE) {
            assertThat(transaction.getBalanceAfter()).isEqualTo(1000L - amount);
        } else if (type == TransactionType.DEPOSIT || type == TransactionType.CALL_EARNING) {
            assertThat(transaction.getBalanceAfter()).isEqualTo(1000L + amount);
        }
        assertThat(transaction.getWallet().getUser()).isEqualTo(testClient);
        assertThat(transaction.getWallet()).isEqualTo(testWallet);
        assertThat(transaction.getCurrencyCode()).isEqualTo(CurrencyCode.KRW);
        assertThat(transaction.getStatus()).isEqualTo(TransactionStatus.PAID);
        assertNotNull(transaction.getCreatedAt());
    }

    protected void verifyTransactionStatus(TransactionStatus expectedStatus, Transaction testTransaction) {
        Transaction freshTransaction = financeHelper.findTransactionById(testTransaction.getId());
        assertThat(freshTransaction.getStatus()).isEqualTo(expectedStatus);
    }

    protected void verifyTransactionIsProcessed(Boolean processed, Transaction testTransaction) {
        Transaction freshTransaction = financeHelper.findTransactionById(testTransaction.getId());
        if (processed) {
            assertThat(freshTransaction.getProcessedAt()).isNotNull();
        } else {
            assertThat(freshTransaction.getProcessedAt()).isNull();
        }
    }

    // Wallet
    protected void verifyWalletStatus(WalletStatus expectedStatus, Wallet testWallet) {
        Wallet freshWallet = financeHelper.findWalletById(testWallet.getId());
        assertThat(freshWallet.getStatus()).isEqualTo(expectedStatus);
    }

    protected void verifyWalletBalance(Long expectedBalance, Wallet testWallet) {
        Wallet freshWallet = financeHelper.findWalletById(testWallet.getId());
        assertThat(freshWallet.getBalance()).isEqualTo(expectedBalance);
    }

    // WithdrawalRequest
    protected void verifyWithdrawalTransaction(Transaction transaction) {
        assertNotNull(transaction.getId());
        assertThat(transaction.getType()).isEqualTo(TransactionType.WITHDRAWAL);
        assertThat(transaction.getAmount()).isEqualTo(100L);
        assertThat(transaction.getBalanceBefore()).isEqualTo(1000L);
        assertThat(transaction.getBalanceAfter()).isEqualTo(900L);
        assertThat(transaction.getCurrencyCode()).isEqualTo(CurrencyCode.KRW);
        assertThat(transaction.getStatus()).isEqualTo(TransactionStatus.PENDING);
        assertNotNull(transaction.getCreatedAt());
    }

    protected void verifyWithdrawal(Withdrawal withdrawal, User testUser) {
        assertNotNull(withdrawal.getId());
        assertThat(withdrawal.getAmount()).isEqualTo(100L);
        assertThat(withdrawal.getCurrencyCode()).isEqualTo(CurrencyCode.KRW);
        assertThat(withdrawal.getStatus()).isEqualTo(WithdrawalStatus.PENDING);
        assertNotNull(withdrawal.getCreatedAt());
        assertThat(withdrawal.getWallet()).isEqualTo(testUser.getWallet());
        assertThat(withdrawal.getRequester()).isEqualTo(testUser);
    }

    protected void verifyWithdrawalStatus(WithdrawalStatus expectedStatus, Withdrawal testWithdrawal) {
        Withdrawal freshWithdrawal = withdrawalRepository.findById(testWithdrawal.getId()).orElseThrow();
        assertThat(freshWithdrawal.getStatus()).isEqualTo(expectedStatus);
    }

    protected void verifyRequester(Long withdrawalId, User testUser) {
        Withdrawal withdrawal = financeHelper.findWithdrawalById(withdrawalId);
        assertThat(withdrawal.getRequester()).isEqualTo(testUser);
    }

    protected void verifyReview(Long withdrawalId, User testAdmin) {
        Withdrawal withdrawal = financeHelper.findWithdrawalById(withdrawalId);
        assertThat(withdrawal.getReviewer()).isEqualTo(testAdmin);
        assertThat(withdrawal.getReviewedAt()).isNotNull();
    }

    protected void verifyNoReview(Long withdrawalId) {
        Withdrawal withdrawal = financeHelper.findWithdrawalById(withdrawalId);
        assertThat(withdrawal.getReviewedAt()).isNull();
        assertThat(withdrawal.getReviewer()).isNull();
    }

    protected void verifyCorrectRejection(Long withdrawalId) {
        Withdrawal withdrawal = financeHelper.findWithdrawalById(withdrawalId);
        assertThat(withdrawal.getRejectionReason()).isEqualTo("Suspicious Transaction");
    }

    protected void verifyWithdrawalIsPaid(Long withdrawalId) {
        Withdrawal withdrawal = financeHelper.findWithdrawalById(withdrawalId);
        assertNotNull(withdrawal.getPaidAt());
    }
}
