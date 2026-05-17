package com.roy.morago.service.finance;

import com.roy.morago.dto.finance.TransactionDTO;
import com.roy.morago.dto.finance.TransactionResponse;
import com.roy.morago.entity.finance.Transaction;
import com.roy.morago.entity.finance.Wallet;
import com.roy.morago.entity.finance.WithdrawalRequest;
import com.roy.morago.entity.user.User;
import com.roy.morago.enums.TransactionStatus;
import com.roy.morago.enums.TransactionType;
import com.roy.morago.exception.DeficientFundsException;
import com.roy.morago.exception.InvalidTransactionStateException;
import com.roy.morago.exception.TransactionNotFoundException;
import com.roy.morago.mapper.TransactionMapper;
import com.roy.morago.repository.finance.TransactionRepository;
import com.roy.morago.repository.finance.WalletRepository;
import com.roy.morago.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.ResponseStatus;

@RequiredArgsConstructor
@Service
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final TransactionMapper transactionMapper;
    private final WalletRepository walletRepository;
    private final UserService userService;
    private final WalletService walletService;

    @Transactional
    @ResponseStatus(HttpStatus.CREATED)
    public TransactionResponse createTransaction(TransactionDTO dto, Authentication authentication) {
        User user = userService.findUserWithAuthentication(authentication);
        Wallet wallet = user.getWallet();
        Long currentBalance = wallet.getBalance();
        walletService.ensureNonNegativeBalance(currentBalance);

        Long transactionAmount = dto.getAmount();

        Long balanceAfter = calculateBalanceAfter(dto.getType(), currentBalance, transactionAmount);
        validateBalanceAfter(balanceAfter);

        Transaction transaction = transactionMapper.createTransactionFromDto(dto);
        transaction.setStatus(TransactionStatus.PENDING);
        transaction.setWallet(wallet);
        transaction.setBalanceBefore(currentBalance);
        transaction.setBalanceAfter(balanceAfter);

        transactionRepository.save(transaction);
        if (!dto.getType().equals(TransactionType.WITHDRAWAL)) {
            processTransaction(transaction.getId());
        }
        return transactionMapper.createTransactionResponse(transaction);
    }

    @Transactional
    public void cancelTransaction(Long id) {
        Transaction transaction = findTransaction(id);
        validatePendingTransaction(transaction, "Error cancelling transaction.");
        transaction.setStatus(TransactionStatus.CANCELED);
    }

    public TransactionResponse getTransaction(Long transactionId) {
        return transactionMapper.createTransactionResponse(findTransaction(transactionId));
    }

    protected void createWithdrawalTransaction(WithdrawalRequest request, User user) {
        Transaction transaction = new Transaction();
        Wallet wallet = user.getWallet();
        transaction.setWallet(wallet);
        transaction.setType(TransactionType.WITHDRAWAL);
        transaction.setAmount(request.getAmount());
        transaction.setCurrencyCode(request.getCurrencyCode());
        transaction.setStatus(TransactionStatus.PENDING);
        transaction.setBalanceBefore(wallet.getBalance());
        transaction.setBalanceAfter
                (calculateBalanceAfter(TransactionType.WITHDRAWAL, wallet.getBalance(), request.getAmount()));
        transaction.setReference("test");
        transaction.setDescription("test");
        transaction.setWithdrawalRequest(request);
        transactionRepository.save(transaction);
    }

    protected void processTransaction(Long id) {
        Transaction transaction = findTransaction(id);
        Wallet wallet = transaction.getWallet();
        validatePendingTransaction(transaction, "Error processing transaction.");
        Long balanceAfter = calculateBalanceAfter(transaction.getType(), wallet.getBalance(), transaction.getAmount());
        transaction.setBalanceBefore(wallet.getBalance());
        transaction.setBalanceAfter(balanceAfter);
        wallet.setBalance(balanceAfter);
        walletRepository.save(wallet);
        transaction.setStatus(TransactionStatus.PAID);
    }

    protected void validateTransactionIsPaid(Long transactionId) {
        Transaction transaction = findTransaction(transactionId);
        if (transaction.getStatus() != TransactionStatus.PAID) {
            throw new InvalidTransactionStateException("Error validating transaction.");
        }
    }

    private Transaction findTransaction(Long id) {
        return transactionRepository.findById(id)
                .orElseThrow(() -> new TransactionNotFoundException("Transaction not found"));
    }

    private Long calculateBalanceAfter(TransactionType type, Long currentBalance, Long transactionBalance) {
        return switch (type) {
            case DEPOSIT, CALL_EARNING -> currentBalance + transactionBalance;
            case WITHDRAWAL, CALL_CHARGE -> currentBalance - transactionBalance;
        };
    }

    private void validatePendingTransaction(Transaction transaction, String message) {
        if (transaction.getStatus() != TransactionStatus.PENDING) {
            throw new InvalidTransactionStateException(message);
        }
    }

    private void validateBalanceAfter(Long balanceAfter) {
        if (balanceAfter < 0L) {
            throw new DeficientFundsException("Not enough funds to complete transaction");
        }
    }

}
