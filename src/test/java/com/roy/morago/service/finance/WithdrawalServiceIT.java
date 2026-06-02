package com.roy.morago.service.finance;

import com.roy.morago.dto.finance.RejectWithdrawalDTO;
import com.roy.morago.dto.finance.WithdrawalRequestDTO;
import com.roy.morago.dto.finance.WithdrawalRequestResponse;
import com.roy.morago.entity.finance.Transaction;
import com.roy.morago.entity.finance.Wallet;
import com.roy.morago.entity.finance.WithdrawalRequest;
import com.roy.morago.entity.user.Role;
import com.roy.morago.entity.user.User;
import com.roy.morago.enums.*;
import com.roy.morago.exception.finance.DeficientFundsException;
import com.roy.morago.exception.finance.ExistingWithdrawalRequestException;
import com.roy.morago.exception.finance.InvalidWithdrawalStateException;
import com.roy.morago.exception.finance.WithdrawalNotFoundException;
import com.roy.morago.repository.finance.TransactionRepository;
import com.roy.morago.repository.finance.WalletRepository;
import com.roy.morago.repository.finance.WithdrawalRequestRepository;
import com.roy.morago.repository.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Transactional;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@Transactional
@SpringBootTest
public class WithdrawalServiceIT {
    @Autowired
    private WithdrawalService withdrawalService;
    @Autowired
    private WalletService walletService;
    @Autowired
    private WithdrawalRequestRepository withdrawalRequestRepository;
    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private WalletRepository walletRepository;

    private User testUser;
    private User testAdmin;
    private WithdrawalRequestDTO testRequestDto;
    private Transaction testTransaction;
    private WithdrawalRequest testRequest;
    private Wallet testWallet;
    private RejectWithdrawalDTO rejectionDto;

    //Helper Methods
    private void verifyWithdrawalTransaction(Transaction transaction) {
        assertThat(transaction.getId()).isNotNull();
        assertThat(transaction.getAmount()).isEqualTo(100L);
        assertThat(transaction.getBalanceBefore()).isEqualTo(1000L);
        assertThat(transaction.getBalanceAfter()).isEqualTo(900L);
        assertThat(transaction.getWallet()).isEqualTo(testWallet);
        assertThat(transaction.getCurrencyCode()).isEqualTo(CurrencyCode.KRW);
        assertThat(transaction.getStatus()).isEqualTo(TransactionStatus.PENDING);
        assertThat(transaction.getCreatedAt()).isNotNull();
    }

    private void verifyWithdrawalRequest(WithdrawalRequest withdrawalRequest) {
        assertThat(withdrawalRequest.getId()).isNotNull();
        assertThat(withdrawalRequest.getAmount()).isEqualTo(100L);
        assertThat(withdrawalRequest.getCurrencyCode()).isEqualTo(CurrencyCode.KRW);
        assertThat(withdrawalRequest.getStatus()).isEqualTo(WithdrawalStatus.PENDING);
        assertThat(withdrawalRequest.getCreatedAt()).isNotNull();
        assertThat(withdrawalRequest.getWallet()).isEqualTo(testWallet);
        assertThat(withdrawalRequest.getRequester()).isEqualTo(testUser);
        assertThat(withdrawalRequest.getTransaction()).isEqualTo(testTransaction);
    }

    private void verifyStatus(WithdrawalStatus wStatus, TransactionStatus tStatus) {
        Transaction freshTransaction = transactionRepository.findById(testTransaction.getId()).orElseThrow();
        WithdrawalRequest freshRequest = withdrawalRequestRepository.findById(testRequest.getId()).orElseThrow();
        assertThat(freshTransaction.getStatus()).isEqualTo(tStatus);
        assertThat(freshRequest.getStatus()).isEqualTo(wStatus);
    }

    private void verifyBalance(Long expectedBalance) {
        Wallet fresh = walletRepository.findById(testWallet.getId()).orElseThrow();
        assertThat(fresh.getBalance()).isEqualTo(expectedBalance);
    }

    private void verifyRequester(Long withdrawalRequestId) {
        WithdrawalRequest request = withdrawalRequestRepository.findById(withdrawalRequestId).orElseThrow();
        assertThat(request.getRequester()).isEqualTo(testUser);
    }

