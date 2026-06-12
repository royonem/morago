package com.roy.morago.service.finance;

import com.roy.morago.dto.finance.WalletDTO;
import com.roy.morago.entity.finance.Wallet;
import com.roy.morago.entity.user.User;
import com.roy.morago.enums.CurrencyCode;
import com.roy.morago.enums.WalletStatus;
import com.roy.morago.exception.finance.DeficientFundsException;
import com.roy.morago.exception.finance.InvalidTransactionStateException;
import com.roy.morago.exception.finance.NonPositiveTransactionException;
import com.roy.morago.exception.finance.WalletNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
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
public class WalletServiceIT {
    @Container
    @ServiceConnection
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8");

    @Autowired
    private WalletService walletService;
    @Autowired
    SetupHelper setUpHelper;
    @Autowired
    VerificationHelper verificationHelper;

    private Wallet testWallet;

    @BeforeEach
    public void setUp() {
        User testUser = setUpHelper.createTestClient();
        walletService.createWallet(testUser, CurrencyCode.KRW);
        testWallet = testUser.getWallet();
    }

    @Test
    void testAddFunds() {
        walletService.addFunds(testWallet.getId(), 500L);
        verificationHelper.verifyWalletBalance(500L, testWallet);
    }

    @Test
    void testAddFunds_negativeAmount_throwsException() {
        assertThatThrownBy(() -> walletService.addFunds(testWallet.getId(), -100L))
                .isInstanceOf(NonPositiveTransactionException.class);
        verificationHelper.verifyWalletBalance(0L, testWallet);
    }

    @Test
    void testSubtractFunds() {
        walletService.addFunds(testWallet.getId(), 500L);
        walletService.subtractFunds(testWallet.getId(), 200L);
        verificationHelper.verifyWalletBalance(300L, testWallet);
    }

    @Test
    void testSubtractFunds_insufficientBalance_throwsException() {
        walletService.addFunds(testWallet.getId(), 500L);
        assertThatThrownBy(() -> walletService.subtractFunds(testWallet.getId(), 1000L))
                .isInstanceOf(DeficientFundsException.class);
        verificationHelper.verifyWalletBalance(500L, testWallet);
    }

    @Test
    void testSubtractFunds_negativeAmount_throwsException() {
        walletService.addFunds(testWallet.getId(), 500L);
        assertThatThrownBy(() -> walletService.subtractFunds(testWallet.getId(), -100L))
                .isInstanceOf(InvalidTransactionStateException.class);
        verificationHelper.verifyWalletBalance(500L, testWallet);
    }

    @Test
    void testChangeCurrencyCode() {
        walletService.updateCurrency(testWallet.getId(), CurrencyCode.USD);
        assertThat(testWallet.getCurrencyCode()).isEqualTo(CurrencyCode.USD);
        verificationHelper.verifyWalletBalance(0L, testWallet);
    }

    @Test
    void testActivateWallet_whenSuspended_becomesActive() {
        walletService.suspendWallet(testWallet.getId());
        walletService.activateWallet(testWallet.getId());
        verificationHelper.verifyWalletStatus(WalletStatus.ACTIVE, testWallet);
    }

    @Test
    void testSuspendWallet() {
        walletService.suspendWallet(testWallet.getId());
        verificationHelper.verifyWalletStatus(WalletStatus.SUSPENDED, testWallet);
    }

    @Test
    void testAddFunds_whenWalletSuspended_throwsException() {
        walletService.suspendWallet(testWallet.getId());
        assertThatThrownBy(() -> walletService.addFunds(testWallet.getId(), 100L))
                .isInstanceOf(InvalidTransactionStateException.class);
        verificationHelper.verifyWalletBalance(0L, testWallet);
    }

    @Test
    void testSubtractFunds_whenWalletSuspended_throwsException() {
        walletService.suspendWallet(testWallet.getId());
        assertThatThrownBy(() -> walletService.subtractFunds(testWallet.getId(), 100L))
                .isInstanceOf(InvalidTransactionStateException.class);
        verificationHelper.verifyWalletBalance(0L, testWallet);
    }

    @Test
    void testBlockWallet() {
        walletService.blockWallet(testWallet.getId());
        verificationHelper.verifyWalletStatus(WalletStatus.BLOCKED, testWallet);
    }

    @Test
    void testAddFunds_whenWalletBlocked_throwsException() {
        walletService.blockWallet(testWallet.getId());
        assertThatThrownBy(() -> walletService.addFunds(testWallet.getId(), 100L))
                .isInstanceOf(InvalidTransactionStateException.class);
        verificationHelper.verifyWalletBalance(0L, testWallet);
    }

    @Test
    void testSubtractFunds_whenWalletBlocked_throwsException() {
        walletService.blockWallet(testWallet.getId());
        assertThatThrownBy(() -> walletService.subtractFunds(testWallet.getId(), 100L))
                .isInstanceOf(InvalidTransactionStateException.class);
        verificationHelper.verifyWalletBalance(0L, testWallet);
    }

    @Test
    void testGetWallet_ById_success() {
        WalletDTO result = walletService.getWallet(testWallet.getId());

        assertThat(result.getBalance()).isEqualTo(0L);
        assertThat(result.getCurrencyCode()).isEqualTo(CurrencyCode.KRW);
        assertThat(result.getStatus()).isEqualTo(WalletStatus.ACTIVE);
    }

    @Test
    void testGetWallet_ById_notFound_throwsException() {
        assertThatThrownBy(() -> walletService.getWallet(-1L))
                .isInstanceOf(WalletNotFoundException.class);
    }
}
