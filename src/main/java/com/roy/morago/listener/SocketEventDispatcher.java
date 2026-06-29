package com.roy.morago.listener;

import com.roy.morago.constants.SocketEvents;
import com.roy.morago.dto.notification.NotificationResponse;
import com.roy.morago.dto.socket.AdminActionEvent;
import com.roy.morago.dto.socket.CallEndedEvent;
import com.roy.morago.dto.socket.IncomingCallEvent;
import com.roy.morago.dto.socket.TransactionProcessedEvent;
import com.roy.morago.service.SocketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class SocketEventDispatcher {
    private final SocketService socketService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onCallRequested(IncomingCallEvent event) {
        try {
            socketService.sendToUser(event.getReceiverId(), SocketEvents.INCOMING_CALL, event);
        } catch (Exception e) {
            log.error("Failed to send incoming call event: callId={}, receiverId={}",
                    event.getCallId(), event.getReceiverId(), e);
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onCallEnded(CallEndedEvent event) {
        try {
            socketService.sendToUser(event.getClientId(), SocketEvents.CALL_ENDED, event);
            socketService.sendToUser(event.getTranslatorId(), SocketEvents.CALL_ENDED, event);
        } catch (Exception e) {
            log.error("Failed to send call ended event: callId={}, clientId={}, translatorId={}",
                    event.getCallId(), event.getClientId(), event.getTranslatorId(), e);
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onAdminAction(AdminActionEvent event) {
        try {
            socketService.sendToUser(event.getUserId(), SocketEvents.ADMIN_ACTION, event);
        } catch (Exception e) {
            log.error("Failed to send admin action event: userId={}", event.getUserId(), e);
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onTransactionProcessed(TransactionProcessedEvent event) {
        try {
            socketService.sendToUser(event.getUserId(), SocketEvents.TRANSACTION_PROCESSED, event);
        } catch (Exception e) {
            log.error("Failed to send transaction processed event: transactionId={}, userId={}",
                    event.getTransactionId(), event.getUserId(), e);
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onNotificationCreated(NotificationResponse event) {
        try {
            socketService.sendToUser(event.userId(), SocketEvents.NOTIFICATION, event);
        } catch (Exception e) {
            log.error("Failed to send notification event: userId={}", event.userId(), e);
        }
    }
}