    private void verifyReview(Long withdrawalRequestId) {
        WithdrawalRequest request = withdrawalRequestRepository.findById(withdrawalRequestId).orElseThrow();
        assertThat(request.getReviewer()).isEqualTo(testAdmin);
        assertThat(request.getReviewedAt()).isNotNull();
    }

    private void verifyNonReview(Long withdrawalRequestId) {
        WithdrawalRequest request = withdrawalRequestRepository.findById(withdrawalRequestId).orElseThrow();
        assertThat(request.getReviewedAt()).isNull();
        assertThat(request.getReviewer()).isNull();
    }

    private void verifyRejection(Long withdrawalRequestId) {
        WithdrawalRequest request = withdrawalRequestRepository.findById(withdrawalRequestId).orElseThrow();
        assertThat(request.getRejectionReason()).isEqualTo("Suspicious Transaction");
    }

    private void verifyPaid(Long withdrawalRequestId) {
        WithdrawalRequest request = withdrawalRequestRepository.findById(withdrawalRequestId).orElseThrow();
        assertThat(request.getPaidAt()).isNotNull();
    }

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

    private WithdrawalRequestDTO createTestWithdrawalRequestDTO(Long withdrawalAmount) {
        testRequestDto = new WithdrawalRequestDTO();
        testRequestDto.setAmount(withdrawalAmount);
        testRequestDto.setCurrencyCode(CurrencyCode.KRW);
        return testRequestDto;
    }

    private void createTestWithdrawalRequest() {
        // create User Auth to create withdrawalRequest
        Authentication userAuth = new UsernamePasswordAuthenticationToken("johndoe@test.com", null);
        WithdrawalRequestResponse responseDto = withdrawalService.createWithdrawalRequest(testRequestDto, userAuth);

        // find and save transaction + withdrawalRequest
        Long transactionId = responseDto.getTransactionId();
        testTransaction= transactionRepository.findById(transactionId).orElseThrow();
        testRequest = withdrawalRequestRepository.findByTransaction(testTransaction);

        // verify that the transaction and withdrawalRequest entities were made correctly
        verifyWithdrawalTransaction(testTransaction);
        verifyWithdrawalRequest(testRequest);
    }

    // Test Set Up
    @BeforeEach
    public void setUp() {
        // create test user, admin, user_wallet, WithdrawalRequestDTO, rejectionDTO
        testUser = createTestUser();
        testAdmin = createTestAdmin();
        testWallet = createTestWallet();
        testRequestDto = createTestWithdrawalRequestDTO(100L);
        rejectionDto = new RejectWithdrawalDTO("Suspicious Transaction");

        createTestWithdrawalRequest();
    }

    // Standard Test Methods
    @WithMockUser(username = "johndoe@test.com")
    @Test
    void testCancelWithdrawalRequest() {
        withdrawalService.cancelWithdrawalRequest(testRequest.getId());

        verifyStatus(WithdrawalStatus.CANCELED, TransactionStatus.CANCELED);
        verifyBalance(1000L);
        verifyRequester(testRequest.getId());
    }

    @WithMockUser(username = "admin@test.com")
    @Test
    void testRejectWithdrawalRequest() {
        Authentication adminAuth = SecurityContextHolder.getContext().getAuthentication();
        withdrawalService.rejectWithdrawalRequest(testRequest.getId(), adminAuth, rejectionDto);

        verifyStatus(WithdrawalStatus.REJECTED, TransactionStatus.FAILED);
        verifyBalance(1000L);
        verifyReview(testRequest.getId());
        verifyRequester(testRequest.getId());
        verifyRejection(testRequest.getId());
    }

    @WithMockUser(username = "admin@test.com")
    @Test
    void testApproveWithdrawalRequest() {
        Authentication adminAuth = SecurityContextHolder.getContext().getAuthentication();
        withdrawalService.approveWithdrawalRequest(testRequest.getId(), adminAuth);

        verifyStatus(WithdrawalStatus.APPROVED, TransactionStatus.PAID);
        verifyBalance(900L);
        verifyReview(testRequest.getId());
        verifyRequester(testRequest.getId());
    }

    @WithMockUser(username = "johndoe@test.com")
    @Test
    void testGetWithdrawalRequestByTransactionId() {
        WithdrawalRequestResponse response = withdrawalService
                .getWithdrawalRequestByTransactionId(testTransaction.getId());

        assertThat(response.getTransactionId()).isEqualTo(testTransaction.getId());
        assertThat(response.getAmount()).isEqualTo(100L);
        assertThat(response.getCurrencyCode()).isEqualTo(CurrencyCode.KRW);
        assertThat(response.getStatus()).isEqualTo(TransactionStatus.PENDING);
    }

