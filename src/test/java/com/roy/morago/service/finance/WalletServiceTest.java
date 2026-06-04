package com.roy.morago.service.finance;

import com.roy.morago.dto.finance.WalletDTO;
import com.roy.morago.entity.finance.Wallet;
import com.roy.morago.entity.user.User;
import com.roy.morago.enums.Availability;
import com.roy.morago.enums.CurrencyCode;
import com.roy.morago.enums.UserStatus;
import com.roy.morago.enums.WalletStatus;
import com.roy.morago.exception.finance.*;
import com.roy.morago.repository.finance.TransactionRepository;
import com.roy.morago.repository.finance.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Optional;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class WalletServiceTest {
    @InjectMocks
    private WalletService walletService;
    @Mock
    private WalletRepository walletRepository;
    @Mock
    private TransactionRepository transactionRepository;

    private Wallet testWallet;
    private User testUser;

    // Stubs
    private void stubFindWalletById() {
        when(walletRepository.findById(testWallet.getId()))
                .thenReturn(Optional.of(testWallet));
    }

    // Setup
    private void createTestUser() {
        testUser = new User();
        testUser.setId(1L);
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

    @BeforeEach
    public void setUp() {
        createTestUser();
        createTestWallet();
        testUser.setWallet(testWallet);
        testWallet.setUser(testUser);
    }

    // Tests
    @Test
    void testAddFunds() {
        stubFindWalletById();
        walletService.addFunds(testWallet.getId(), 500L);
        assertThat(testWallet.getBalance()).isEqualTo(1500L);
    }

    @Test
    void testAddFunds_negativeAmount_throwsException() {
        stubFindWalletById();
        assertThatThrownBy(() -> walletService.addFunds(testWallet.getId(), -100L))
                .isInstanceOf(NonPositiveTransactionException.class);
        assertThat(testWallet.getBalance()).isEqualTo(1000L);
    }

    @Test
    void testSubtractFunds() {
        stubFindWalletById();
        walletService.subtractFunds(testWallet.getId(), 200L);
        assertThat(testWallet.getBalance()).isEqualTo(800L);
    }

    @Test
    void testSubtractFunds_insufficientBalance_throwsException() {
        stubFindWalletById();
        assertThatThrownBy(() -> walletService.subtractFunds(testWallet.getId(), 1200L))
                .isInstanceOf(DeficientFundsException.class);
        assertThat(testWallet.getBalance()).isEqualTo(1000L);
    }

    @Test
    void testSubtractFunds_negativeAmount_throwsException() {
        stubFindWalletById();
        assertThatThrownBy(() -> walletService.subtractFunds(testWallet.getId(), -100L))
                .isInstanceOf(NonPositiveTransactionException.class);
        assertThat(testWallet.getBalance()).isEqualTo(1000L);
    }

    @Test
    void testChangeCurrencyCode() {
        stubFindWalletById();
        walletService.updateCurrency(testWallet.getId(), CurrencyCode.USD);
        assertThat(testWallet.getCurrencyCode()).isEqualTo(CurrencyCode.USD);
    }

    @Test
    void testActivateWallet_whenSuspended_becomesActive() {
        stubFindWalletById();
        walletService.suspendWallet(testWallet.getId());
        assertThat(testWallet.getStatus()).isEqualTo(WalletStatus.SUSPENDED);
        walletService.activateWallet(testWallet.getId());
        assertThat(testWallet.getStatus()).isEqualTo(WalletStatus.ACTIVE);
    }

    @Test
    void testSuspendWallet() {
        stubFindWalletById();
        walletService.suspendWallet(testWallet.getId());
        assertThat(testWallet.getStatus()).isEqualTo(WalletStatus.SUSPENDED);
    }

    @Test
    void testAddFunds_whenWalletSuspended_throwsException() {
        stubFindWalletById();
        walletService.suspendWallet(testWallet.getId());
        assertThat(testWallet.getStatus()).isEqualTo(WalletStatus.SUSPENDED);
        assertThatThrownBy(() -> walletService.addFunds(testWallet.getId(), 100L))
                .isInstanceOf(NonActiveWalletException.class);
        assertThat(testWallet.getBalance()).isEqualTo(1000L);
    }

    @Test
    void testSubtractFunds_whenWalletSuspended_throwsException() {
        stubFindWalletById();
        walletService.suspendWallet(testWallet.getId());
        assertThat(testWallet.getStatus()).isEqualTo(WalletStatus.SUSPENDED);
        assertThatThrownBy(() -> walletService.subtractFunds(testWallet.getId(), 100L))
                .isInstanceOf(NonActiveWalletException.class);
        assertThat(testWallet.getBalance()).isEqualTo(1000L);
    }

    @Test
    void testBlockWallet() {
        stubFindWalletById();
        walletService.blockWallet(testWallet.getId());
        assertThat(testWallet.getStatus()).isEqualTo(WalletStatus.BLOCKED);
    }

    @Test
    void testAddFunds_whenWalletBlocked_throwsException() {
        stubFindWalletById();
        walletService.blockWallet(testWallet.getId());
        assertThat(testWallet.getStatus()).isEqualTo(WalletStatus.BLOCKED);
        assertThatThrownBy(() -> walletService.addFunds(testWallet.getId(), 100L))
                .isInstanceOf(NonActiveWalletException.class);
        assertThat(testWallet.getBalance()).isEqualTo(1000L);
    }

    @Test
    void testSubtractFunds_whenWalletBlocked_throwsException() {
        stubFindWalletById();
        walletService.blockWallet(testWallet.getId());
        assertThat(testWallet.getStatus()).isEqualTo(WalletStatus.BLOCKED);
        assertThatThrownBy(() -> walletService.subtractFunds(testWallet.getId(), 100L))
                .isInstanceOf(NonActiveWalletException.class);
        assertThat(testWallet.getBalance()).isEqualTo(1000L);
    }

    @Test
    void testGetWalletById_success() {
        stubFindWalletById();

        WalletDTO result = walletService.getWalletById(testWallet.getId());

        assertThat(result.getBalance()).isEqualTo(1000L);
        assertThat(result.getCurrencyCode()).isEqualTo(CurrencyCode.KRW);
        assertThat(result.getStatus()).isEqualTo(WalletStatus.ACTIVE);
    }

    @Test
    void testGetWalletById_notFound_throwsException() {
        when(walletRepository.findById(testWallet.getId()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> walletService.getWalletById(testWallet.getId()))
                .isInstanceOf(WalletNotFoundException.class);
    }
}
