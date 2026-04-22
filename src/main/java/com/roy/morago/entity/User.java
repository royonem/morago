package com.roy.morago.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class User extends BaseEntity {
    @OneToOne(optional = true, cascade = CascadeType.ALL)
    @JoinColumn(name = "profile_picture_id")
    private File profilePicture;
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
    @Column(nullable = false) // eventually turn into enum
    private String availability;
    @Column(nullable = false) // eventually turn into enum
    private String status;
    @Column(nullable = false)
    private int topikLevel;
    @Column(nullable = false)
    private LocalDateTime birthdate;

    @ManyToMany
    @JoinTable(
            name = "user_topics",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "topic_id")
    )
    private List<Topic> topics;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Notification> notifications;
}
