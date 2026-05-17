package com.roy.morago.repository.finance;

import com.roy.morago.entity.finance.WithdrawalRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WithdrawalRequestRepository extends JpaRepository<WithdrawalRequest, Long> {
}
