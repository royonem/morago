package com.roy.morago.service.finance;

import com.roy.morago.dto.finance.TransactionResponse;
import com.roy.morago.entity.finance.Transaction;
import com.roy.morago.entity.finance.Wallet;
import com.roy.morago.entity.user.User;
import com.roy.morago.enums.*;
import com.roy.morago.exception.finance.DeficientFundsException;
import com.roy.morago.exception.finance.ExistingTransactionException;
import com.roy.morago.exception.finance.TransactionNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@SuppressWarnings("SpringBootApplicationProperties")
@Testcontainers
@Transactional
@SpringBootTest(properties = {
        "app.jwt.secret=vLp7X9mQ2sR8tY3uF5jH6kL1nB4cD8eF0gH2jK3lP5qR7tY9u"
})
public class TransactionServiceIT {
    @Container
    @ServiceConnection
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8");

    @Autowired
    private TransactionService transactionService;
    @Autowired
    private SetupHelper setUpHelper;
    @Autowired
    private VerificationHelper verificationHelper;

    private Wallet testWallet;
    private Transaction testTransaction;

    @BeforeEach
    public void setUp() {
        User testUser = setUpHelper.createTestClient();
        testWallet = setUpHelper.createTestWallet(testUser);
    }

    @WithMockUser(username = "johndoe@test.com")
    @Test
    void testCreateDepositTransaction() {
        testTransaction = setUpHelper.createTestTransaction(TransactionType.DEPOSIT, 500L);
        verificationHelper.verifyWalletBalance(1500L, testWallet);
        verificationHelper.verifyTransactionStatus(TransactionStatus.PAID, testTransaction);
        verificationHelper.verifyTransactionIsProcessed(Boolean.TRUE, testTransaction);
    }

    @WithMockUser(username = "johndoe@test.com")
    @Test
    void testCreateCallChargeTransaction() {
        testTransaction = setUpHelper.createTestTransaction(TransactionType.CALL_CHARGE, 400L);
        verificationHelper.verifyWalletBalance(600L, testWallet);
        verificationHelper.verifyTransactionStatus(TransactionStatus.PAID, testTransaction);
        verificationHelper.verifyTransactionIsProcessed(Boolean.TRUE, testTransaction);
    }

    @WithMockUser(username = "johndoe@test.com")
    @Test
    void testCreateCallEarningTransaction() {
        testTransaction = setUpHelper.createTestTransaction(TransactionType.CALL_EARNING, 200L);
        verificationHelper.verifyWalletBalance(1200L, testWallet);
        verificationHelper.verifyTransactionStatus(TransactionStatus.PAID, testTransaction);
        verificationHelper.verifyTransactionIsProcessed(Boolean.TRUE, testTransaction);
    }

    @WithMockUser(username = "johndoe@test.com")
    @Test
    void testCancelTransaction() {
        testTransaction = setUpHelper.createPendingTestTransaction(TransactionType.DEPOSIT, 400L, testWallet);
        transactionService.cancelTransaction(testTransaction.getId());
        verificationHelper.verifyTransactionStatus(TransactionStatus.CANCELED, testTransaction);
        verificationHelper.verifyWalletBalance(1000L, testWallet);
        verificationHelper.verifyTransactionIsProcessed(Boolean.FALSE, testTransaction);
    }

    @WithMockUser(username = "johndoe@test.com")
    @Test
    void testGetTransaction() {
        testTransaction = setUpHelper.createTestTransaction(TransactionType.DEPOSIT, 500L);
        TransactionResponse response = transactionService.getTransaction(testTransaction.getId());
        assertThat(response.id()).isEqualTo(testTransaction.getId());
        assertThat(response.amount()).isEqualTo(500L);
        verificationHelper.verifyTransactionStatus(TransactionStatus.PAID,  testTransaction);
    }

    @WithMockUser(username = "johndoe@test.com")
    @Test
    void testGetTransaction_notFound_throwsException() {
        assertThatThrownBy(() -> transactionService.getTransaction(-1L))
                .isInstanceOf(TransactionNotFoundException.class);
    }

    @WithMockUser(username = "johndoe@test.com")
    @Test
    void testCreateCallChargeTransaction_insufficientFunds_throwsException() {
        assertThatThrownBy(() -> setUpHelper.createTestTransaction(TransactionType.CALL_CHARGE, 1100L))
                .isInstanceOf(DeficientFundsException.class);
    }

    @WithMockUser(username = "johndoe@test.com")
    @Test
    void testCreateTransaction_existingPending_throwsException() {
        testTransaction = setUpHelper.createPendingTestTransaction(TransactionType.CALL_CHARGE, 500L,  testWallet);
        assertThatThrownBy(() -> setUpHelper.createTestTransaction(TransactionType.CALL_CHARGE, 500L))
                .isInstanceOf(ExistingTransactionException.class);
    }
}