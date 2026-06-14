package com.roy.morago.service.finance;

import com.roy.morago.dto.finance.RejectWithdrawalDTO;
import com.roy.morago.dto.finance.WithdrawalRequest;
import com.roy.morago.dto.finance.WithdrawalResponse;
import com.roy.morago.entity.finance.Transaction;
import com.roy.morago.entity.finance.Wallet;
import com.roy.morago.entity.finance.Withdrawal;
import com.roy.morago.entity.user.User;
import com.roy.morago.enums.TransactionStatus;
import com.roy.morago.enums.WithdrawalStatus;
import com.roy.morago.exception.finance.*;
import com.roy.morago.mapper.WithdrawalMapper;
import com.roy.morago.repository.finance.TransactionRepository;
import com.roy.morago.repository.finance.WithdrawalRepository;
import com.roy.morago.service.user.UserHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@RequiredArgsConstructor
@Service
public class WithdrawalService {
    private final WithdrawalMapper mapper;
    private final TransactionService transactionService;
    private final UserHelper userHelper;
    private final WithdrawalRepository withdrawalRepository;
    private final FinanceHelper financeHelper;
    private final TransactionRepository transactionRepository;

    @Transactional
    public WithdrawalResponse createWithdrawal(WithdrawalRequest request, Authentication authentication) {
        User user = userHelper.findUserWithAuthentication(authentication);
        Wallet wallet = user.getWallet();
        checkExistingPendingWithdrawal(user);

        validateSufficientFunds(request.amount(), wallet);

        Withdrawal withdrawal = mapper.createEntityFromRequest(request);
        withdrawal.setStatus(WithdrawalStatus.PENDING);
        withdrawal.setWallet(wallet);
        withdrawal.setRequester(user);

        Transaction transaction = transactionService.createWithdrawalTransaction(user, withdrawal);
        transactionRepository.save(transaction);
        withdrawalRepository.save(withdrawal);

        return mapper.createResponseFromEntity(withdrawal);
    }

    public WithdrawalResponse getWithdrawal(Long withdrawalId) {
        Withdrawal request = findWithdrawalById(withdrawalId);
        return mapper.createResponseFromEntity(request);
    }

    @Transactional
    public void cancelWithdrawal(Long withdrawalId) {
        Withdrawal withdrawal = findWithdrawalById(withdrawalId);
        validatePendingWithdrawal(withdrawal);
        withdrawal.setStatus(WithdrawalStatus.CANCELED);
        transactionService.cancelTransaction(withdrawal.getTransaction().getId());
    }

    @Transactional
    public void rejectWithdrawal(Long withdrawalId, Authentication adminAuth, RejectWithdrawalDTO rejectionDTO) {
        Withdrawal withdrawal = findWithdrawalById(withdrawalId);
        validatePendingWithdrawal(withdrawal);
        logReview(withdrawal, adminAuth);
        withdrawal.setRejectionReason(rejectionDTO.rejectionReason());
        withdrawal.setStatus(WithdrawalStatus.REJECTED);
        withdrawal.getTransaction().setStatus(TransactionStatus.FAILED);
    }

    @Transactional
    public void approveWithdrawal(Long withdrawalId, Authentication adminAuth) {
        Withdrawal withdrawal = findWithdrawalById(withdrawalId);

        validatePendingWithdrawal(withdrawal);
        validateSufficientFunds(withdrawal.getAmount(), withdrawal.getWallet());
        logReview(withdrawal, adminAuth);

        withdrawal.setStatus(WithdrawalStatus.APPROVED);

        transactionService.processTransaction(withdrawal.getTransaction());
        financeHelper.validateTransactionIsPaid(withdrawal.getTransaction());
        withdrawal.setPaidAt(LocalDateTime.now());
    }

    private void logReview(Withdrawal withdrawal, Authentication adminAuth) {
        withdrawal.setReviewer(userHelper.findUserWithAuthentication(adminAuth));
        withdrawal.setReviewedAt(LocalDateTime.now());
    }

    private void checkExistingPendingWithdrawal(User user) {
        if (withdrawalRepository.existsByRequesterAndStatus(user, WithdrawalStatus.PENDING)) {
            throw new ExistingWithdrawalException("Pending withdrawal request already exists.");
        }
    }

    private void validateSufficientFunds(Long withdrawalAmount, Wallet wallet) {
        if (withdrawalAmount > wallet.getBalance()) {
            throw new DeficientFundsException("Request amount exceeds balance.");
        }
    }

    private void validatePendingWithdrawal(Withdrawal withdrawal) {
        if (withdrawal.getStatus() != WithdrawalStatus.PENDING) {
            throw new InvalidWithdrawalStateException("Withdrawal state is invalid.");
        } else if (withdrawal.getTransaction().getStatus() != TransactionStatus.PENDING) {
            throw new InvalidTransactionStateException("Transaction is not pending.");
        }
    }

    private Withdrawal findWithdrawalById(Long id) {
        return withdrawalRepository.findById(id)
                .orElseThrow(() -> new WithdrawalNotFoundException("Withdrawal not found"));
    }
}
