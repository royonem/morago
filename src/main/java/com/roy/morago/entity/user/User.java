package com.roy.morago.entity.user;

import com.roy.morago.entity.BaseEntity;
import com.roy.morago.entity.file.File;
import com.roy.morago.entity.finance.BankAccount;
import com.roy.morago.entity.finance.Wallet;
import com.roy.morago.entity.topic.Topic;
import com.roy.morago.entity.notification.Notification;
import com.roy.morago.enums.Availability;
import com.roy.morago.enums.TopikLevel;
import com.roy.morago.enums.UserStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class User extends BaseEntity {
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "profile_picture_id")
    private File profilePicture;

    @OneToOne(mappedBy = "user")
    private Wallet wallet;

    @OneToOne(mappedBy = "user")
    private BankAccount bankAccount;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Notification> notifications = new ArrayList<>();

    @ManyToMany
    @JoinTable(
            name = "user_topics",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "topic_id")
    )
    private Set<Topic> topics;

    @ManyToMany
    @JoinTable(
            name = "user_languages",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "language_id")
    )
    private Set<Language> languages;

    @ManyToMany
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

    @Column(nullable = false)
    private String firstName;
    @Column(nullable = false)
    private String lastName;
    @Column(nullable = false, unique = true)
    private String email;
    @Column(nullable = false)
    private String passwordHash;
    @Column(nullable = false, unique = true)
    private String phone;
    @Column(nullable = false)
    private Availability availability;
    @Column(nullable = false)
    private UserStatus status;
    @Column
    private TopikLevel topikLevel;
    @Column
    private LocalDate birthdate;
}
