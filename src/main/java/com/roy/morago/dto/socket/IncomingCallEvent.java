package com.roy.morago.dto.socket;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class IncomingCallEvent {
    private Long callId;
    private Long callerId;
    private String callerName;
    private Long receiverId;
    private String receiverName;
    private LocalDateTime sentAt;
}