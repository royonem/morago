package com.roy.morago.service.finance;

import com.roy.morago.dto.finance.WithdrawalRejection;
import com.roy.morago.dto.finance.WithdrawalRequest;
import com.roy.morago.dto.finance.WithdrawalResponse;
import com.roy.morago.entity.finance.Transaction;
import com.roy.morago.entity.finance.Wallet;
import com.roy.morago.entity.finance.Withdrawal;
import com.roy.morago.entity.user.User;
import com.roy.morago.enums.*;
import com.roy.morago.exception.finance.DeficientFundsException;
import com.roy.morago.exception.finance.ExistingWithdrawalException;
import com.roy.morago.exception.finance.InvalidWithdrawalStateException;
import com.roy.morago.exception.finance.WithdrawalNotFoundException;
import com.roy.morago.repository.finance.TransactionRepository;
import com.roy.morago.service.SetupHelper;
import com.roy.morago.service.VerificationHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
public class WithdrawalServiceIT {
    @Container
    @ServiceConnection
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8");
    @Autowired
    private WithdrawalService withdrawalService;
    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    private SetupHelper setUpHelper;
    @Autowired
    private VerificationHelper verificationHelper;
    @Autowired
    private FinanceHelper financeHelper;

    private User testUser;
    private User testAdmin;
    private WithdrawalRequest testWithdrawalRequest;
    private Transaction testTransaction;
    private Withdrawal testWithdrawal;
    private Wallet testWallet;
    private WithdrawalRejection rejectionDto;

    @BeforeEach
    public void setUp() {
        testUser = setUpHelper.createTestClient();
        testAdmin = setUpHelper.createTestAdmin();
        testWallet = setUpHelper.createTestWallet(testUser);
        testWithdrawalRequest = setUpHelper.createTestWithdrawalRequest(100L);
        rejectionDto = new WithdrawalRejection("Suspicious Transaction");
    }

    // Standard Test Methods
    @Test
    void testCreateWithdrawal() {
        Authentication userAuth = new UsernamePasswordAuthenticationToken("johndoe@test.com", null);
        WithdrawalResponse withdrawalResponse = withdrawalService.createWithdrawal(testWithdrawalRequest, userAuth);

        Long withdrawalId = withdrawalResponse.id();
        testWithdrawal = financeHelper.findWithdrawalById(withdrawalId);
        testTransaction= transactionRepository.findByWithdrawal(testWithdrawal);
        testWallet = testWithdrawal.getWallet();

        verificationHelper.verifyWithdrawalTransaction(testTransaction);
        verificationHelper.verifyWithdrawal(testWithdrawal, testUser);
    }

    @WithMockUser(username = "johndoe@test.com")
    @Test
    void testCancelWithdrawalRequest() {
        testCreateWithdrawal();
        withdrawalService.cancelWithdrawal(testWithdrawal.getId());

        verificationHelper.verifyWithdrawalStatus(WithdrawalStatus.CANCELED, testWithdrawal);
        verificationHelper.verifyTransactionStatus(TransactionStatus.CANCELED, testTransaction);
        verificationHelper.verifyWalletBalance(1000L, testWallet);
        verificationHelper.verifyRequester(testWithdrawal.getId(), testUser);
    }

    @WithMockUser(username = "admin@test.com")
    @Test
    void testRejectWithdrawal() {
        testCreateWithdrawal();
        Authentication adminAuth = SecurityContextHolder.getContext().getAuthentication();
        withdrawalService.rejectWithdrawal(testWithdrawal.getId(), adminAuth, rejectionDto);

        verificationHelper.verifyWithdrawalStatus(WithdrawalStatus.REJECTED, testWithdrawal);
        verificationHelper.verifyTransactionStatus(TransactionStatus.FAILED, testTransaction);
        verificationHelper.verifyWalletBalance(1000L, testWallet);
        verificationHelper.verifyRequester(testWithdrawal.getId(), testUser);
        verificationHelper.verifyReview(testWithdrawal.getId(), testAdmin);
        verificationHelper.verifyCorrectRejection(testWithdrawal.getId());
    }

