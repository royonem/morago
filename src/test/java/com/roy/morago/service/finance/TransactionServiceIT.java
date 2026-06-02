package com.roy.morago.service.finance;

import com.roy.morago.dto.finance.TransactionDTO;
import com.roy.morago.dto.finance.TransactionResponse;
import com.roy.morago.entity.finance.Transaction;
import com.roy.morago.entity.finance.Wallet;
import com.roy.morago.entity.user.Role;
import com.roy.morago.entity.user.User;
import com.roy.morago.enums.*;
import com.roy.morago.exception.finance.DeficientFundsException;
import com.roy.morago.exception.finance.ExistingTransactionException;
import com.roy.morago.exception.finance.TransactionNotFoundException;
import com.roy.morago.repository.finance.TransactionRepository;
import com.roy.morago.repository.finance.WalletRepository;
import com.roy.morago.repository.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@Transactional
@SpringBootTest
public class TransactionServiceIT {
    @Autowired
    private TransactionService transactionService;
    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private WalletService walletService;
    @Autowired
    private WalletRepository walletRepository;

    private User testUser;
    private User testAdmin;
    private Wallet testWallet;
    private TransactionDTO testTransactionDto;
    private Transaction testTransaction;

    // Verification Methods
    private void verifyTransaction(Transaction transaction, TransactionType type, Long amount) {
        assertThat(transaction.getId()).isNotNull();
        assertThat(transaction.getAmount()).isEqualTo(amount);
        assertThat(transaction.getBalanceBefore()).isEqualTo(1000L);
        if (type == TransactionType.WITHDRAWAL || type == TransactionType.CALL_CHARGE) {
            assertThat(transaction.getBalanceAfter()).isEqualTo(1000L - amount);
        } else if (type == TransactionType.DEPOSIT || type == TransactionType.CALL_EARNING) {
            assertThat(transaction.getBalanceAfter()).isEqualTo(1000L + amount);
        }
        assertThat(transaction.getWallet()).isEqualTo(testWallet);
        assertThat(transaction.getCurrencyCode()).isEqualTo(CurrencyCode.KRW);
        assertThat(transaction.getStatus()).isEqualTo(TransactionStatus.PAID);
        assertThat(transaction.getCreatedAt()).isNotNull();
    }

    private void verifyStatus(TransactionStatus status) {
        Transaction freshTransaction = transactionRepository.findById(testTransaction.getId()).orElseThrow();
        assertThat(freshTransaction.getStatus()).isEqualTo(status);
    }

    private void verifyBalance(Long expectedBalance) {
        Wallet fresh = walletRepository.findById(testWallet.getId()).orElseThrow();
        assertThat(fresh.getBalance()).isEqualTo(expectedBalance);
    }

    private void verifyProcessed(Boolean processed) {
        Transaction freshTransaction = transactionRepository.findById(testTransaction.getId()).orElseThrow();
        if (processed) {
            assertThat(freshTransaction.getProcessedAt()).isNotNull();
        } else {
            assertThat(freshTransaction.getProcessedAt()).isNull();
        }
    }

    // Helper Methods and Setup
    private User createTestUser() {
        testUser = new User();
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setEmail("johndoe@test.com");
        testUser.setPasswordHash("password");
        testUser.setPhone("010-1234-5678");
        testUser.setAvailability(Availability.IDLE);
        testUser.setStatus(UserStatus.VERIFIED);
        return userRepository.save(testUser);
    }

    private User createTestAdmin() {
        testAdmin = new User();

        testAdmin.setFirstName("Best");
        testAdmin.setLastName("Admin");
        testAdmin.setEmail("admin@test.com");
        testAdmin.setPasswordHash("admin_password");
        testAdmin.setPhone("010-9999-9999");
        testAdmin.setAvailability(Availability.IDLE);
        testAdmin.setStatus(UserStatus.VERIFIED);
        Role adminRole = new Role();
        adminRole.setName("ROLE_ADMIN");
        testAdmin.getRoles().add(adminRole);
        return userRepository.save(testAdmin);
    }

    private Wallet createTestWallet() {
        walletService.createWallet(testUser, CurrencyCode.KRW);
        testWallet = walletRepository.findByUserId(testUser.getId()).orElseThrow();
        walletService.addFunds(testWallet.getId(), 1000L);
        return walletRepository.save(testWallet);
    }

    private void createTestTransactionDTO(TransactionType type, Long amount) {
        testTransactionDto = new TransactionDTO();
        testTransactionDto.setType(type);
        testTransactionDto.setAmount(amount);
        testTransactionDto.setCurrencyCode(CurrencyCode.KRW);
        testTransactionDto.setDescription("test transaction");
        testTransactionDto.setReference("test reference");
    }

