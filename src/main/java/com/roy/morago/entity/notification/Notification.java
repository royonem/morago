package com.roy.morago.entity.notification;

import com.roy.morago.entity.BaseEntity;
import com.roy.morago.entity.user.User;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "notifications")
public class Notification extends BaseEntity {
    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String title;
    @Column(nullable = false)
    private String content;
    @Column
    private String actionUrl;
    @Column(nullable = false)
    private Boolean isRead = false;
    @Column
    private LocalDateTime scheduledFor;
    @Column
    private LocalDateTime sentAt;
    @Column
    private LocalDateTime readAt;
}