package com.example.notification_system.websocket;

import java.security.Principal;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import com.example.notification_system.security.JwtTokenPrincipal;

@Component
public class WebSocketEventListener {
    
    private final ConcurrentHashMap<String, JwtTokenPrincipal> activeUsers = new ConcurrentHashMap<>();

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        Principal user = headerAccessor.getUser();
     
        if (user instanceof JwtTokenPrincipal jwtUser) {
            String sessionId = headerAccessor.getSessionId();
            activeUsers.put(sessionId, jwtUser); 
        }
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        activeUsers.remove(sessionId);  
    }

    public boolean isUserActive(String userName) {

        return activeUsers.values().stream()
                .anyMatch(jwtUser -> jwtUser.getName().equals(userName));
    }

    public String getTokenForUser(String userName) {

        return activeUsers.values().stream()
                .filter(jwtUser -> jwtUser.getName().equals(userName))
                .map(JwtTokenPrincipal::getToken)
                .findFirst()
                .orElse(null);
    }
}
