package com.roy.morago.service.finance;

import com.roy.morago.dto.finance.TransactionDTO;
import com.roy.morago.dto.finance.TransactionResponse;
import com.roy.morago.entity.finance.Transaction;
import com.roy.morago.entity.finance.Wallet;
import com.roy.morago.entity.user.User;
import com.roy.morago.enums.*;
import com.roy.morago.exception.finance.DeficientFundsException;
import com.roy.morago.exception.finance.ExistingTransactionException;
import com.roy.morago.exception.finance.TransactionNotFoundException;
import com.roy.morago.mapper.TransactionMapper;
import com.roy.morago.repository.finance.TransactionRepository;
import com.roy.morago.repository.finance.WalletRepository;
import com.roy.morago.repository.user.UserRepository;
import com.roy.morago.service.user.UserHelper;
import com.roy.morago.service.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import java.util.Optional;
import java.util.UUID;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TransactionServiceTest {
    @InjectMocks
    private TransactionService transactionService;
    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private Authentication authentication;
    @Mock
    private UserService userService;
    @Mock
    private UserHelper userHelper;
    @Mock
    private UserRepository userRepository;
    @Mock
    private WalletService walletService;
    @Mock
    private WalletRepository walletRepository;
    @Mock
    private TransactionMapper transactionMapper;

    private User testUser;
    private Wallet testWallet;
    private TransactionDTO testTransactionDto;
    private TransactionResponse testResponse;
    private Transaction testTransaction;
    private Transaction testPendingTransaction;

    // Stubs
    private void stubFindUserWithAuthentication() {
        when(userHelper.findUserWithAuthentication(authentication))
                .thenReturn(testUser);
    }

    private void stubFindTransactionById() {
        when(transactionRepository.findById(testTransaction.getId()))
                .thenReturn(Optional.of(testTransaction));
    }

    private void stubFindPendingById() {
        when(transactionRepository.findById(testPendingTransaction.getId()))
                .thenReturn(Optional.of(testPendingTransaction));
    }

    private void stubMapFromDTO() {
        when(transactionMapper.createTransactionFromDto(any(TransactionDTO.class)))
                .thenReturn(testTransaction);
    }

    private void stubCreate() {
        stubMapFromDTO();
        stubFindUserWithAuthentication();
        stubFindTransactionById();
    }

    private void stubMapFromEntity() {
        when(transactionMapper.createTransactionResponse(any(Transaction.class)))
                .thenReturn(testResponse);
    }

    private void stubPendingTransaction() {
        when(transactionRepository.existsByWalletUserIdAndStatus(testUser.getId(), TransactionStatus.PENDING))
                .thenReturn(true);
    }

    // Setup and Helpers
    private void createTestUser() {
        testUser = new User();
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setEmail("johndoe@test.com");
        testUser.setPasswordHash("password");
        testUser.setPhone("010-1234-5678");
        testUser.setAvailability(Availability.IDLE);
        testUser.setStatus(UserStatus.VERIFIED);
    }

    private void createTestWallet() {
        testWallet = new Wallet();
        testWallet.setId(1L);
        testWallet.setBalance(1000L);
        testWallet.setCurrencyCode(CurrencyCode.KRW);
        testWallet.setStatus(WalletStatus.ACTIVE);
    }

    private void createTestTransactionDTO(TransactionType type, Long amount) {
        testTransactionDto = new TransactionDTO();
        testTransactionDto.setType(type);
        testTransactionDto.setAmount(amount);
        testTransactionDto.setCurrencyCode(CurrencyCode.KRW);
    }

    private void createTestResponseDTO() {
        testResponse = new TransactionResponse();
        testResponse.setId(testTransaction.getId());
        testResponse.setType(testTransaction.getType());
        testResponse.setAmount(testTransaction.getAmount());
        testResponse.setCurrencyCode(testTransaction.getCurrencyCode());
        testResponse.setStatus(TransactionStatus.PAID);
    }

    private void createTestTransaction(TransactionType type, Long amount) {
        testTransaction = new Transaction();
        testTransaction.setId(1L);
        testTransaction.setType(type);
        testTransaction.setAmount(amount);
        testTransaction.setCurrencyCode(CurrencyCode.KRW);
    }

    private void createTestPendingTransaction(TransactionType type, Long amount) {
        testPendingTransaction = new Transaction();
        testPendingTransaction.setId(2L);
        testPendingTransaction.setWallet(testWallet);
        testPendingTransaction.setType(type);
        testPendingTransaction.setAmount(amount);
        testPendingTransaction.setCurrencyCode(CurrencyCode.KRW);
        testPendingTransaction.setStatus(TransactionStatus.PENDING);
        testPendingTransaction.setBalanceBefore(testWallet.getBalance());
        if (type == TransactionType.WITHDRAWAL || type == TransactionType.CALL_CHARGE) {
            testPendingTransaction.setBalanceAfter(testWallet.getBalance() - amount);
        } else {
            testPendingTransaction.setBalanceAfter(testWallet.getBalance() + amount);
        }
        testPendingTransaction.setReference(generateTransactionReference(type));
        testPendingTransaction.setDescription(generateTransactionDescription(type, amount));
    }

    private String generateTransactionReference(TransactionType type) {
        String random = UUID.randomUUID().toString();
        return type + "-" + random;
    }

    private String generateTransactionDescription(TransactionType type, Long amount) {
        return type + " transaction of " + amount;
    }

    @BeforeEach
    public void setUp() {
        createTestUser();
        createTestWallet();
        testUser.setWallet(testWallet);
        testWallet.setUser(testUser);
    }

    // Standard Tests
    @Test
    void testCreateDepositTransaction() {
        createTestTransactionDTO(TransactionType.DEPOSIT, 1000L);
        createTestTransaction(TransactionType.DEPOSIT, 1000L);
        stubCreate();
        transactionService.createTransaction(testTransactionDto, authentication);
        assertThat(testWallet.getBalance()).isEqualTo(2000L);
    }

    @Test
    void testCreateCallChargeTransaction() {
        createTestTransactionDTO(TransactionType.CALL_CHARGE, 700L);
        createTestTransaction(TransactionType.CALL_CHARGE, 700L);
        stubCreate();
        transactionService.createTransaction(testTransactionDto, authentication);
        assertThat(testWallet.getBalance()).isEqualTo(300L);
    }

    @Test
    void testCreateCallEarningTransaction() {
        createTestTransactionDTO(TransactionType.CALL_EARNING, 5000L);
        createTestTransaction(TransactionType.CALL_EARNING, 5000L);
        stubCreate();
        transactionService.createTransaction(testTransactionDto, authentication);
        assertThat(testWallet.getBalance()).isEqualTo(6000L);
    }

    @Test
    void testCancelTransaction() {
        createTestPendingTransaction(TransactionType.DEPOSIT, 400L);
        stubFindPendingById();
        transactionService.cancelTransaction(testPendingTransaction.getId());
        assertThat(testWallet.getBalance()).isEqualTo(1000L);
        assertThat(testPendingTransaction.getStatus()).isEqualTo(TransactionStatus.CANCELED);
    }

    @Test
    void testGetTransaction() {
        createTestTransaction(TransactionType.DEPOSIT, 500L);
        createTestResponseDTO();
        stubFindTransactionById();
        stubMapFromEntity();
        TransactionResponse response = transactionService.getTransaction(testTransaction.getId());
        assertThat(response.getId()).isEqualTo(testTransaction.getId());
        assertThat(response.getAmount()).isEqualTo(500L);
        assertThat(response.getCurrencyCode()).isEqualTo(CurrencyCode.KRW);
        assertThat(response.getStatus()).isEqualTo(TransactionStatus.PAID);
    }

    // Failed Tests
    @Test
    void testGetTransaction_notFound_throwsException() {
        assertThatThrownBy(() -> transactionService.getTransaction(-1L))
                .isInstanceOf(TransactionNotFoundException.class);
    }

    @Test
    void testCreateCallChargeTransaction_insufficientFunds_throwsException() {
        createTestTransactionDTO(TransactionType.CALL_CHARGE, 1200L);
        stubFindUserWithAuthentication();
        assertThatThrownBy(() -> transactionService.createTransaction(testTransactionDto, authentication))
                .isInstanceOf(DeficientFundsException.class);
    }

    @Test
    void testCreateTransaction_existingPending_throwsException() {
        // Create 1st Transaction (Pending)
        createTestPendingTransaction(TransactionType.WITHDRAWAL, 500L);

        // Create 2nd Transaction
        createTestTransactionDTO(TransactionType.DEPOSIT, 800L);
        stubFindUserWithAuthentication();
        stubPendingTransaction();

        assertThatThrownBy(() -> transactionService.createTransaction(testTransactionDto, authentication))
                .isInstanceOf(ExistingTransactionException.class);
    }
}