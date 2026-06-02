package com.roy.morago.service.finance;

import com.roy.morago.dto.finance.WalletDTO;
import com.roy.morago.entity.finance.Wallet;
import com.roy.morago.entity.user.User;
import com.roy.morago.enums.Availability;
import com.roy.morago.enums.CurrencyCode;
import com.roy.morago.enums.UserStatus;
import com.roy.morago.enums.WalletStatus;
import com.roy.morago.exception.finance.DeficientFundsException;
import com.roy.morago.exception.finance.InvalidTransactionStateException;
import com.roy.morago.exception.finance.NonActiveWalletException;
import com.roy.morago.exception.finance.WalletNotFoundException;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class WalletServiceTest {
    @InjectMocks
    private WalletService walletService;
    @Mock
    private WalletRepository walletRepository;

    private Wallet testWallet;
    private User testUser;

    private void stubWalletRepository() {
        when(walletRepository.findById(testWallet.getId()))
                .thenReturn(Optional.of(testWallet));
        when(walletRepository.save(any(Wallet.class)))
                .thenReturn(testWallet);
    }

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

    @Test
    void testAddFunds() {
        stubWalletRepository();
        walletService.addFunds(testWallet.getId(), 500L);
        assertThat(testWallet.getBalance()).isEqualTo(1500L);
        verify(walletRepository).save(testWallet);
    }

    @Test
    void testAddFunds_negativeAmount_throwsException() {
        stubWalletRepository();
        assertThatThrownBy(() -> walletService.addFunds(testWallet.getId(), -100L))
                .isInstanceOf(InvalidTransactionStateException.class);
        assertThat(testWallet.getBalance()).isEqualTo(1000L);
        verify(walletRepository, never()).save(any(Wallet.class));
    }

    @Test
    void testSubtractFunds() {
        stubWalletRepository();
        walletService.subtractFunds(testWallet.getId(), 200L);
        assertThat(testWallet.getBalance()).isEqualTo(800L);
        verify(walletRepository).save(testWallet);
    }

    @Test
    void testSubtractFunds_insufficientBalance_throwsException() {
        stubWalletRepository();
        assertThatThrownBy(() -> walletService.subtractFunds(testWallet.getId(), 1200L))
                .isInstanceOf(DeficientFundsException.class);
        assertThat(testWallet.getBalance()).isEqualTo(1000L);
        verify(walletRepository, never()).save(any(Wallet.class));
    }

    @Test
    void testSubtractFunds_negativeAmount_throwsException() {
        stubWalletRepository();
        assertThatThrownBy(() -> walletService.subtractFunds(testWallet.getId(), -100L))
                .isInstanceOf(InvalidTransactionStateException.class);
        assertThat(testWallet.getBalance()).isEqualTo(1000L);
        verify(walletRepository, never()).save(any(Wallet.class));
    }

    @Test
    void testChangeCurrencyCode() {
        stubWalletRepository();
        walletService.updateCurrency(testWallet.getId(), CurrencyCode.USD);
        assertThat(testWallet.getCurrencyCode()).isEqualTo(CurrencyCode.USD);
        verify(walletRepository).save(testWallet);
    }

    @Test
    void testActivateWallet_whenSuspended_becomesActive() {
        stubWalletRepository();
        walletService.suspendWallet(testWallet.getId());
        assertThat(testWallet.getStatus()).isEqualTo(WalletStatus.SUSPENDED);
        walletService.activateWallet(testWallet.getId());
        assertThat(testWallet.getStatus()).isEqualTo(WalletStatus.ACTIVE);
        verify(walletRepository, times(2)).save(testWallet);
    }

    @Test
    void testSuspendWallet() {
        stubWalletRepository();
        walletService.suspendWallet(testWallet.getId());
        assertThat(testWallet.getStatus()).isEqualTo(WalletStatus.SUSPENDED);
        verify(walletRepository).save(testWallet);
    }

    @Test
    void testAddFunds_whenWalletSuspended_throwsException() {
        stubWalletRepository();
        walletService.suspendWallet(testWallet.getId());
        assertThat(testWallet.getStatus()).isEqualTo(WalletStatus.SUSPENDED);
        verify(walletRepository).save(testWallet);
        assertThatThrownBy(() -> walletService.addFunds(testWallet.getId(), 100L))
                .isInstanceOf(NonActiveWalletException.class);
        assertThat(testWallet.getBalance()).isEqualTo(1000L);
        verify(walletRepository, times(1)).save(testWallet);
    }

    @Test
    void testSubtractFunds_whenWalletSuspended_throwsException() {
        stubWalletRepository();
        walletService.suspendWallet(testWallet.getId());
        assertThat(testWallet.getStatus()).isEqualTo(WalletStatus.SUSPENDED);
        verify(walletRepository).save(testWallet);
        assertThatThrownBy(() -> walletService.subtractFunds(testWallet.getId(), 100L))
                .isInstanceOf(NonActiveWalletException.class);
        assertThat(testWallet.getBalance()).isEqualTo(1000L);
        verify(walletRepository, times(1)).save(testWallet);
    }

    @Test
    void testBlockWallet() {
        stubWalletRepository();
        walletService.blockWallet(testWallet.getId());
        assertThat(testWallet.getStatus()).isEqualTo(WalletStatus.BLOCKED);
        verify(walletRepository).save(testWallet);
    }

    @Test
    void testAddFunds_whenWalletBlocked_throwsException() {
        stubWalletRepository();
        walletService.blockWallet(testWallet.getId());
        assertThat(testWallet.getStatus()).isEqualTo(WalletStatus.BLOCKED);
        verify(walletRepository).save(testWallet);
        assertThatThrownBy(() -> walletService.addFunds(testWallet.getId(), 100L))
                .isInstanceOf(NonActiveWalletException.class);
        assertThat(testWallet.getBalance()).isEqualTo(1000L);
        verify(walletRepository, times(1)).save(testWallet);
    }

    @Test
    void testSubtractFunds_whenWalletBlocked_throwsException() {
        stubWalletRepository();
        walletService.blockWallet(testWallet.getId());
        assertThat(testWallet.getStatus()).isEqualTo(WalletStatus.BLOCKED);
        verify(walletRepository).save(testWallet);
        assertThatThrownBy(() -> walletService.subtractFunds(testWallet.getId(), 100L))
                .isInstanceOf(NonActiveWalletException.class);
        assertThat(testWallet.getBalance()).isEqualTo(1000L);
        verify(walletRepository, times(1)).save(testWallet);
    }

    @Test
    void testGetWalletById_success() {
        stubWalletRepository();

        WalletDTO result = walletService.getWalletById(testWallet.getId());

        assertThat(result.getBalance()).isEqualTo(1000L);
        assertThat(result.getCurrencyCode()).isEqualTo(CurrencyCode.KRW);
        assertThat(result.getStatus()).isEqualTo(WalletStatus.ACTIVE);
        verify(walletRepository).findById(testWallet.getId());
        verify(walletRepository, never()).save(any());
    }

    @Test
    void testGetWalletById_notFound_throwsException() {
        when(walletRepository.findById(testWallet.getId()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> walletService.getWalletById(testWallet.getId()))
                .isInstanceOf(WalletNotFoundException.class);

        verify(walletRepository).findById(testWallet.getId());
        verify(walletRepository, never()).save(any());
    }
}
