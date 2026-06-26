package com.roy.morago.dto.socket;

import com.roy.morago.entity.call.Call;
import com.roy.morago.entity.user.User;
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

    public static IncomingCallEvent from(Call call, User caller) {
        IncomingCallEvent event = new IncomingCallEvent();
        event.setCallId(call.getId());
        event.setCallerId(caller.getId());
        event.setCallerName(caller.getFullName());
        event.setReceiverId(call.getReceiver().getId());
        event.setReceiverName(call.getReceiver().getFullName());
        event.setSentAt(LocalDateTime.now());
        return event;
    }
}