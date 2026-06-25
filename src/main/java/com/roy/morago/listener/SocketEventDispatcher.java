package com.roy.morago.listener;

import com.roy.morago.constants.SocketEvents;
import com.roy.morago.dto.socket.CallEndedEvent;
import com.roy.morago.dto.socket.IncomingCallEvent;
import com.roy.morago.service.SocketService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class SocketEventDispatcher {
    private final SocketService socketService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onCallRequested(IncomingCallEvent event) {
        socketService.sendToUser(event.getReceiverId(), SocketEvents.INCOMING_CALL, event);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onCallEnded(CallEndedEvent event) {
        socketService.sendToUser(event.getClientId(), SocketEvents.CALL_ENDED, event);
        socketService.sendToUser(event.getTranslatorId(), SocketEvents.CALL_ENDED, event);
    }

}
