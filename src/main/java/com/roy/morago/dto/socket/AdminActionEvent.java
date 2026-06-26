package com.roy.morago.dto.socket;

import com.roy.morago.entity.user.User;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AdminActionEvent {
    private Long userId;
    private String email;
    private String action;
    private LocalDateTime sentAt;

    public static AdminActionEvent from(User user) {
        AdminActionEvent event = new AdminActionEvent();
        event.setUserId(user.getId());
        event.setEmail(user.getEmail());
        event.setAction("Verification");
        event.setSentAt(LocalDateTime.now());
        return event;
    }
}
