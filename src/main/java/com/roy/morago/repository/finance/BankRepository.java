package com.roy.morago.repository.finance;

import com.roy.morago.entity.finance.BankAccount;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BankRepository extends JpaRepository<BankAccount, Long> {
    boolean existsByIdAndUserId(Long bankAccountId, Long userId);
}
