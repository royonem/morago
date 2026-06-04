package com.roy.morago.repository.finance;

import com.roy.morago.entity.finance.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface WalletRepository extends JpaRepository<Wallet, Long> {
    boolean existsByIdAndUserId(Long walletId, Long userId);

    Optional<Wallet> findByUserId(Long id);
}
