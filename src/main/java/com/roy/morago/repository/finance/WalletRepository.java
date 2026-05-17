package com.roy.morago.repository.finance;

import com.roy.morago.entity.finance.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WalletRepository extends JpaRepository<Wallet, Long> {
    boolean existsByIdAndUserId(Long walletId, Long userId);
}
