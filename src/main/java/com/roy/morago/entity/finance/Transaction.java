package com.roy.morago.entity.finance;

import com.roy.morago.entity.BaseEntity;
import com.roy.morago.entity.call.Call;
import com.roy.morago.enums.CurrencyCode;
import com.roy.morago.enums.TransactionStatus;
import com.roy.morago.enums.TransactionType;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "transactions")
public class Transaction extends BaseEntity {
    @ManyToOne(optional = false)
    @JoinColumn(name = "wallet_id")
    private Wallet wallet;

    @OneToOne
    @JoinColumn(name = "call_id")
    private Call call;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "withdrawal_request_id", unique = true)
    private WithdrawalRequest withdrawalRequest;

    @Column(nullable = false)
    private TransactionType type;
    @Column(nullable = false)
    private Long amount;
    @Column(nullable = false)
    private CurrencyCode currencyCode;
    @Column(nullable = false)
    private TransactionStatus status;
    @Column(nullable = false)
    private Long balanceBefore;
    @Column(nullable = false)
    private Long balanceAfter;
    @Column(nullable = false,  unique = true)
    private String reference;
    @Column(nullable = false)
    private String description;
    @Column
    private LocalDateTime processedAt;
}
