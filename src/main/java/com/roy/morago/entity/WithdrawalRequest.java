package com.roy.morago.entity;

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
@Table(name = "withdrawal_requests")
public class WithdrawalRequest extends BaseEntity {
    @ManyToOne(optional = false)
    @JoinColumn(name = "requester_id", nullable = false)
    private User requester;
    @ManyToOne
    @JoinColumn(name = "reviewer_id")
    private User reviewer;
    @ManyToOne(optional = false)
    @JoinColumn(name = "wallet_id", nullable = false)
    private Wallet wallet;
    @ManyToOne(optional = false)
    @JoinColumn(name = "bank_account_id", nullable = false)
    private BankAccount bankAccount;
    @Column(nullable = false)
    private Integer coinAmount;
    @Column(nullable = false)
    private BigDecimal cashAmount;
    @Column(nullable = false)
    private String status;
    @Column
    private String rejectionReason;
    @Column
    private LocalDateTime reviewedAt;
    @Column
    private LocalDateTime paidAt;
}
