package com.roy.morago.service.finance;

import com.roy.morago.dto.finance.TransactionRequest;
import com.roy.morago.dto.finance.TransactionResponse;
import com.roy.morago.dto.finance.TransactionSearchRequest;
import com.roy.morago.dto.socket.TransactionProcessedEvent;
import com.roy.morago.entity.call.Call;
import com.roy.morago.entity.finance.Transaction;
import com.roy.morago.entity.finance.Wallet;
import com.roy.morago.entity.finance.Withdrawal;
import com.roy.morago.entity.user.User;
import com.roy.morago.enums.TransactionStatus;
import com.roy.morago.enums.TransactionType;
import com.roy.morago.mapper.TransactionMapper;
import com.roy.morago.repository.finance.TransactionRepository;
import com.roy.morago.service.user.UserHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@RequiredArgsConstructor
@Service
public class TransactionService {
    private final WalletService walletService;
    private final TransactionRepository transactionRepository;
    private final TransactionMapper transactionMapper;
    private final FinanceHelper helper;
    private final UserHelper userHelper;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public TransactionResponse createDepositTransaction(TransactionRequest request, Authentication authentication) {
        User user = userHelper.findUserWithAuthentication(authentication);
        log.info("Creating deposit transaction: userId={}", user.getId());
        helper.validateNoPendingTransactions(user);
        helper.validateDepositTransaction(request);
        Transaction deposit = createTransactionEntity(user, request);

        processTransaction(deposit);
        transactionRepository.save(deposit);
        log.info("Deposit transaction created: transactionId={}, userId={}", deposit.getId(), user.getId());
        return transactionMapper.createTransactionResponse(deposit);
    }

    protected Transaction createWithdrawalTransaction(User user, Withdrawal request) {
        log.info("Creating withdrawal transaction: userId={}", user.getId());
        Transaction transaction = new Transaction();
        transaction.setType(TransactionType.WITHDRAWAL);
        transaction.setWallet(user.getWallet());
        transaction.setAmount(request.getAmount());
        transaction.setCurrencyCode(request.getCurrencyCode());
        setTransactionBalance(transaction);

        transaction.setStatus(TransactionStatus.PENDING);
        transaction.setReference(helper.generateTransactionReference(TransactionType.WITHDRAWAL));
        transaction.setDescription(helper.generateTransactionDescription(TransactionType.WITHDRAWAL, request.getAmount()));

        transaction.setWithdrawal(request);
        request.setTransaction(transaction);
        log.info("Withdrawal transaction created: transactionId={}, userId={}", transaction.getId(), user.getId());
        return transaction;
    }

    @Transactional
    public void createCallChargeTransaction(Call call, User client) {
        log.info("Creating call charge transaction: clientId={}", client.getId());
        Transaction transaction = new Transaction();
        transaction.setType(TransactionType.CALL_CHARGE);
        transaction.setWallet(client.getWallet());
        transaction.setCall(call);
        transaction.setAmount(call.getCost());
        transaction.setCurrencyCode(client.getWallet().getCurrencyCode());
        transaction.setStatus(TransactionStatus.PENDING);
        setTransactionBalance(transaction);
        transaction.setReference(helper.generateTransactionReference(TransactionType.CALL_CHARGE));
        transaction.setDescription(helper.generateTransactionDescription(TransactionType.CALL_CHARGE, call.getCost()));
        processTransaction(transaction);
        transactionRepository.save(transaction);
        log.info("Call charge transaction created: transactionId={}, clientId={}", transaction.getId(), client.getId());
    }

    @Transactional
    public void createCallEarningTransaction(Call call, User translator) {
        log.info("Creating call earning transaction: translatorId={}", translator.getId());
        Transaction transaction = new Transaction();
        transaction.setType(TransactionType.CALL_EARNING);
        transaction.setWallet(translator.getWallet());
        transaction.setCall(call);
        transaction.setAmount(call.getCost());
        transaction.setCurrencyCode(translator.getWallet().getCurrencyCode());
        transaction.setStatus(TransactionStatus.PENDING);
        setTransactionBalance(transaction);
        transaction.setReference(helper.generateTransactionReference(TransactionType.CALL_EARNING));
        transaction.setDescription(helper.generateTransactionDescription(TransactionType.CALL_EARNING, call.getCost()));
        processTransaction(transaction);
        transactionRepository.save(transaction);
        log.info("Call earning transaction created: transactionId={}, translatorId={}", transaction.getId(), transaction.getId());
    }

