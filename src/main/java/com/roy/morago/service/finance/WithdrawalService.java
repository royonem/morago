package com.roy.morago.service.finance;

import com.roy.morago.dto.finance.WithdrawalRejection;
import com.roy.morago.dto.finance.WithdrawalRequest;
import com.roy.morago.dto.finance.WithdrawalResponse;
import com.roy.morago.entity.finance.Transaction;
import com.roy.morago.entity.finance.Wallet;
import com.roy.morago.entity.finance.Withdrawal;
import com.roy.morago.entity.user.User;
import com.roy.morago.enums.TransactionStatus;
import com.roy.morago.enums.WithdrawalStatus;
import com.roy.morago.mapper.WithdrawalMapper;
import com.roy.morago.repository.finance.TransactionRepository;
import com.roy.morago.repository.finance.WithdrawalRepository;
import com.roy.morago.service.user.UserHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Slf4j
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
        log.info("Creating withdrawal: userId={}, amount={}", user.getId(), request.amount());
        Wallet wallet = user.getWallet();
        financeHelper.validateNoPendingWithdrawals(user);
        financeHelper.validateSufficientWalletBalance(request.amount(), wallet);

        Withdrawal withdrawal = mapper.toEntity(request);
        withdrawal.setStatus(WithdrawalStatus.PENDING);
        withdrawal.setWallet(wallet);
        withdrawal.setRequester(user);
        withdrawal.setBankAccount(user.getBankAccount());

        Transaction transaction = transactionService.createWithdrawalTransaction(user, withdrawal);
        transactionRepository.save(transaction);
        withdrawalRepository.save(withdrawal);
        log.info("Withdrawal created: withdrawalId={}, userId={}, amount={}, status={}", withdrawal.getId(), user.getId(), withdrawal.getAmount(), withdrawal.getStatus());
        return mapper.toResponse(withdrawal);
    }

    public WithdrawalResponse getWithdrawal(Long withdrawalId) {
        Withdrawal withdrawal = financeHelper.findWithdrawalById(withdrawalId);
        return mapper.toResponse(withdrawal);
    }

    @Transactional
    public void cancelWithdrawal(Long withdrawalId) {
        log.info("Canceling withdrawal: withdrawalId={}", withdrawalId);
        Withdrawal withdrawal = financeHelper.findWithdrawalById(withdrawalId);
        financeHelper.validateWithdrawalIsPending(withdrawal);
        withdrawal.setStatus(WithdrawalStatus.CANCELED);
        transactionService.cancelTransaction(withdrawal.getTransaction().getId());
        log.info("Withdrawal canceled: withdrawalId={}, userId={}, amount={}, status={}",
                withdrawal.getId(), withdrawal.getRequester().getId(), withdrawal.getAmount(), withdrawal.getStatus());
    }

    @Transactional
    public void rejectWithdrawal(Long withdrawalId, Authentication adminAuth, WithdrawalRejection rejection) {
        log.info("Rejecting withdrawal: withdrawalId={}", withdrawalId);
        Withdrawal withdrawal = financeHelper.findWithdrawalById(withdrawalId);
        User admin =  userHelper.findUserWithAuthentication(adminAuth);
        financeHelper.validateWithdrawalIsPending(withdrawal);
        logReview(withdrawal, admin);
        withdrawal.setRejectionReason(rejection.rejectionReason());
        withdrawal.setStatus(WithdrawalStatus.REJECTED);
        withdrawal.getTransaction().setStatus(TransactionStatus.FAILED);
        log.info("Withdrawal rejected: withdrawalId={}, userId={}, amount={}, status={}, adminId={}, reason={}",
                withdrawal.getId(), withdrawal.getRequester().getId(), withdrawal.getAmount(), withdrawal.getStatus(), admin.getId(), withdrawal.getRejectionReason());
    }

    @Transactional
    public void approveWithdrawal(Long withdrawalId, Authentication adminAuth) {
        log.info("Approving withdrawal: withdrawalId={}", withdrawalId);
        Withdrawal withdrawal = financeHelper.findWithdrawalById(withdrawalId);
        User admin =  userHelper.findUserWithAuthentication(adminAuth);
        financeHelper.validateWithdrawalIsPending(withdrawal);
        financeHelper.validateSufficientWalletBalance(withdrawal.getAmount(), withdrawal.getWallet());
        logReview(withdrawal, admin);
        withdrawal.setStatus(WithdrawalStatus.APPROVED);
        transactionService.processTransaction(withdrawal.getTransaction());
        financeHelper.validateTransactionIsPaid(withdrawal.getTransaction());
        withdrawal.setPaidAt(LocalDateTime.now());
        log.info("Withdrawal approved and processed: withdrawalId={}, userId={}, amount={}, status={} adminId={}",
                withdrawalId, withdrawal.getRequester().getId(), withdrawal.getAmount(), withdrawal.getStatus(), admin.getId());
    }

    private void logReview(Withdrawal withdrawal, User admin) {
        withdrawal.setReviewer(admin);
        withdrawal.setReviewedAt(LocalDateTime.now());
    }
}
