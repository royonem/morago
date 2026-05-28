package com.roy.morago.service.finance;

import com.roy.morago.entity.finance.Wallet;
import com.roy.morago.entity.user.User;
import com.roy.morago.enums.Availability;
import com.roy.morago.enums.CurrencyCode;
import com.roy.morago.enums.UserStatus;
import com.roy.morago.enums.WalletStatus;
import com.roy.morago.exception.DeficientFundsException;
import com.roy.morago.exception.InvalidTransactionStateException;
import com.roy.morago.exception.WalletNotFoundException;
import com.roy.morago.repository.finance.WalletRepository;
import com.roy.morago.repository.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@Transactional
@SpringBootTest
public class WalletServiceTest {
    @Autowired
    private WalletService walletService;
    @Autowired
    private WalletRepository walletRepository;
    @Autowired
    private UserRepository userRepository;

    private Wallet testWallet;

    private User testUser;

    //Helper Methods
    private Wallet getTestWallet(Long id) {
        return walletRepository.findById(id)
                .orElseThrow(() -> new WalletNotFoundException("Wallet not found for test: " + id));
    }

    private void verifyBalance(Long walletId, Long expectedBalance) {
        Wallet wallet = getTestWallet(walletId);
        assertThat(wallet.getBalance()).isEqualTo(expectedBalance);
    }

    private void verifyStatus(Long walletId, WalletStatus expectedStatus) {
        Wallet wallet = getTestWallet(walletId);
        assertThat(wallet.getStatus()).isEqualTo(expectedStatus);
    }

    private void verifyCurrencyCode(Long walletId, CurrencyCode expectedCurrencyCode) {
        Wallet wallet = getTestWallet(walletId);
        assertThat(wallet.getCurrencyCode()).isEqualTo(expectedCurrencyCode);
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

    @BeforeEach
    public void setUp() {
        testUser = createTestUser();

        walletService.createWallet(testUser, CurrencyCode.KRW);
        testWallet = walletRepository.findByUserId(testUser.getId()).orElseThrow();

        assertThat(testWallet).isNotNull();
        verifyBalance(testWallet.getId(), 0L);
        verifyCurrencyCode(testWallet.getId(),  CurrencyCode.KRW);
        verifyStatus(testWallet.getId(), WalletStatus.ACTIVE);
    }

    @Test
    void testAddFunds() {
        walletService.addFunds(testWallet.getId(), 500L);
        verifyBalance(testWallet.getId(), 500L);
    }

    @Test
    void testAddFunds_negativeAmount_throwsException() {
        assertThatThrownBy(() -> walletService.addFunds(testWallet.getId(), -100L))
                .isInstanceOf(InvalidTransactionStateException.class);
        verifyBalance(testWallet.getId(), 0L);
    }

    @Test
    void testSubtractFunds() {
        walletService.addFunds(testWallet.getId(), 500L);
        walletService.subtractFunds(testWallet.getId(), 200L);
        verifyBalance(testWallet.getId(), 300L);
    }

    @Test
    void testSubtractFunds_insufficientBalance_throwsException() {
        walletService.addFunds(testWallet.getId(), 500L);
        assertThatThrownBy(() -> walletService.subtractFunds(testWallet.getId(), 1000L))
                .isInstanceOf(DeficientFundsException.class);
        verifyBalance(testWallet.getId(), 500L);
    }

    @Test
    void testSubtractFunds_negativeAmount_throwsException() {
        walletService.addFunds(testWallet.getId(), 500L);
        assertThatThrownBy(() -> walletService.subtractFunds(testWallet.getId(), -100L))
                .isInstanceOf(InvalidTransactionStateException.class);
        verifyBalance(testWallet.getId(), 500L);
    }

    @Test
    void testChangeCurrencyCode() {
        walletService.updateCurrency(testWallet.getId(), CurrencyCode.USD);
        verifyCurrencyCode(testWallet.getId(), CurrencyCode.USD);
        verifyBalance(testWallet.getId(), 0L);
    }

    @Test
    void testActivateWallet_whenSuspended_becomesActive() {
        walletService.suspendWallet(testWallet.getId());
        walletService.activateWallet(testWallet.getId());
        verifyStatus(testWallet.getId(), WalletStatus.ACTIVE);
    }

    @Test
    void testSuspendWallet() {
        walletService.suspendWallet(testWallet.getId());
        verifyStatus(testWallet.getId(), WalletStatus.SUSPENDED);
    }

    @Test
    void testAddFunds_whenWalletSuspended_throwsException() {
        walletService.suspendWallet(testWallet.getId());
        assertThatThrownBy(() -> walletService.addFunds(testWallet.getId(), 100L))
                .isInstanceOf(InvalidTransactionStateException.class);
        verifyBalance(testWallet.getId(), 0L);
    }

    @Test
    void testSubtractFunds_whenWalletSuspended_throwsException() {
        walletService.suspendWallet(testWallet.getId());
        assertThatThrownBy(() -> walletService.subtractFunds(testWallet.getId(), 100L))
                .isInstanceOf(InvalidTransactionStateException.class);
        verifyBalance(testWallet.getId(), 0L);
    }

    @Test
    void testBlockWallet() {
        walletService.blockWallet(testWallet.getId());
        verifyStatus(testWallet.getId(), WalletStatus.BLOCKED);
    }

    @Test
    void testAddFunds_whenWalletBlocked_throwsException() {
        walletService.blockWallet(testWallet.getId());
        assertThatThrownBy(() -> walletService.addFunds(testWallet.getId(), 100L))
                .isInstanceOf(InvalidTransactionStateException.class);
        verifyBalance(testWallet.getId(), 0L);
    }

    @Test
    void testSubtractFunds_whenWalletBlocked_throwsException() {
        walletService.blockWallet(testWallet.getId());
        assertThatThrownBy(() -> walletService.subtractFunds(testWallet.getId(), 100L))
                .isInstanceOf(InvalidTransactionStateException.class);
        verifyBalance(testWallet.getId(), 0L);
    }
}
