package com.roy.morago.repository.finance;

import com.roy.morago.entity.finance.Withdrawal;
import com.roy.morago.entity.user.User;
import com.roy.morago.enums.WithdrawalStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WithdrawalRepository extends JpaRepository<Withdrawal, Long> {
    boolean existsByIdAndRequesterId(Long withdrawalId, Long userId);

    boolean existsByRequesterAndStatus(User requester, WithdrawalStatus status);
}
