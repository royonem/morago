package com.roy.morago.service.finance;

import com.roy.morago.dto.finance.TransactionRequest;
import com.roy.morago.dto.finance.TransactionResponse;
import com.roy.morago.entity.finance.Transaction;
import com.roy.morago.entity.finance.Wallet;
import com.roy.morago.entity.user.User;
import com.roy.morago.enums.*;
import com.roy.morago.exception.finance.DeficientFundsException;
import com.roy.morago.exception.finance.ExistingTransactionException;
import com.roy.morago.exception.finance.TransactionNotFoundException;
import com.roy.morago.service.SetupHelper;
import com.roy.morago.service.VerificationHelper;
import com.roy.morago.service.user.UserHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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
    @Autowired
    private UserHelper userHelper;

    @BeforeEach
    public void setUp() {
        User testUser = setUpHelper.createTestClient();
        testWallet = setUpHelper.createTestWallet(testUser);
    }

    @Test
    void testCreateDepositTransaction() {
        testTransaction = setUpHelper.createTestTransaction(TransactionType.DEPOSIT, 500L);
        verificationHelper.verifyWalletBalance(1500L, testWallet);
        verificationHelper.verifyTransactionStatus(TransactionStatus.PAID, testTransaction);
        verificationHelper.verifyTransactionIsProcessed(Boolean.TRUE, testTransaction);
    }

    @WithMockUser(username = "johndoe@test.com")
    @Test
    void testCreateDepositTransaction_new() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        TransactionRequest depositRequest = setUpHelper.createTestTransactionRequest(TransactionType.DEPOSIT, 900L);
        TransactionResponse depositResponse = transactionService.createDepositTransaction(depositRequest, authentication);
        verificationHelper.verifyWalletBalance(1900L, testWallet);
        assertThat(depositResponse.status().equals(TransactionStatus.PAID));
        assertThat(depositResponse.type()).isEqualTo(TransactionType.DEPOSIT);
        assertNotNull(depositResponse.processedAt());
    }

    @Test
    void testCreateCallChargeTransaction() {
        testTransaction = setUpHelper.createTestTransaction(TransactionType.CALL_CHARGE, 400L);
        verificationHelper.verifyWalletBalance(600L, testWallet);
        verificationHelper.verifyTransactionStatus(TransactionStatus.PAID, testTransaction);
        verificationHelper.verifyTransactionIsProcessed(Boolean.TRUE, testTransaction);
    }

    @Test
    void testCreateCallEarningTransaction() {
        testTransaction = setUpHelper.createTestTransaction(TransactionType.CALL_EARNING, 200L);
        verificationHelper.verifyWalletBalance(1200L, testWallet);
        verificationHelper.verifyTransactionStatus(TransactionStatus.PAID, testTransaction);
        verificationHelper.verifyTransactionIsProcessed(Boolean.TRUE, testTransaction);
    }

    @Test
    void testCancelTransaction() {
        testTransaction = setUpHelper.createPendingTestTransaction(TransactionType.DEPOSIT, 400L, testWallet);
        transactionService.cancelTransaction(testTransaction.getId());
        verificationHelper.verifyTransactionStatus(TransactionStatus.CANCELED, testTransaction);
        verificationHelper.verifyWalletBalance(1000L, testWallet);
        verificationHelper.verifyTransactionIsProcessed(Boolean.FALSE, testTransaction);
    }

    @Test
    void testGetTransaction() {
        testTransaction = setUpHelper.createTestTransaction(TransactionType.DEPOSIT, 500L);
        TransactionResponse response = transactionService.getTransaction(testTransaction.getId());
        assertThat(response.id()).isEqualTo(testTransaction.getId());
        assertThat(response.amount()).isEqualTo(500L);
        verificationHelper.verifyTransactionStatus(TransactionStatus.PAID, testTransaction);
    }

    @WithMockUser(username = "johndoe@test.com")
    @Test
    void testGetAllTransactions_withPagination() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userHelper.findUserWithAuthentication(authentication);

        for (int i = 1; i <= 50; i++) {
            TransactionRequest request = setUpHelper.createTestTransactionRequest(
                    TransactionType.DEPOSIT,
                    (long) (i * 100)
            );
            transactionService.createDepositTransaction(request, authentication);
        }
        Pageable firstPage = PageRequest.of(0, 20, Sort.by("id").descending());
        Page<TransactionResponse> page1 = transactionService.getTransactionsByUserId(user.getId(), firstPage);

        assertEquals(50, page1.getTotalElements());
        assertEquals(3, page1.getTotalPages());
        assertEquals(20, page1.getContent().size());
        assertEquals(0, page1.getNumber());
        assertEquals(5000L, page1.getContent().getFirst().amount());
        assertEquals(3100L, page1.getContent().getLast().amount());

        Pageable thirdPage = PageRequest.of(2, 20, Sort.by("id").descending());
        Page<TransactionResponse> page3 = transactionService.getTransactionsByUserId(user.getId(), thirdPage);
        assertEquals(10, page3.getContent().size());
        assertEquals(2, page3.getNumber());
        assertEquals(100L, page3.getContent().getLast().amount());
    }

    @Test
    void testGetTransaction_notFound_throwsException() {
        assertThatThrownBy(() -> transactionService.getTransaction(-1L))
                .isInstanceOf(TransactionNotFoundException.class);
    }

    @Test
    void testCreateCallChargeTransaction_insufficientFunds_throwsException() {
        assertThatThrownBy(() -> setUpHelper.createTestTransaction(TransactionType.CALL_CHARGE, 1100L))
                .isInstanceOf(DeficientFundsException.class);
    }

    @Test
    void testCreateTransaction_existingPending_throwsException() {
        testTransaction = setUpHelper.createPendingTestTransaction(TransactionType.CALL_CHARGE, 500L, testWallet);
        assertThatThrownBy(() -> setUpHelper.createTestTransaction(TransactionType.CALL_CHARGE, 500L))
                .isInstanceOf(ExistingTransactionException.class);
    }
}