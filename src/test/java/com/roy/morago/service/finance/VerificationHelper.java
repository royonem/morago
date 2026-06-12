package com.roy.morago.service.finance;

import com.roy.morago.entity.finance.Transaction;
import com.roy.morago.entity.finance.Wallet;
import com.roy.morago.entity.user.User;
import com.roy.morago.enums.CurrencyCode;
import com.roy.morago.enums.TransactionStatus;
import com.roy.morago.enums.TransactionType;
import com.roy.morago.enums.WalletStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Component
public class VerificationHelper {
    @Autowired
    private WalletService walletService;
    @Autowired
    private FinanceHelper financeHelper;

    public void verifyTransaction(Transaction transaction, TransactionType type, Long amount, User testClient, Wallet testWallet) {
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

    public void verifyTransactionStatus(TransactionStatus expectedStatus, Transaction testTransaction) {
        Transaction freshTransaction = financeHelper.findTransactionById(testTransaction.getId());
        assertThat(freshTransaction.getStatus()).isEqualTo(expectedStatus);
    }

    public void verifyWalletStatus(WalletStatus expectedStatus, Wallet testWallet) {
        Wallet freshWallet = walletService.findWalletById(testWallet.getId());
        assertThat(freshWallet.getStatus()).isEqualTo(expectedStatus);
    }

    public void verifyWalletBalance(Long expectedBalance, Wallet testWallet) {
        Wallet freshWallet = walletService.findWalletById(testWallet.getId());
        assertThat(freshWallet.getBalance()).isEqualTo(expectedBalance);
    }

    public void verifyTransactionIsProcessed(Boolean processed, Transaction testTransaction) {
        Transaction freshTransaction = financeHelper.findTransactionById(testTransaction.getId());
        if (processed) {
            assertThat(freshTransaction.getProcessedAt()).isNotNull();
        } else {
            assertThat(freshTransaction.getProcessedAt()).isNull();
        }
    }
}
