package com.roy.morago.service.finance;

import com.roy.morago.dto.call.CallSearchRequest;
import com.roy.morago.dto.finance.TransactionRequest;
import com.roy.morago.dto.finance.TransactionSearchRequest;
import com.roy.morago.entity.call.Call;
import com.roy.morago.entity.finance.BankAccount;
import com.roy.morago.entity.finance.Transaction;
import com.roy.morago.entity.finance.Wallet;
import com.roy.morago.entity.finance.Withdrawal;
import com.roy.morago.entity.user.User;
import com.roy.morago.enums.TransactionStatus;
import com.roy.morago.enums.TransactionType;
import com.roy.morago.enums.WalletStatus;
import com.roy.morago.enums.WithdrawalStatus;
import com.roy.morago.exception.finance.*;
import com.roy.morago.repository.finance.BankRepository;
import com.roy.morago.repository.finance.TransactionRepository;
import com.roy.morago.repository.finance.WalletRepository;
import com.roy.morago.repository.finance.WithdrawalRepository;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class FinanceHelper {
    private final TransactionRepository transactionRepository;
    private final WithdrawalRepository withdrawalRepository;
    private final WalletRepository walletRepository;
    private final BankRepository bankRepository;

    // Find Helpers
    public Transaction findTransactionById(Long id) {
        return transactionRepository.findById(id)
                .orElseThrow(() -> new TransactionNotFoundException("Transaction not found"));
    }

    public Withdrawal findWithdrawalById(Long id) {
        return withdrawalRepository.findById(id)
                .orElseThrow(() -> new WithdrawalNotFoundException("Withdrawal not found"));
    }

    public Wallet findWalletById(Long id) {
        return walletRepository.findById(id)
                .orElseThrow(() -> new WalletNotFoundException("Wallet not found"));
    }

    protected BankAccount findBankAccountById(Long id) {
        return bankRepository.findById(id).orElseThrow(()
                -> new BankNotFoundException("Bank account not found"));
    }

    // Validation Helpers
    protected void validateDepositTransaction(TransactionRequest dto) {
        if (dto.type() !=  TransactionType.DEPOSIT) {
            throw new InvalidTransactionStateException("Transaction type is invalid.");
        }
    }

    protected void validateNonWithdrawalTransaction(TransactionRequest dto) {
        if (dto.type() == TransactionType.WITHDRAWAL) {
            throw new InvalidTransactionStateException("Transaction type is invalid.");
        }
    }

    protected void validateNoPendingTransactions(User user) {
        if (transactionRepository.existsByWalletUserIdAndStatus(user.getId(), TransactionStatus.PENDING)) {
            throw new ExistingTransactionException("Pending transaction already exists.");
        }
    }

    protected void validateNoOtherPendingTransactions(User user, Long transactionId) {
        if (transactionRepository.existsByWalletUserIdAndStatusAndIdNot
                (user.getId(), TransactionStatus.PENDING, transactionId)) {
            throw new ExistingTransactionException("ending transaction already exists.");
        }
    }

    protected void validateTransactionIsPending(Transaction transaction, String message) {
        if (transaction.getStatus() != TransactionStatus.PENDING) {
            throw new InvalidTransactionStateException(message);
        }
    }

    protected void validateTransactionIsPaid(Transaction transaction) {
        if (transaction.getStatus() != TransactionStatus.PAID) {
            throw new InvalidTransactionStateException("Error validating transaction.");
        }
    }

    protected void validatePositiveTransaction(Long amount) {
        if (amount <= 0) {
            throw new NonPositiveTransactionException("Transactions must include a positive amount.");
        }
    }

    protected void validateNonNegativeWalletBalance(Long balance) {
        if (balance < 0L) {
            throw new DeficientFundsException("Wallet does not have enough funds");
        }
    }

    protected void validateWalletIsActive(Wallet wallet) {
        if (wallet.getStatus() != WalletStatus.ACTIVE) {
            throw new NonActiveWalletException("Cannot access wallet.");
        }
    }

    protected void validateNoPendingWithdrawals(User user) {
        if (withdrawalRepository.existsByRequesterAndStatus(user, WithdrawalStatus.PENDING)) {
            throw new ExistingWithdrawalException("Pending withdrawal request already exists.");
        }
    }

    protected void validateSufficientWalletBalance(Long withdrawalAmount, Wallet wallet) {
        if (withdrawalAmount > wallet.getBalance()) {
            throw new DeficientFundsException("Request amount exceeds balance.");
        }
    }

    protected void validateWithdrawalIsPending(Withdrawal withdrawal) {
        if (withdrawal.getStatus() != WithdrawalStatus.PENDING) {
            throw new InvalidWithdrawalStateException("Withdrawal state is invalid.");
        } else if (withdrawal.getTransaction().getStatus() != TransactionStatus.PENDING) {
            throw new InvalidTransactionStateException("Transaction is not pending.");
        }
    }

    // Generation Helpers
    public String generateTransactionReference(TransactionType type) {
        String random = UUID.randomUUID().toString();
        return type + "-" + random;
    }

    public String generateTransactionDescription(TransactionType type, Long amount) {
        return type + " transaction of " + amount;
    }

    // Build Helpers
    protected Specification<Transaction> buildSpecification(TransactionSearchRequest request) {
        return (root, query, cb) -> {
            List<Predicate> predicates = buildPredicates(request, root, cb);
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    protected Specification<Transaction> buildSpecificationForUser(Long userId, TransactionSearchRequest request) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(
                    cb.or(
                            cb.equal(root.join("user").get("id"), userId)
                    )
            );
            predicates.addAll(buildPredicates(request, root, cb));
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private List<Predicate> buildPredicates(TransactionSearchRequest request, Root<Transaction> root, CriteriaBuilder cb) {
        List<Predicate> predicates = new ArrayList<>();
        Optional.ofNullable(request.walletUserId()).ifPresent(id ->
                predicates.add(cb.equal(root.join("wallet").join("user").get("id"), id)));
        Optional.ofNullable(request.callId()).ifPresent(id ->
                predicates.add(cb.equal(root.join("call").get("id"), id)));

        Optional.ofNullable(request.type()).ifPresent(type ->
                predicates.add(cb.equal(root.get("type"), type)));
        Optional.ofNullable(request.status()).ifPresent(status ->
                predicates.add(cb.equal(root.get("status"), status)));
        Optional.ofNullable(request.currencyCode()).ifPresent(code ->
                predicates.add(cb.equal(root.get("currencyCode"), code)));

        // Amount range
        Optional.ofNullable(request.amountFrom()).ifPresent(amount ->
                predicates.add(cb.greaterThanOrEqualTo(root.get("amount"), amount)));
        Optional.ofNullable(request.amountTo()).ifPresent(amount ->
                predicates.add(cb.lessThanOrEqualTo(root.get("amount"), amount)));

        // Created date range
        Optional.ofNullable(request.createdFrom()).ifPresent(date ->
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), date)));
        Optional.ofNullable(request.createdTo()).ifPresent(date ->
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), date)));

        // Processed date range
        Optional.ofNullable(request.processedFrom()).ifPresent(date ->
                predicates.add(cb.greaterThanOrEqualTo(root.get("processedAt"), date)));
        Optional.ofNullable(request.processedTo()).ifPresent(date ->
                predicates.add(cb.lessThanOrEqualTo(root.get("processedAt"), date)));
        return predicates;
    }
}