    @WithMockUser(username = "admin@test.com")
    @Test
    void testApproveWithdrawal() {
        testCreateWithdrawal();
        Authentication adminAuth = SecurityContextHolder.getContext().getAuthentication();
        withdrawalService.approveWithdrawal(testWithdrawal.getId(), adminAuth);

        verificationHelper.verifyWithdrawalStatus(WithdrawalStatus.APPROVED, testWithdrawal);
        verificationHelper.verifyTransactionStatus(TransactionStatus.PAID, testTransaction);
        verificationHelper.verifyWalletBalance(900L, testWallet);
        verificationHelper.verifyRequester(testWithdrawal.getId(), testUser);
        verificationHelper.verifyReview(testWithdrawal.getId(), testAdmin);
    }

    @WithMockUser(username = "johndoe@test.com")
    @Test
    void testGetWithdrawalRequest() {
        testCreateWithdrawal();
        WithdrawalResponse response = withdrawalService.getWithdrawal(testWithdrawal.getId());

        assertThat(response.id()).isEqualTo(testWithdrawal.getId());
        assertThat(response.amount()).isEqualTo(100L);
        assertThat(response.currencyCode()).isEqualTo(CurrencyCode.KRW);
        assertThat(response.status()).isEqualTo(WithdrawalStatus.PENDING);
    }

    // Test Already Approved
    @Test
    void testCancelWithdrawalRequest_alreadyApproved_throwsException() {
        testCreateWithdrawal();
        Authentication adminAuth = new UsernamePasswordAuthenticationToken("admin@test.com", null);
        withdrawalService.approveWithdrawal(testWithdrawal.getId(), adminAuth);
        Authentication userAuth = new UsernamePasswordAuthenticationToken("johndoe@test.com", null);
        SecurityContextHolder.getContext().setAuthentication(userAuth);

        assertThatThrownBy(() -> withdrawalService.cancelWithdrawal(testWithdrawal.getId()))
                .isInstanceOf(InvalidWithdrawalStateException.class);

        verificationHelper.verifyWithdrawalStatus(WithdrawalStatus.APPROVED, testWithdrawal);
        verificationHelper.verifyTransactionStatus(TransactionStatus.PAID, testTransaction);
        verificationHelper.verifyWalletBalance(900L, testWallet);
        verificationHelper.verifyRequester(testWithdrawal.getId(), testUser);
        verificationHelper.verifyReview(testWithdrawal.getId(), testAdmin);
    }

    @WithMockUser(username = "admin@test.com")
    @Test
    void testRejectWithdrawal_alreadyApproved_throwsException() {
        testCreateWithdrawal();
        Authentication adminAuth = SecurityContextHolder.getContext().getAuthentication();
        withdrawalService.approveWithdrawal(testWithdrawal.getId(), adminAuth);
        assertThatThrownBy(() -> withdrawalService.rejectWithdrawal(testWithdrawal.getId(), adminAuth, rejectionDto))
                .isInstanceOf(InvalidWithdrawalStateException.class);

        verificationHelper.verifyWithdrawalStatus(WithdrawalStatus.APPROVED, testWithdrawal);
        verificationHelper.verifyTransactionStatus(TransactionStatus.PAID, testTransaction);
        verificationHelper.verifyWalletBalance(900L, testWallet);
        verificationHelper.verifyRequester(testWithdrawal.getId(), testUser);
        verificationHelper.verifyReview(testWithdrawal.getId(), testAdmin);
    }

    @WithMockUser(username = "admin@test.com")
    @Test
    void testApproveWithdrawal_alreadyApproved_throwsException() {
        testCreateWithdrawal();
        Authentication adminAuth = SecurityContextHolder.getContext().getAuthentication();
        withdrawalService.approveWithdrawal(testWithdrawal.getId(), adminAuth);
        assertThatThrownBy(() -> withdrawalService.approveWithdrawal(testWithdrawal.getId(), adminAuth))
                .isInstanceOf(InvalidWithdrawalStateException.class);

        verificationHelper.verifyWithdrawalStatus(WithdrawalStatus.APPROVED, testWithdrawal);
        verificationHelper.verifyTransactionStatus(TransactionStatus.PAID, testTransaction);
        verificationHelper.verifyWalletBalance(900L, testWallet);
        verificationHelper.verifyRequester(testWithdrawal.getId(), testUser);
        verificationHelper.verifyReview(testWithdrawal.getId(), testAdmin);
    }