    // Test Already Approved
    @Test
    void testCancelWithdrawalRequest_alreadyApproved_throwsException() {
        Authentication adminAuth = new UsernamePasswordAuthenticationToken("admin@test.com", null);
        withdrawalService.approveWithdrawalRequest(testRequest.getId(), adminAuth);
        Authentication userAuth = new UsernamePasswordAuthenticationToken("johndoe@test.com", null);
        SecurityContextHolder.getContext().setAuthentication(userAuth);

        assertThatThrownBy(() -> withdrawalService.cancelWithdrawalRequest(testRequest.getId()))
                .isInstanceOf(InvalidWithdrawalStateException.class);
        verifyStatus(WithdrawalStatus.APPROVED, TransactionStatus.PAID);
        verifyBalance(900L);
        verifyRequester(testRequest.getId());
        verifyReview(testRequest.getId());
        verifyPaid(testRequest.getId());
    }

    @WithMockUser(username = "admin@test.com")
    @Test
    void testRejectWithdrawalRequest_alreadyApproved_throwsException() {
        Authentication adminAuth = SecurityContextHolder.getContext().getAuthentication();
        withdrawalService.approveWithdrawalRequest(testRequest.getId(), adminAuth);
        assertThatThrownBy(() -> withdrawalService.rejectWithdrawalRequest(testRequest.getId(), adminAuth, rejectionDto))
                .isInstanceOf(InvalidWithdrawalStateException.class);
        verifyStatus(WithdrawalStatus.APPROVED, TransactionStatus.PAID);
        verifyBalance(900L);
        verifyRequester(testRequest.getId());
        verifyReview(testRequest.getId());
        verifyPaid(testRequest.getId());
    }

    @WithMockUser(username = "admin@test.com")
    @Test
    void testApproveWithdrawalRequest_alreadyApproved_throwsException() {
        Authentication adminAuth = SecurityContextHolder.getContext().getAuthentication();
        withdrawalService.approveWithdrawalRequest(testRequest.getId(), adminAuth);
        assertThatThrownBy(() -> withdrawalService.approveWithdrawalRequest(testRequest.getId(), adminAuth))
                .isInstanceOf(InvalidWithdrawalStateException.class);
        verifyStatus(WithdrawalStatus.APPROVED, TransactionStatus.PAID);
        verifyBalance(900L);
        verifyRequester(testRequest.getId());
        verifyReview(testRequest.getId());
        verifyPaid(testRequest.getId());
    }

    // Test Already Canceled
    @Test
    void testRejectWithdrawalRequest_alreadyCanceled_throwsException() {
        Authentication userAuth = new UsernamePasswordAuthenticationToken("johndoe@test.com", null);
        SecurityContextHolder.getContext().setAuthentication(userAuth);
        withdrawalService.cancelWithdrawalRequest(testRequest.getId());

        Authentication adminAuth = new UsernamePasswordAuthenticationToken("admin@test.com", null);
        assertThatThrownBy(() -> withdrawalService.rejectWithdrawalRequest(testRequest.getId(), adminAuth, rejectionDto))
                .isInstanceOf(InvalidWithdrawalStateException.class);
        verifyStatus(WithdrawalStatus.CANCELED, TransactionStatus.CANCELED);
        verifyBalance(1000L);
        verifyRequester(testRequest.getId());
        verifyNonReview(testRequest.getId());
    }

    @Test
    void testApproveWithdrawalRequest_alreadyCanceled_throwsException() {
        Authentication userAuth = new UsernamePasswordAuthenticationToken("johndoe@test.com", null);
        SecurityContextHolder.getContext().setAuthentication(userAuth);
        withdrawalService.cancelWithdrawalRequest(testRequest.getId());
        Authentication adminAuth = new UsernamePasswordAuthenticationToken("admin@test.com", null);
        assertThatThrownBy(() -> withdrawalService.approveWithdrawalRequest(testRequest.getId(), adminAuth))
                .isInstanceOf(InvalidWithdrawalStateException.class);
        verifyStatus(WithdrawalStatus.CANCELED, TransactionStatus.CANCELED);
        verifyBalance(1000L);
        verifyRequester(testRequest.getId());
        verifyNonReview(testRequest.getId());
    }

