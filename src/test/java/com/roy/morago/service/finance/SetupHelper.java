package com.roy.morago.service.finance;

import com.roy.morago.dto.finance.TransactionRequest;
import com.roy.morago.dto.finance.TransactionResponse;
import com.roy.morago.entity.finance.Transaction;
import com.roy.morago.entity.finance.Wallet;
import com.roy.morago.entity.user.User;
import com.roy.morago.enums.*;
import com.roy.morago.repository.finance.TransactionRepository;
import com.roy.morago.repository.finance.WalletRepository;
import com.roy.morago.repository.user.UserRepository;
import com.roy.morago.service.user.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class SetupHelper {
    @Autowired
    private WalletService walletService;
    @Autowired
    private WalletRepository walletRepository;
    @Autowired
    private TransactionService transactionService;
    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    private FinanceHelper financeHelper;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleService roleService;
    @Autowired
    private VerificationHelper verificationHelper;


    public User createTestClient() {
        User testUser = new User();
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setEmail("johndoe@test.com");
        testUser.setPasswordHash("password");
        testUser.setPhone("010-1234-5678");
        testUser.setAvailability(Availability.IDLE);
        testUser.setStatus(UserStatus.VERIFIED);
        testUser.getRoles().add(roleService.getClientRole());
        return userRepository.save(testUser);
    }

    public User createTestTranslator() {
        User testUser = new User();
        testUser.setFirstName("Sarah");
        testUser.setLastName("Collins");
        testUser.setEmail("sarah@test.com");
        testUser.setPasswordHash("password2");
        testUser.setPhone("010-1111-1111");
        testUser.setAvailability(Availability.ONLINE);
        testUser.setStatus(UserStatus.VERIFIED);
        testUser.getRoles().add(roleService.getTranslatorRole());
        return userRepository.save(testUser);
    }

    public User createTestAdmin() {
        User testAdmin = new User();
        testAdmin.setFirstName("Best");
        testAdmin.setLastName("Admin");
        testAdmin.setEmail("admin@test.com");
        testAdmin.setPasswordHash("admin_password");
        testAdmin.setPhone("010-9999-9999");
        testAdmin.setAvailability(Availability.IDLE);
        testAdmin.setStatus(UserStatus.VERIFIED);
        testAdmin.getRoles().add(roleService.getAdminRole());
        return userRepository.save(testAdmin);
    }

    public Wallet createTestWallet(User testUser) {
        walletService.createWallet(testUser, CurrencyCode.KRW);
        Wallet testWallet = walletRepository.findByUserId(testUser.getId()).orElseThrow();
        walletService.addFunds(testWallet.getId(), 1000L);
        return walletRepository.save(testWallet);
    }

    public TransactionRequest createTestTransactionRequest(TransactionType type, Long amount) {
        return new TransactionRequest(type, amount, CurrencyCode.KRW);
    }

    public Transaction createTestTransaction(TransactionType type, Long amount) {
        Authentication userAuth = new UsernamePasswordAuthenticationToken("johndoe@test.com", null);

        TransactionRequest testTransactionRequest = createTestTransactionRequest(type, amount);
        TransactionResponse responseDto = transactionService.createTransaction(testTransactionRequest, userAuth);

        Transaction testTransaction = financeHelper.findTransaction(responseDto.id());
        Wallet testWallet = testTransaction.getWallet();
        User testUser = testWallet.getUser();

        verificationHelper.verifyTransaction(testTransaction, type, amount, testUser, testWallet);
        return testTransaction;
    }

    public Transaction createPendingTestTransaction(TransactionType type, Long amount, Wallet testWallet) {
        Transaction pendingTransaction = new Transaction();
        pendingTransaction.setType(type);
        pendingTransaction.setAmount(amount);
        pendingTransaction.setCurrencyCode(CurrencyCode.KRW);
        pendingTransaction.setDescription(financeHelper.generateTransactionDescription(type, amount));
        pendingTransaction.setReference(financeHelper.generateTransactionReference(type));
        pendingTransaction.setStatus(TransactionStatus.PENDING);
        pendingTransaction.setWallet(testWallet);
        pendingTransaction.setBalanceBefore(testWallet.getBalance());
        if (type == TransactionType.WITHDRAWAL || type == TransactionType.CALL_CHARGE) {
            pendingTransaction.setBalanceAfter(testWallet.getBalance() - amount);
        } else {
            pendingTransaction.setBalanceAfter(testWallet.getBalance() + amount);
        }
        return transactionRepository.save(pendingTransaction);
    }

}