    // Test Already Canceled
    @Test
    void testRejectWithdrawal_alreadyCanceled_throwsException() {
        testCreateWithdrawal();
        Authentication userAuth = new UsernamePasswordAuthenticationToken("johndoe@test.com", null);
        SecurityContextHolder.getContext().setAuthentication(userAuth);
        withdrawalService.cancelWithdrawal(testWithdrawal.getId());

        Authentication adminAuth = new UsernamePasswordAuthenticationToken("admin@test.com", null);
        assertThatThrownBy(() -> withdrawalService.rejectWithdrawal(testWithdrawal.getId(), adminAuth, rejectionDto))
                .isInstanceOf(InvalidWithdrawalStateException.class);
        verificationHelper.verifyWithdrawalStatus(WithdrawalStatus.CANCELED, testWithdrawal);
        verificationHelper.verifyTransactionStatus(TransactionStatus.CANCELED, testTransaction);
        verificationHelper.verifyWalletBalance(1000L, testWallet);
        verificationHelper.verifyRequester(testWithdrawal.getId(), testUser);
        verificationHelper.verifyNoReview(testWithdrawal.getId());
    }

    @Test
    void testApproveWithdrawal_alreadyCanceled_throwsException() {
        testCreateWithdrawal();
        Authentication userAuth = new UsernamePasswordAuthenticationToken("johndoe@test.com", null);
        SecurityContextHolder.getContext().setAuthentication(userAuth);
        withdrawalService.cancelWithdrawal(testWithdrawal.getId());
        Authentication adminAuth = new UsernamePasswordAuthenticationToken("admin@test.com", null);
        assertThatThrownBy(() -> withdrawalService.approveWithdrawal(testWithdrawal.getId(), adminAuth))
                .isInstanceOf(InvalidWithdrawalStateException.class);
        verificationHelper.verifyWithdrawalStatus(WithdrawalStatus.CANCELED, testWithdrawal);
        verificationHelper.verifyTransactionStatus(TransactionStatus.CANCELED, testTransaction);
        verificationHelper.verifyWalletBalance(1000L, testWallet);
        verificationHelper.verifyRequester(testWithdrawal.getId(), testUser);
        verificationHelper.verifyNoReview(testWithdrawal.getId());
    }

    @WithMockUser(username = "johndoe@test.com")
    @Test
    void testCancelWithdrawalRequest_alreadyCanceled_throwsException() {
        testCreateWithdrawal();
        withdrawalService.cancelWithdrawal(testWithdrawal.getId());
        assertThatThrownBy(() -> withdrawalService.cancelWithdrawal(testWithdrawal.getId()))
                .isInstanceOf(InvalidWithdrawalStateException.class);
        verificationHelper.verifyWithdrawalStatus(WithdrawalStatus.CANCELED, testWithdrawal);
        verificationHelper.verifyTransactionStatus(TransactionStatus.CANCELED, testTransaction);
        verificationHelper.verifyWalletBalance(1000L, testWallet);
        verificationHelper.verifyRequester(testWithdrawal.getId(), testUser);
    }

    // Test Already Rejected
    @WithMockUser(username = "admin@test.com")
    @Test
    void testRejectWithdrawal_alreadyRejected_throwsException() {
        testCreateWithdrawal();
        Authentication adminAuth = SecurityContextHolder.getContext().getAuthentication();
        withdrawalService.rejectWithdrawal(testWithdrawal.getId(), adminAuth, rejectionDto);
        assertThatThrownBy(() -> withdrawalService.rejectWithdrawal(testWithdrawal.getId(), adminAuth, rejectionDto))
                .isInstanceOf(InvalidWithdrawalStateException.class);
        verificationHelper.verifyWithdrawalStatus(WithdrawalStatus.REJECTED, testWithdrawal);
        verificationHelper.verifyTransactionStatus(TransactionStatus.FAILED, testTransaction);

        verificationHelper.verifyWithdrawalStatus(WithdrawalStatus.REJECTED, testWithdrawal);
        verificationHelper.verifyTransactionStatus(TransactionStatus.FAILED, testTransaction);
        verificationHelper.verifyWalletBalance(1000L, testWallet);
        verificationHelper.verifyRequester(testWithdrawal.getId(), testUser);
        verificationHelper.verifyReview(testWithdrawal.getId(), testAdmin);
        verificationHelper.verifyCorrectRejection(testWithdrawal.getId());
    }

