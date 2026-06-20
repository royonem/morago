package com.roy.morago.service;

import com.roy.morago.dto.call.CallRequest;
import com.roy.morago.dto.call.CallResponse;
import com.roy.morago.dto.call.CallSearchRequest;
import com.roy.morago.dto.finance.TransactionRequest;
import com.roy.morago.dto.finance.TransactionResponse;
import com.roy.morago.dto.finance.WithdrawalRequest;
import com.roy.morago.entity.call.Call;
import com.roy.morago.entity.finance.BankAccount;
import com.roy.morago.entity.finance.Transaction;
import com.roy.morago.entity.finance.Wallet;
import com.roy.morago.entity.user.User;
import com.roy.morago.enums.*;
import com.roy.morago.repository.finance.BankRepository;
import com.roy.morago.repository.finance.TransactionRepository;
import com.roy.morago.repository.finance.WalletRepository;
import com.roy.morago.repository.user.UserRepository;
import com.roy.morago.service.call.CallHelper;
import com.roy.morago.service.call.CallService;
import com.roy.morago.service.finance.FinanceHelper;
import com.roy.morago.service.finance.TransactionService;
import com.roy.morago.service.finance.WalletService;
import com.roy.morago.service.user.RoleService;
import com.roy.morago.service.user.UserHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

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
    private CallService callService;
    @Autowired
    private VerificationHelper verificationHelper;
    @Autowired
    private BankRepository bankRepository;
    @Autowired
    private UserHelper userHelper;
    @Autowired
    private CallHelper callHelper;


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
        testUser.setBankAccount(createTestBankAccount(testUser));
        return userRepository.save(testUser);
    }

    public User createTestClient2() {
        User testUser = new User();
        testUser.setFirstName("Joe");
        testUser.setLastName("Mack");
        testUser.setEmail("joemack@test.com");
        testUser.setPasswordHash("password");
        testUser.setPhone("010-0000-0000");
        testUser.setAvailability(Availability.IDLE);
        testUser.setStatus(UserStatus.VERIFIED);
        testUser.getRoles().add(roleService.getClientRole());
        testUser.setBankAccount(createTestBankAccount3(testUser));
        testUser.setWallet(createTestWallet(testUser));
        return userRepository.save(testUser);
    }

    public User createTestTranslator() {
        User testUser = new User();
        testUser.setFirstName("Sara");
        testUser.setLastName("Brown");
        testUser.setEmail("translator@test.com");
        testUser.setPasswordHash("Sara");
        testUser.setPhone("010-9999-1111");
        testUser.setAvailability(Availability.ONLINE);
        testUser.setStatus(UserStatus.VERIFIED);
        testUser.getRoles().add(roleService.getTranslatorRole());
        testUser.setBankAccount(createTestBankAccount2(testUser));
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

    public BankAccount createTestBankAccount(User testUser) {
        BankAccount bankAccount = new BankAccount();
        bankAccount.setBankName("Hana Bank");
        bankAccount.setAccountNumber("0000111122223333");
        bankAccount.setUser(testUser);
        return bankRepository.save(bankAccount);
    }

    public BankAccount createTestBankAccount2(User testTranslator) {
        BankAccount bankAccount = new BankAccount();
        bankAccount.setBankName("Kookmin Bank");
        bankAccount.setAccountNumber("8888777766665555");
        bankAccount.setUser(testTranslator);
        return bankRepository.save(bankAccount);
    }

    public BankAccount createTestBankAccount3(User testClient2) {
        BankAccount bankAccount = new BankAccount();
        bankAccount.setBankName("IBK");
        bankAccount.setAccountNumber("9999777766665555");
        bankAccount.setUser(testClient2);
        return bankRepository.save(bankAccount);
    }

    public TransactionRequest createTestTransactionRequest(TransactionType type, Long amount) {
        return new TransactionRequest(type, amount, CurrencyCode.KRW);
    }

    public Transaction createTestTransaction(TransactionType type, Long amount) {
        Authentication userAuth = new UsernamePasswordAuthenticationToken("johndoe@test.com", null);

        TransactionRequest testTransactionRequest = createTestTransactionRequest(type, amount);
        TransactionResponse responseDto = transactionService.createTransaction(testTransactionRequest, userAuth);

        Transaction testTransaction = financeHelper.findTransactionById(responseDto.id());
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

    public WithdrawalRequest createTestWithdrawalRequest(Long withdrawalAmount) {
        return new WithdrawalRequest(withdrawalAmount, CurrencyCode.KRW);
    }

    public User getClientId(CallRequest request) {
        return userHelper.findUserById(request.clientId());
    }

    public User getTranslatorId(CallRequest request) {
        return userHelper.findUserById(request.translatorId());
    }

    // call setup
    public CallRequest createTestCallRequest(User testClient, User testTranslator) {
        return new CallRequest(
                testClient.getId(),
                testTranslator.getId(),
                1L
        );
    }

    public CallSearchRequest createTestCallSearchRequest(
            User testClient, User testTranslator, CallStatus callStatus, Integer ratingFrom, Integer ratingTo) {
        return new CallSearchRequest(
                testClient.getId(),
                null,
                1L,
                callStatus,
                ratingFrom,
                ratingTo,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );
    }

    public CallResponse createTestRequestCall(CallRequest request) {
        return callService.requestCall(request, getClientId(request));
    }

    public CallResponse createTestAcceptCall(CallRequest request) {
        CallResponse response = createTestRequestCall(request);
        return callService.acceptCall(response.id(), getTranslatorId(request));
    }

    public CallResponse createTestCancelCall(CallRequest request) {
        CallResponse response = createTestRequestCall(request);
        return callService.cancelCall(response.id(), getClientId(request));
    }

    public CallResponse createTestDeclineCall(CallRequest request) {
        CallResponse response = createTestRequestCall(request);
        return callService.declineCall(response.id(), getTranslatorId(request));
    }

    public CallResponse createTestStartCall(CallRequest request) {
        CallResponse response = createTestAcceptCall(request);
        return callService.startCall(response.id());
    }

    public CallResponse createTestEndCall(CallRequest request, Long seconds) {
        CallResponse response = createTestStartCall(request);
        Call testCall = callHelper.findCallById(response.id());
        testCall.setStartedAt(LocalDateTime.now().minusSeconds(seconds));
        return callService.endCall(response.id());
    }

    public CallResponse createTestRateCall(CallRequest request, Long seconds, Integer rating) {
        CallResponse response = createTestEndCall(request, seconds);
        return callService.rateCall(response.id(), rating);
    }

    public void createMassCallLog(User testClient, User testTranslator) {
        testClient.getWallet().setBalance(1000000L);
        User testClient2 = createTestClient2();
        testClient2.getWallet().setBalance(1000000L);
        // client 1 = 50
        // client 2 = 20
        for (int i = 0; i < 70; i++) {
            CallRequest request = createTestCallRequest(testClient, testTranslator);
            CallRequest request2 = createTestCallRequest(testClient2, testTranslator);
            if (i < 5) {
                createTestRequestCall(request); // 5
            } else if (i < 10) {
                createTestCancelCall(request2); // 5
            } else if (i < 15) {
                createTestDeclineCall(request); // 5
            } else if (i < 20) {
                createTestEndCall(request2, 59L); // 5
            } else if (i < 30) {
                createTestRateCall(request, 59L, 1); // 10
            } else if (i < 40) {
                createTestRateCall(request2, 59L, 2); // 10
            } else if (i < 50) {
                createTestRateCall(request, 59L, 3); // 10
            } else if (i < 60) {
                createTestRateCall(request, 59L, 4); // 10
            } else {
                createTestRateCall(request, 59L, 5); // 10
            }
        }
    }
}
