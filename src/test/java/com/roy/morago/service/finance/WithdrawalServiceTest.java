package com.roy.morago.service.finance;

import com.roy.morago.dto.finance.*;
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
import com.roy.morago.mapper.WithdrawalRequestMapper;
import com.roy.morago.repository.finance.TransactionRepository;
import com.roy.morago.repository.finance.WalletRepository;
import com.roy.morago.repository.finance.WithdrawalRequestRepository;
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
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class WithdrawalServiceTest {
    @InjectMocks
    private WithdrawalService withdrawalService;
    @Mock
    private WalletService walletService;
    @Mock
    private TransactionService transactionService;
    @Mock
    private UserService userService;
    @Mock
    private UserHelper userHelper;
    @Mock
    private WithdrawalRequestRepository withdrawalRequestRepository;
    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private WalletRepository walletRepository;
    @Mock
    private Authentication userAuthentication;
    @Mock
    private Authentication adminAuthentication;
    @Mock
    private WithdrawalRequestMapper withdrawalRequestMapper;

    private User testUser;
    private User testAdmin;
    private Wallet testWallet;
    private WithdrawalRequest testRequest;
    private WithdrawalRequestDTO testRequestDto;
    private WithdrawalRequestResponse testResponse;
    private RejectWithdrawalDTO rejectionDto;

    // Stubs
    private void stubFindUserWithAuthentication() {
        when(userHelper.findUserWithAuthentication(userAuthentication))
                .thenReturn(testUser);
    }

    private void stubFindAdminWithAuthentication() {
        when(userHelper.findUserWithAuthentication(adminAuthentication))
                .thenReturn(testAdmin);
    }

    private void stubFindWithdrawalById() {
        when(withdrawalRequestRepository.findById(testRequest.getId()))
                .thenReturn(Optional.of(testRequest));
    }

    private void stubPendingWithdrawalRequest() {
        when(withdrawalRequestRepository.existsByRequesterAndStatus(testUser, WithdrawalStatus.PENDING))
                .thenReturn(true);
    }

    private void stubMapFromDTO() {
        when(withdrawalRequestMapper.createWithdrawalRequestFromDto(any(WithdrawalRequestDTO.class)))
                .thenReturn(testRequest);
    }

    private void stubMapFromEntity() {
        when(withdrawalRequestMapper.createWithdrawalRequestResponse(any(WithdrawalRequest.class)))
                .thenReturn(testResponse);
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

    private void createTestAdmin() {
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
    }

    private void createTestWallet() {
        testWallet = new Wallet();
        testWallet.setId(1L);
        testWallet.setBalance(1000L);
        testWallet.setCurrencyCode(CurrencyCode.KRW);
        testWallet.setStatus(WalletStatus.ACTIVE);
    }

    private void createTestWithdrawalRequestDTO(Long amount) {
        testRequestDto = new WithdrawalRequestDTO();
        testRequestDto.setAmount(amount);
        testRequestDto.setCurrencyCode(CurrencyCode.KRW);
    }

    private void createTestResponseDTO() {
        testResponse = new WithdrawalRequestResponse();
        testResponse.setId(testRequest.getId());
        testResponse.setAmount(testRequest.getAmount());
        testResponse.setCurrencyCode(testRequest.getCurrencyCode());
        testResponse.setStatus(WithdrawalStatus.PENDING);
    }

    private void createTestWithdrawalRequest(Long amount) {
        testRequest = new WithdrawalRequest();
        testRequest.setId(1L);
        testRequest.setAmount(amount);
        testRequest.setCurrencyCode(CurrencyCode.KRW);
        testRequest.setStatus(WithdrawalStatus.PENDING);
        testRequest.setWallet(testWallet);
        testRequest.setRequester(testUser);

        Transaction testTransaction = new Transaction();
        testTransaction.setId(1L);
        testTransaction.setWallet(testWallet);
        testTransaction.setStatus(TransactionStatus.PENDING);
        testRequest.setTransaction(testTransaction);
        testTransaction.setWithdrawalRequest(testRequest);
    }

    private void createRejectionDTO() {
        rejectionDto = new RejectWithdrawalDTO("Suspicious Transaction");
    }

    @BeforeEach
    public void setUp() {
        createTestUser();
        createTestAdmin();
        createTestWallet();
        testUser.setWallet(testWallet);
        testWallet.setUser(testUser);
    }

    // Standard Tests
    @Test
    void testCreateWithdrawalRequest() {
        createTestWithdrawalRequestDTO(100L);
        createTestWithdrawalRequest(100L);
        stubMapFromDTO();
        stubFindUserWithAuthentication();
        withdrawalService.createWithdrawalRequest(testRequestDto, userAuthentication);
        assertThat(testRequest.getAmount()).isEqualTo(100L);
        assertThat(testRequest.getStatus()).isEqualTo(WithdrawalStatus.PENDING);
    }

    @Test
    void testCancelWithdrawalRequest() {
        createTestWithdrawalRequestDTO(100L);
        createTestWithdrawalRequest(100L);
        stubFindWithdrawalById();

        withdrawalService.cancelWithdrawalRequest(testRequest.getId());
        assertThat(testRequest.getStatus()).isEqualTo(WithdrawalStatus.CANCELED);
    }

    @Test
    void testRejectWithdrawalRequest() {
        createTestWithdrawalRequestDTO(100L);
        createTestWithdrawalRequest(100L);
        createRejectionDTO();

        stubFindAdminWithAuthentication();
        stubFindWithdrawalById();

        withdrawalService.rejectWithdrawalRequest(testRequest.getId(), adminAuthentication, rejectionDto);
        assertThat(testRequest.getStatus()).isEqualTo(WithdrawalStatus.REJECTED);
        assertThat(testRequest.getReviewer()).isEqualTo(testAdmin);
        assertThat(testRequest.getRejectionReason()).isEqualTo(rejectionDto.rejectionReason());
    }

    @Test
    void testApproveWithdrawalRequest() {
        createTestWithdrawalRequestDTO(500L);
        createTestWithdrawalRequest(500L);

        stubFindAdminWithAuthentication();
        stubFindWithdrawalById();

        withdrawalService.approveWithdrawalRequest(testRequest.getId(), adminAuthentication);
        assertThat(testRequest.getStatus()).isEqualTo(WithdrawalStatus.APPROVED);
        assertThat(testRequest.getReviewer()).isEqualTo(testAdmin);
        assertThat(testRequest.getRejectionReason()).isNull();
        assertThat(testRequest.getPaidAt()).isNotNull();
    }

    @Test
    void testGetWithdrawalRequest() {
        createTestWithdrawalRequest(100L);
        createTestResponseDTO();

        stubFindWithdrawalById();
        stubMapFromEntity();

        WithdrawalRequestResponse response = withdrawalService
                .getWithdrawalRequest(testRequest.getId());

        assertThat(response.getId()).isEqualTo(testRequest.getId());
        assertThat(response.getAmount()).isEqualTo(100L);
        assertThat(response.getCurrencyCode()).isEqualTo(CurrencyCode.KRW);
        assertThat(response.getStatus()).isEqualTo(WithdrawalStatus.PENDING);
    }

    // Test Already Approved
    @Test
    void testCancelWithdrawalRequest_alreadyApproved_throwsException() {
        createTestWithdrawalRequestDTO(100L);
        createTestWithdrawalRequest(100L);
        stubFindWithdrawalById();

        withdrawalService.approveWithdrawalRequest(testRequest.getId(), adminAuthentication);

        assertThatThrownBy(() -> withdrawalService.cancelWithdrawalRequest(testRequest.getId()))
                .isInstanceOf(InvalidWithdrawalStateException.class);

        assertThat(testRequest.getStatus()).isEqualTo(WithdrawalStatus.APPROVED);
        assertThat(testRequest.getRequester()).isEqualTo(testUser);
        assertThat(testRequest.getPaidAt()).isNotNull();
    }

    @Test
    void testRejectWithdrawalRequest_alreadyApproved_throwsException() {
        createTestWithdrawalRequestDTO(100L);
        createTestWithdrawalRequest(100L);
        createRejectionDTO();

        stubFindWithdrawalById();

        withdrawalService.approveWithdrawalRequest(testRequest.getId(), adminAuthentication);

        assertThatThrownBy(() -> withdrawalService.rejectWithdrawalRequest(testRequest.getId(), adminAuthentication, rejectionDto))
                .isInstanceOf(InvalidWithdrawalStateException.class);

        assertThat(testRequest.getStatus()).isEqualTo(WithdrawalStatus.APPROVED);
        assertThat(testRequest.getRequester()).isEqualTo(testUser);
        assertThat(testRequest.getPaidAt()).isNotNull();
    }

    @Test
    void testApproveWithdrawalRequest_alreadyApproved_throwsException() {
        createTestWithdrawalRequestDTO(100L);
        createTestWithdrawalRequest(100L);
        stubFindWithdrawalById();

        withdrawalService.approveWithdrawalRequest(testRequest.getId(), adminAuthentication);

        assertThatThrownBy(() -> withdrawalService.approveWithdrawalRequest(testRequest.getId(), adminAuthentication))
                .isInstanceOf(InvalidWithdrawalStateException.class);

        assertThat(testRequest.getRequester()).isEqualTo(testUser);
    }

    // Test Already Canceled
    @Test
    void testRejectWithdrawalRequest_alreadyCanceled_throwsException() {
        createTestWithdrawalRequestDTO(100L);
        createTestWithdrawalRequest(100L);
        stubFindWithdrawalById();
        withdrawalService.cancelWithdrawalRequest(testRequest.getId());

        assertThatThrownBy(() -> withdrawalService.rejectWithdrawalRequest(testRequest.getId(), adminAuthentication, rejectionDto))
                .isInstanceOf(InvalidWithdrawalStateException.class);
        assertThat(testRequest.getStatus()).isEqualTo(WithdrawalStatus.CANCELED);
    }

    @Test
    void testApproveWithdrawalRequest_alreadyCanceled_throwsException() {
        createTestWithdrawalRequestDTO(100L);
        createTestWithdrawalRequest(100L);
        stubFindWithdrawalById();
        withdrawalService.cancelWithdrawalRequest(testRequest.getId());

        assertThatThrownBy(() -> withdrawalService.approveWithdrawalRequest(testRequest.getId(), adminAuthentication))
                .isInstanceOf(InvalidWithdrawalStateException.class);
        assertThat(testRequest.getStatus()).isEqualTo(WithdrawalStatus.CANCELED);
    }

    @Test
    void testCancelWithdrawalRequest_alreadyCanceled_throwsException() {
        createTestWithdrawalRequestDTO(100L);
        createTestWithdrawalRequest(100L);
        stubFindWithdrawalById();
        withdrawalService.cancelWithdrawalRequest(testRequest.getId());
        assertThatThrownBy(() -> withdrawalService.cancelWithdrawalRequest(testRequest.getId()))
                .isInstanceOf(InvalidWithdrawalStateException.class);
    }

    // Test Already Rejected
    @Test
    void testRejectWithdrawalRequest_alreadyRejected_throwsException() {
        createTestWithdrawalRequestDTO(100L);
        createTestWithdrawalRequest(100L);
        createRejectionDTO();
        stubFindAdminWithAuthentication();
        stubFindWithdrawalById();
        withdrawalService.rejectWithdrawalRequest(testRequest.getId(), adminAuthentication, rejectionDto);

        assertThatThrownBy(() -> withdrawalService.rejectWithdrawalRequest(testRequest.getId(), adminAuthentication, rejectionDto))
                .isInstanceOf(InvalidWithdrawalStateException.class);
    }

    @Test
    void testCancelWithdrawalRequest_alreadyRejected_throwsException() {
        createTestWithdrawalRequestDTO(100L);
        createTestWithdrawalRequest(100L);
        createRejectionDTO();
        stubFindAdminWithAuthentication();
        stubFindWithdrawalById();
        withdrawalService.rejectWithdrawalRequest(testRequest.getId(), adminAuthentication, rejectionDto);

        assertThatThrownBy(() -> withdrawalService.cancelWithdrawalRequest(testRequest.getId()))
                .isInstanceOf(InvalidWithdrawalStateException.class);
        assertThat(testRequest.getStatus()).isEqualTo(WithdrawalStatus.REJECTED);
        assertThat(testRequest.getRequester()).isEqualTo(testUser);
    }

    @Test
    void testApproveWithdrawalRequest_alreadyRejected_throwsException() {
        createTestWithdrawalRequestDTO(100L);
        createTestWithdrawalRequest(100L);
        createRejectionDTO();
        stubFindAdminWithAuthentication();
        stubFindWithdrawalById();
        withdrawalService.rejectWithdrawalRequest(testRequest.getId(), adminAuthentication, rejectionDto);

        assertThatThrownBy(() -> withdrawalService.approveWithdrawalRequest(testRequest.getId(), adminAuthentication))
                .isInstanceOf(InvalidWithdrawalStateException.class);
        assertThat(testRequest.getStatus()).isEqualTo(WithdrawalStatus.REJECTED);
    }

    // Test Other Failed Cases
    @Test
    void testCreateWithdrawalRequest_insufficientFunds_throwsException() {
        createTestWithdrawalRequestDTO(1100L);
        createTestWithdrawalRequest(1100L);
        stubFindUserWithAuthentication();
        assertThatThrownBy(() -> withdrawalService.createWithdrawalRequest(testRequestDto, userAuthentication))
                .isInstanceOf(DeficientFundsException.class);
    }

    @Test
    void testCreateWithdrawalRequest_existingPending_throwsException() {
        createTestWithdrawalRequestDTO(100L);
        createTestWithdrawalRequest(100L);

        stubFindUserWithAuthentication();
        stubMapFromDTO();

        withdrawalService.createWithdrawalRequest(testRequestDto, userAuthentication);
        stubPendingWithdrawalRequest();

        assertThatThrownBy(() -> withdrawalService.createWithdrawalRequest(testRequestDto, userAuthentication))
                .isInstanceOf(ExistingWithdrawalRequestException.class);
    }

    @Test
    void testGetWithdrawalRequest_notFound_throwsException() {
        assertThatThrownBy(() -> withdrawalService.getWithdrawalRequest(-1L))
                .isInstanceOf(WithdrawalNotFoundException.class);
    }
}