    @Test
    void testCancelWithdrawalRequest_alreadyRejected_throwsException() {
        testCreateWithdrawal();
        Authentication adminAuth = new UsernamePasswordAuthenticationToken("admin@test.com", null);
        withdrawalService.rejectWithdrawal(testWithdrawal.getId(), adminAuth, rejectionDto);

        Authentication userAuth = new UsernamePasswordAuthenticationToken("johndoe@test.com", null);
        SecurityContextHolder.getContext().setAuthentication(userAuth);
        assertThatThrownBy(() -> withdrawalService.cancelWithdrawal(testWithdrawal.getId()))
                .isInstanceOf(InvalidWithdrawalStateException.class);

        verificationHelper.verifyWithdrawalStatus(WithdrawalStatus.REJECTED, testWithdrawal);
        verificationHelper.verifyTransactionStatus(TransactionStatus.FAILED, testTransaction);
        verificationHelper.verifyWalletBalance(1000L, testWallet);
        verificationHelper.verifyRequester(testWithdrawal.getId(), testUser);
        verificationHelper.verifyReview(testWithdrawal.getId(), testAdmin);
        verificationHelper.verifyCorrectRejection(testWithdrawal.getId());
    }

    @WithMockUser(username = "admin@test.com")
    @Test
    void testApproveWithdrawal_alreadyRejected_throwsException() {
        testCreateWithdrawal();
        Authentication adminAuth = SecurityContextHolder.getContext().getAuthentication();
        withdrawalService.rejectWithdrawal(testWithdrawal.getId(), adminAuth, rejectionDto);
        assertThatThrownBy(() -> withdrawalService.approveWithdrawal(testWithdrawal.getId(), adminAuth))
                .isInstanceOf(InvalidWithdrawalStateException.class);
        verificationHelper.verifyWithdrawalStatus(WithdrawalStatus.REJECTED, testWithdrawal);
        verificationHelper.verifyTransactionStatus(TransactionStatus.FAILED, testTransaction);
        verificationHelper.verifyWalletBalance(1000L, testWallet);
        verificationHelper.verifyRequester(testWithdrawal.getId(), testUser);
        verificationHelper.verifyReview(testWithdrawal.getId(), testAdmin);
        verificationHelper.verifyCorrectRejection(testWithdrawal.getId());
    }

    // Test Create Failed Cases
    @WithMockUser(username = "johndoe@test.com")
    @Test
    void testCreateWithdrawalRequest_insufficientFunds_throwsException() {
        Authentication userAuth = SecurityContextHolder.getContext().getAuthentication();
        WithdrawalRequest insufficientRequestDto = setUpHelper.createTestWithdrawalRequest(1100L);
        assertThatThrownBy(() -> withdrawalService.createWithdrawal(insufficientRequestDto, userAuth))
                .isInstanceOf(DeficientFundsException.class);
    }

    @WithMockUser(username = "johndoe@test.com")
    @Test
    void testCreateWithdrawalRequest_existingPending_throwsException() {
        testCreateWithdrawal();
        Authentication userAuth = SecurityContextHolder.getContext().getAuthentication();
        assertThatThrownBy(() -> withdrawalService.createWithdrawal(testWithdrawalRequest, userAuth))
                .isInstanceOf(ExistingWithdrawalException.class);
    }

    // Test Get Failed Case
    @WithMockUser(username = "johndoe@test.com")
    @Test
    void testGetWithdrawalRequestByTransactionId_notFound_throwsException() {
        assertThatThrownBy(() -> withdrawalService.getWithdrawal(-1L))
                .isInstanceOf(WithdrawalNotFoundException.class);
    }
}
