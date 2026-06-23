package com.roy.morago.service;

import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SocketService {

    private final SocketIOServer socketServer;

    public SocketService(SocketIOServer socketServer) {
        this.socketServer = socketServer;
    }

    public void sendToUser(Long userId, String event, Object data) {
        String room = "user:" + userId;
        log.info("Sending event '{}' to user {} in room {}", event, userId, room);
        socketServer.getRoomOperations(room).sendEvent(event, data);
    }

    public void joinRoom(SocketIOClient client, String room) {
        client.joinRoom(room);
        log.info("Client {} joined room {}", client.getSessionId(), room);
    }

    public void leaveRoom(SocketIOClient client, String room) {
        client.leaveRoom(room);
        log.info("Client {} left room {}", client.getSessionId(), room);
    }
}