    private void testCreateTransaction(TransactionType type, Long amount) {
        Authentication userAuth = new UsernamePasswordAuthenticationToken("johndoe@test.com", null);

        createTestTransactionDTO(type, amount);
        TransactionResponse responseDto = transactionService.createTransaction(testTransactionDto, userAuth);

        Long transactionId = responseDto.getId();
        testTransaction = transactionRepository.findById(transactionId).orElseThrow();

        verifyTransaction(testTransaction, type, amount);
    }

    private String generateTransactionReference(TransactionType type) {
        String random = UUID.randomUUID().toString();
        return type + "-" + random;
    }

    private String generateTransactionDescription(TransactionType type, Long amount) {
        return type + " transaction of " + amount;
    }

    private void testCreatePendingTransaction(TransactionType type, Long amount) {
        Transaction pendingTransaction = new Transaction();
        pendingTransaction.setType(type);
        pendingTransaction.setAmount(amount);
        pendingTransaction.setCurrencyCode(CurrencyCode.KRW);
        pendingTransaction.setDescription(generateTransactionDescription(type, amount));
        pendingTransaction.setReference(generateTransactionReference(type));
        pendingTransaction.setStatus(TransactionStatus.PENDING);
        pendingTransaction.setWallet(testWallet);
        pendingTransaction.setBalanceBefore(testWallet.getBalance());
        pendingTransaction.setBalanceAfter(testWallet.getBalance() + amount);
        testTransaction = transactionRepository.save(pendingTransaction);
    }

    @BeforeEach
    public void setUp() {
        testUser = createTestUser();
        testAdmin = createTestAdmin();
        testWallet = createTestWallet();
    }

    // Standard Tests
    @WithMockUser(username = "johndoe@test.com")
    @Test
    void testCreateDepositTransaction() {
        testCreateTransaction(TransactionType.DEPOSIT, 500L);
        verifyBalance(1500L);
        verifyStatus(TransactionStatus.PAID);
        verifyProcessed(Boolean.TRUE);
    }

    @WithMockUser(username = "johndoe@test.com")
    @Test
    void testCreateCallChargeTransaction() {
        testCreateTransaction(TransactionType.CALL_CHARGE, 400L);
        verifyBalance(600L);
        verifyStatus(TransactionStatus.PAID);
        verifyProcessed(Boolean.TRUE);
    }

    @WithMockUser(username = "johndoe@test.com")
    @Test
    void testCreateCallEarningTransaction() {
        testCreateTransaction(TransactionType.CALL_EARNING, 200L);
        verifyBalance(1200L);
        verifyStatus(TransactionStatus.PAID);
        verifyProcessed(Boolean.TRUE);
    }

    @WithMockUser(username = "johndoe@test.com")
    @Test
    void testCancelTransaction() {
        testCreatePendingTransaction(TransactionType.DEPOSIT, 400L);
        transactionService.cancelTransaction(testTransaction.getId());
        verifyStatus(TransactionStatus.CANCELED);
        verifyBalance(1000L);
        verifyProcessed(Boolean.FALSE);
    }

    @WithMockUser(username = "johndoe@test.com")
    @Test
    void testGetTransaction() {
        testCreateTransaction(TransactionType.DEPOSIT, 500L);
        TransactionResponse response = transactionService.getTransaction(testTransaction.getId());
        assertThat(response.getId()).isEqualTo(testTransaction.getId());
        assertThat(response.getAmount()).isEqualTo(500L);
        verifyStatus(TransactionStatus.PAID);
    }

    // Failed Tests
    @WithMockUser(username = "johndoe@test.com")
    @Test
    void testGetTransaction_notFound_throwsException() {
        assertThatThrownBy(() -> transactionService.getTransaction(-1L))
                .isInstanceOf(TransactionNotFoundException.class);
    }

    @WithMockUser(username = "johndoe@test.com")
    @Test
    void testCreateCallChargeTransaction_insufficientFunds_throwsException() {
        assertThatThrownBy(() -> testCreateTransaction(TransactionType.CALL_CHARGE, 1100L))
                .isInstanceOf(DeficientFundsException.class);
        // maybe move this to CallServiceTest later
    }

    @WithMockUser(username = "johndoe@test.com")
    @Test
    void testCreateTransaction_existingPending_throwsException() {
        testCreatePendingTransaction(TransactionType.CALL_CHARGE, 500L);
        assertThatThrownBy(() -> testCreateTransaction(TransactionType.CALL_CHARGE, 500L))
                .isInstanceOf(ExistingTransactionException.class);
    }
}