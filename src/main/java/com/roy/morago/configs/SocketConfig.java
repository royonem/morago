package com.roy.morago.configs;

import com.corundumstudio.socketio.SocketIOServer;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SocketConfig {
    @Value("${socket.port:8081}")
    private int socketPort;

    @Value("${socket.host:localhost}")
    private String socketHost;

    @Bean
    public SocketIOServer socketIOServer() {
        com.corundumstudio.socketio.Configuration config =
                new com.corundumstudio.socketio.Configuration();
        config.setHostname(socketHost);
        config.setPort(socketPort);
        config.setOrigin("*");
        config.setWorkerThreads(100);

        SocketIOServer server = new SocketIOServer(config);
        server.start();
        return server;
    }

    @PreDestroy
    public void stopSocketServer() {
        if (socketIOServer() != null) {
            socketIOServer().stop();
        }
    }
}
