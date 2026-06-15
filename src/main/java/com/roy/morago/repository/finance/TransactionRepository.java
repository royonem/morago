package com.roy.morago.repository.finance;

import com.roy.morago.entity.finance.Transaction;
import com.roy.morago.entity.finance.Withdrawal;
import com.roy.morago.enums.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    boolean existsByWalletUserIdAndStatus(Long userId, TransactionStatus status);

    boolean existsByIdAndWalletUserId(Long transactionId, Long userId);

    List<Transaction> getAllByWalletUserId(Long userId);

    Transaction findByWithdrawal(Withdrawal withdrawal);

    boolean existsByWalletUserIdAndStatusAndIdNot(Long walletUserId, TransactionStatus status, Long id);
}
