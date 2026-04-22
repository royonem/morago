package com.roy.morago.entity.finance;

import com.roy.morago.entity.BaseEntity;
import com.roy.morago.entity.user.User;
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
@Table(name = "bank_accounts")
public class BankAccount extends BaseEntity {
    @OneToOne(optional = false, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private String bankName;
    @Column(nullable = false, unique = true)
    private String accountNumber;
}
