package com.roy.morago.dto.socket;

import com.roy.morago.entity.call.Call;
import com.roy.morago.entity.user.User;
import com.roy.morago.enums.CallStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CallEndedEvent {
    private Long callId;
    private Long clientId;
    private Long translatorId;
    private String status;
    private Long duration;
    private LocalDateTime sentAt;

    public static CallEndedEvent from(Call call) {
        CallEndedEvent event = new CallEndedEvent();
        event.setCallId(call.getId());
        event.setClientId(call.getClient().getId());
        event.setTranslatorId(call.getTranslator().getId());
        event.setStatus(call.getStatus().name().toLowerCase());
        event.setDuration(call.getFullDurationSeconds());
        event.setSentAt(LocalDateTime.now());
        return event;
    }
}