    @Transactional
    public TransactionResponse createTransaction(TransactionRequest request, Authentication authentication) {
        User user = userHelper.findUserWithAuthentication(authentication);
        log.info("Creating transaction: userId={}", user.getId());
        helper.validateNoPendingTransactions(user);
        helper.validateNonWithdrawalTransaction(request);
        Transaction transaction = createTransactionEntity(user, request);

        processTransaction(transaction);
        transactionRepository.save(transaction);
        log.info("Transaction created: transactionId={}, userId={}", transaction.getId(), user.getId());
        return transactionMapper.createTransactionResponse(transaction);
    }

    public TransactionResponse getTransaction(Long transactionId) {
        return transactionMapper.createTransactionResponse(helper.findTransactionById(transactionId));
    }

    public Page<TransactionResponse> getAllTransactions(Pageable pageable) {
        return transactionRepository.findAll(pageable).map(transactionMapper::createTransactionResponse);
    }

    public Page<TransactionResponse> getTransactionsByUserId(Long userId, Pageable pageable) {
        return transactionRepository.findByWalletUserId(userId, pageable)
                .map(transactionMapper::createTransactionResponse);
    }

    public Page<TransactionResponse> searchTransactions(TransactionSearchRequest request) {
        Specification<Transaction> spec = helper.buildSpecification(request);
        return transactionRepository.findAll(spec, request.toPageable())
                .map(transactionMapper::createTransactionResponse);
    }

    public Page<TransactionResponse> searchTransactionsByUserId(Long userId, TransactionSearchRequest request) {
        Specification<Transaction> spec = helper.buildSpecificationForUser(userId, request);
        return transactionRepository.findAll(spec, request.toPageable())
                .map(transactionMapper::createTransactionResponse);
    }

    @Transactional
    public void cancelTransaction(Long id) {
        log.info("Canceling transaction: transactionId={}", id);
        Transaction transaction = helper.findTransactionById(id);
        helper.validateTransactionIsPending(transaction, "Error cancelling non-pending transaction.");
        transaction.setStatus(TransactionStatus.CANCELED);
        log.info("Transaction canceled: transactionId={}, userId={}", id,  transaction.getWallet().getUser().getId());
    }

    // Transaction Helper Methods
    private Transaction createTransactionEntity(User user, TransactionRequest request) {
        Transaction transaction = transactionMapper.createTransactionFromDto(request);
        transaction.setWallet(user.getWallet());
        transaction.setStatus(TransactionStatus.PENDING);
        transaction.setReference(helper.generateTransactionReference(request.type()));
        transaction.setDescription(helper.generateTransactionDescription(request.type(), request.amount()));
        return transaction;
    }

    protected void processTransaction(Transaction transaction) {
        log.info("Processing transaction: transactionId={}", transaction.getId());
        helper.validateTransactionIsPending(transaction, "Error processing non-pending transaction.");
        setTransactionBalance(transaction);
        setWalletBalance(transaction);
        transaction.setStatus(TransactionStatus.PAID);
        transaction.setProcessedAt(LocalDateTime.now());
        Long userId = transaction.getWallet().getUser().getId();
        log.info("Transaction processed: transactionId={}, userId={}, type={}, amount={}, newBalance={}",
                transaction.getId(), userId, transaction.getType(),
                transaction.getAmount(), transaction.getWallet().getBalance());

        TransactionProcessedEvent event = TransactionProcessedEvent.from(transaction);
        eventPublisher.publishEvent(event);
    }

    private void setTransactionBalance(Transaction transaction) {
        Wallet wallet = transaction.getWallet();
        Long currentBalance = wallet.getBalance();
        Long transactionAmount = transaction.getAmount();

        transaction.setBalanceBefore(currentBalance);
        switch (transaction.getType()) {
            case DEPOSIT, CALL_EARNING -> transaction.setBalanceAfter(currentBalance + transactionAmount);
            case WITHDRAWAL, CALL_CHARGE -> transaction.setBalanceAfter(currentBalance - transactionAmount);
        }
        helper.validateNonNegativeWalletBalance(transaction.getBalanceAfter());
    }

    private void setWalletBalance(Transaction transaction) {
        Wallet wallet = transaction.getWallet();
        User user = wallet.getUser();
        Long transactionAmount = transaction.getAmount();
        switch (transaction.getType()) {
            case DEPOSIT, CALL_EARNING -> {
                helper.validateNoOtherPendingTransactions(user, transaction.getId());
                walletService.addFunds(wallet.getId(), transactionAmount);
            }
            case WITHDRAWAL, CALL_CHARGE -> {
                helper.validateNoOtherPendingTransactions(user, transaction.getId());
                walletService.subtractFunds(wallet.getId(), transactionAmount);
            }
        }
    }
}