    @WithMockUser(username = "johndoe@test.com")
    @Test
    void testCancelWithdrawalRequest_alreadyCanceled_throwsException() {
        withdrawalService.cancelWithdrawalRequest(testRequest.getId());
        assertThatThrownBy(() -> withdrawalService.cancelWithdrawalRequest(testRequest.getId()))
                .isInstanceOf(InvalidWithdrawalStateException.class);
        verifyStatus(WithdrawalStatus.CANCELED, TransactionStatus.CANCELED);
        verifyBalance(1000L);
        verifyRequester(testRequest.getId());
    }

    // Test Already Rejected
    @WithMockUser(username = "admin@test.com")
    @Test
    void testRejectWithdrawalRequest_alreadyRejected_throwsException() {
        Authentication adminAuth = SecurityContextHolder.getContext().getAuthentication();
        withdrawalService.rejectWithdrawalRequest(testRequest.getId(), adminAuth, rejectionDto);
        assertThatThrownBy(() -> withdrawalService.rejectWithdrawalRequest(testRequest.getId(), adminAuth, rejectionDto))
                .isInstanceOf(InvalidWithdrawalStateException.class);
        verifyStatus(WithdrawalStatus.REJECTED, TransactionStatus.FAILED);
        verifyBalance(1000L);
        verifyRequester(testRequest.getId());
        verifyReview(testRequest.getId());
    }

    @Test
    void testCancelWithdrawalRequest_alreadyRejected_throwsException() {
        Authentication adminAuth = new UsernamePasswordAuthenticationToken("admin@test.com", null);
        withdrawalService.rejectWithdrawalRequest(testRequest.getId(), adminAuth, rejectionDto);

        Authentication userAuth = new UsernamePasswordAuthenticationToken("johndoe@test.com", null);
        SecurityContextHolder.getContext().setAuthentication(userAuth);
        assertThatThrownBy(() -> withdrawalService.cancelWithdrawalRequest(testRequest.getId()))
                .isInstanceOf(InvalidWithdrawalStateException.class);
        verifyStatus(WithdrawalStatus.REJECTED, TransactionStatus.FAILED);
        verifyBalance(1000L);
        verifyRequester(testRequest.getId());
        verifyReview(testRequest.getId());
    }

    @WithMockUser(username = "admin@test.com")
    @Test
    void testApproveWithdrawalRequest_alreadyRejected_throwsException() {
        Authentication adminAuth = SecurityContextHolder.getContext().getAuthentication();
        withdrawalService.rejectWithdrawalRequest(testRequest.getId(), adminAuth, rejectionDto);
        assertThatThrownBy(() -> withdrawalService.approveWithdrawalRequest(testRequest.getId(), adminAuth))
                .isInstanceOf(InvalidWithdrawalStateException.class);
        verifyStatus(WithdrawalStatus.REJECTED, TransactionStatus.FAILED);
        verifyBalance(1000L);
        verifyRequester(testRequest.getId());
        verifyReview(testRequest.getId());
    }

    // Test Create Failed Cases
    @WithMockUser(username = "johndoe@test.com")
    @Test
    void testCreateWithdrawalRequest_insufficientFunds_throwsException() {
        Authentication userAuth = SecurityContextHolder.getContext().getAuthentication();
        WithdrawalRequestDTO insufficientRequestDto = createTestWithdrawalRequestDTO(1100L);
        assertThatThrownBy(() -> withdrawalService.createWithdrawalRequest(insufficientRequestDto, userAuth))
                .isInstanceOf(DeficientFundsException.class);
    }

    @WithMockUser(username = "johndoe@test.com")
    @Test
    void testCreateWithdrawalRequest_existingPending_throwsException() {
        Authentication userAuth = SecurityContextHolder.getContext().getAuthentication();
        assertThatThrownBy(() -> withdrawalService.createWithdrawalRequest(testRequestDto, userAuth))
                .isInstanceOf(ExistingWithdrawalRequestException.class);
    }

    // Test Get Failed Case
    @WithMockUser(username = "johndoe@test.com")
    @Test
    void testGetWithdrawalRequestByTransactionId_notFound_throwsException() {
        assertThatThrownBy(() -> withdrawalService.getWithdrawalRequestByTransactionId(-1L))
                .isInstanceOf(WithdrawalNotFoundException.class);
    }
}
