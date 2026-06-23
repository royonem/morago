package com.roy.morago.dto.socket;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AdminActionEvent {
    private Long userId;
    private String email;
    private String action;
    private LocalDateTime sentAt;
}
