package com.roy.morago.entity.finance;

import com.roy.morago.entity.BaseEntity;
import com.roy.morago.entity.user.User;
import com.roy.morago.enums.CurrencyCode;
import com.roy.morago.enums.WalletStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "wallets")
public class Wallet extends BaseEntity {
    @OneToOne(optional = false, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "user_id")
    private User user;

    @Version
    private Long version;
    @Column(nullable = false)
    private Long balance;
    @Column(nullable = false)
    private CurrencyCode currencyCode;
    @Column(nullable = false)
    private WalletStatus status;
}
