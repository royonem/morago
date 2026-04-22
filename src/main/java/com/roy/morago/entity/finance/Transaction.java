package com.roy.morago.entity.finance;

import com.roy.morago.entity.BaseEntity;
import com.roy.morago.entity.call.Call;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "transactions")
public class Transaction extends BaseEntity {
    @OneToOne(optional = false)
    @JoinColumn(name = "wallet_id")
    private Wallet wallet;

    @OneToOne
    @JoinColumn(name = "call_id")
    private Call call;

    @OneToOne
    @JoinColumn(name = "withdrawal_request_id", unique = true)
    private WithdrawalRequest withdrawalRequest;

    @Column(nullable = false) // eventually use enum not string
    private String type;
    @Column(nullable = false)
    private Integer coinAmount;
    @Column(nullable = false)
    private BigDecimal cashAmount;
    @Column(nullable = false)
    private String currencyCode;
    @Column(nullable = false) // eventually use enum not string
    private String status;
    @Column(nullable = false)
    private Integer balanceBefore;
    @Column(nullable = false)
    private Integer balanceAfter;
    @Column(nullable = false,  unique = true)
    private String reference;
    @Column(nullable = false)
    private String description;
    @Column
    private LocalDateTime processedAt;
}
