package com.roy.morago.service.finance;

import com.roy.morago.dto.finance.RejectWithdrawalDTO;
import com.roy.morago.dto.finance.WithdrawalRequestDTO;
import com.roy.morago.dto.finance.WithdrawalRequestResponse;
import com.roy.morago.entity.finance.Transaction;
import com.roy.morago.entity.finance.Wallet;
import com.roy.morago.entity.finance.WithdrawalRequest;
import com.roy.morago.entity.user.User;
import com.roy.morago.enums.TransactionStatus;
import com.roy.morago.enums.WithdrawalStatus;
import com.roy.morago.exception.finance.*;
import com.roy.morago.mapper.WithdrawalRequestMapper;
import com.roy.morago.repository.finance.TransactionRepository;
import com.roy.morago.repository.finance.WithdrawalRequestRepository;
import com.roy.morago.service.user.UserHelper;
import com.roy.morago.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@RequiredArgsConstructor
@Service
public class WithdrawalService {
    private final WithdrawalRequestMapper mapper;
    private final TransactionService transactionService;
    private final UserHelper userHelper;
    private final WithdrawalRequestRepository withdrawalRequestRepository;
    private final TransactionHelper transactionHelper;
    private final TransactionRepository transactionRepository;

    @Transactional
    public WithdrawalRequestResponse createWithdrawalRequest(WithdrawalRequestDTO dto, Authentication authentication) {
        User user = userHelper.findUserWithAuthentication(authentication);
        Wallet wallet = user.getWallet();
        checkExistingPendingWithdrawal(user);

        validateSufficientFunds(dto.getAmount(), wallet);

        WithdrawalRequest request = mapper.createWithdrawalRequestFromDto(dto);
        request.setStatus(WithdrawalStatus.PENDING);
        request.setWallet(wallet);
        request.setRequester(user);

        Transaction transaction = transactionHelper.createWithdrawalTransaction(request, user);
        transactionRepository.save(transaction);
        withdrawalRequestRepository.save(request);

        return mapper.createWithdrawalRequestResponse(request);
    }

    public WithdrawalRequestResponse getWithdrawalRequest(Long requestId) {
        WithdrawalRequest request = findWithdrawalRequest(requestId);
        return mapper.createWithdrawalRequestResponse(request);
    }

    @Transactional
    public void cancelWithdrawalRequest(Long requestId) {
        WithdrawalRequest request = findWithdrawalRequest(requestId);
        validatePendingWithdrawal(request);
        request.setStatus(WithdrawalStatus.CANCELED);
        transactionService.cancelTransaction(request.getTransaction().getId());
    }

    @Transactional
    public void rejectWithdrawalRequest(Long requestId, Authentication adminAuth, RejectWithdrawalDTO rejectionDTO) {
        WithdrawalRequest request = findWithdrawalRequest(requestId);
        validatePendingWithdrawal(request);
        logReview(request, adminAuth);
        request.setRejectionReason(rejectionDTO.rejectionReason());
        request.setStatus(WithdrawalStatus.REJECTED);
        transactionHelper.failTransaction(request.getTransaction());
    }

    @Transactional
    public void approveWithdrawalRequest(Long requestId, Authentication adminAuth) {
        WithdrawalRequest request = findWithdrawalRequest(requestId);

        // Final check for valid withdrawal
        validatePendingWithdrawal(request);
        validateSufficientFunds(request.getAmount(), request.getWallet());
        logReview(request, adminAuth);

        request.setStatus(WithdrawalStatus.APPROVED);

        transactionHelper.processTransaction(request.getTransaction());
        transactionHelper.validateTransactionIsPaid(request.getTransaction());
        request.setPaidAt(LocalDateTime.now());
    }

    private void logReview(WithdrawalRequest request, Authentication adminAuth) {
        request.setReviewer(userHelper.findUserWithAuthentication(adminAuth));
        request.setReviewedAt(LocalDateTime.now());
    }

    private void checkExistingPendingWithdrawal(User user) {
        if (withdrawalRequestRepository.existsByRequesterAndStatus(user, WithdrawalStatus.PENDING)) {
            throw new ExistingWithdrawalRequestException("Pending withdrawal request already exists.");
        }
    }

    private void validateSufficientFunds(Long requestAmount, Wallet wallet) {
        if (requestAmount > wallet.getBalance()) {
            throw new DeficientFundsException("Request amount exceeds balance.");
        }
    }

    private void validatePendingWithdrawal(WithdrawalRequest request) {
        if (request.getStatus() != WithdrawalStatus.PENDING) {
            throw new InvalidWithdrawalStateException("Withdrawal state is invalid.");
        } else if (request.getTransaction().getStatus() != TransactionStatus.PENDING) {
            throw new InvalidTransactionStateException("Transaction is not pending.");
        }
    }

    private WithdrawalRequest findWithdrawalRequest(Long id) {
        return withdrawalRequestRepository.findById(id)
                .orElseThrow(() -> new WithdrawalNotFoundException("Withdrawal request not found"));
    }
}
