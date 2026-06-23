package com.roy.morago.dto.socket;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CallEndedEvent {
    private Long callId;
    private String status;
    private Long duration;
    private LocalDateTime sentAt;
}
