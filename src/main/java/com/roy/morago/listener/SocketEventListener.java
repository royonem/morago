package com.roy.morago.listener;

import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.annotation.OnConnect;
import com.corundumstudio.socketio.annotation.OnDisconnect;
import com.corundumstudio.socketio.annotation.OnEvent;
import com.roy.morago.constants.SocketEvents;
import com.roy.morago.service.SocketService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class SocketEventListener {

    private final SocketService socketService;
    private final Map<UUID, Long> clientUserIdMap = new ConcurrentHashMap<>();

    public SocketEventListener(SocketService socketService) {
        this.socketService = socketService;
    }

    @OnConnect
    public void onConnect(SocketIOClient client) {
        log.info("Client connected: {}", client.getSessionId());

        String userIdParam = client.getHandshakeData().getSingleUrlParam("userId");
        if (userIdParam != null) {
            try {
                Long userId = Long.parseLong(userIdParam);
                clientUserIdMap.put(client.getSessionId(), userId);
                socketService.joinRoom(client, "user:" + userId);
                log.info("User {} joined their room", userId);
            } catch (NumberFormatException e) {
                log.warn("Invalid userId param: {}", userIdParam);
            }
        }
    }

    @OnDisconnect
    public void onDisconnect(SocketIOClient client) {
        log.info("Client disconnected: {}", client.getSessionId());
        Long userId = clientUserIdMap.remove(client.getSessionId());
        if (userId != null) {
            socketService.leaveRoom(client, "user:" + userId);
        }
    }

    @OnEvent(SocketEvents.JOIN_ROOM)
    public void onJoinRoom(SocketIOClient client, String room) {
        socketService.joinRoom(client, room);
    }
}