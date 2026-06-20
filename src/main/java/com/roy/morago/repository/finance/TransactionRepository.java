package com.roy.morago.repository.finance;

import com.roy.morago.entity.finance.Transaction;
import com.roy.morago.entity.finance.Withdrawal;
import com.roy.morago.enums.TransactionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface TransactionRepository extends JpaRepository<Transaction, Long>, JpaSpecificationExecutor<Transaction> {
    boolean existsByWalletUserIdAndStatus(Long userId, TransactionStatus status);

    boolean existsByIdAndWalletUserId(Long transactionId, Long userId);

    Page<Transaction> findByWalletUserId(Long userId, Pageable pageable);

    Transaction findByWithdrawal(Withdrawal withdrawal);

    boolean existsByWalletUserIdAndStatusAndIdNot(Long walletUserId, TransactionStatus status, Long id);
}
