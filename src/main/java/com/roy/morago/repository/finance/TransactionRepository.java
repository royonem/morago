package com.roy.morago.repository.finance;

import com.roy.morago.entity.finance.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
}
