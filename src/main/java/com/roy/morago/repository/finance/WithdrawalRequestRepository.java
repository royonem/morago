package com.roy.morago.repository.finance;

import com.roy.morago.entity.finance.Transaction;
import com.roy.morago.entity.finance.WithdrawalRequest;
import com.roy.morago.entity.user.User;
import com.roy.morago.enums.WithdrawalStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WithdrawalRequestRepository extends JpaRepository<WithdrawalRequest, Long> {
    boolean existsByIdAndRequesterId(Long withdrawalId, Long userId);

    WithdrawalRequest findByTransaction(Transaction transaction);

    boolean existsByRequesterAndStatus(User requester, WithdrawalStatus status);
}
