package com.roy.morago.entity.call;

import com.roy.morago.entity.BaseEntity;
import com.roy.morago.entity.topic.Topic;
import com.roy.morago.entity.user.User;
import com.roy.morago.enums.CallStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Duration;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "calls")
public class Call extends BaseEntity {
    @ManyToOne(optional = false)
    @JoinColumn(name = "client_id", nullable = false)
    private User client;

    @ManyToOne(optional = false)
    @JoinColumn(name = "translator_id", nullable = false)
    private User translator;

    @ManyToOne(optional = false)
    @JoinColumn(name = "topic_id", nullable = false)
    private Topic topic;

    @Column(nullable = false)
    private Long cost;
    @Column(nullable = false)
    private Long maxCallTime;
    @Column(nullable = false)
    private CallStatus status;
    @Column(nullable = false)
    private Boolean isClientInitiator;
    @Column
    private Integer rating;
    @Column
    private LocalDateTime acceptedAt;
    @Column
    private LocalDateTime startedAt;
    @Column
    private LocalDateTime endedAt;
    @Column
    private LocalDateTime canceledAt;

    public User getCaller() {
        return isClientInitiator ? getClient() : getTranslator();
    }

    public User getReceiver() {
        return isClientInitiator ? getTranslator() : getClient();
    }

    public long getOngoingDurationSeconds() {
        if (startedAt == null) {
            return 0L;
        }
        return Duration.between(startedAt, LocalDateTime.now()).toSeconds();
    }

    public long getFullDurationSeconds() {
        if (startedAt == null || endedAt == null) {
            return 0L;
        }
        return Duration.between(startedAt, endedAt).toSeconds();
    }

    public long getExpectedCost() {
        long callSeconds = getFullDurationSeconds();
        long minutes = (long) Math.ceil(callSeconds / 60.0);
        return minutes * 1000;
    }
}
