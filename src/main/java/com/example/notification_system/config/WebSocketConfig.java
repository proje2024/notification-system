package com.example.notification_system.config;

import java.security.Principal;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import com.example.notification_system.security.JwtTokenPrincipal;
import com.example.notification_system.security.JwtTokenUtil;


@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final JwtTokenUtil jwtTokenUtil;
    private final String frontendServiceUrl;
    private final String frontendIpUrl;

    @Autowired
    public WebSocketConfig(JwtTokenUtil jwtTokenUtil) {
        this.jwtTokenUtil = jwtTokenUtil;
        this.frontendServiceUrl = System.getenv("FRONTEND_SERVICE_URL");
        this.frontendIpUrl = System.getenv("FRONTEND_LOCAL_URL");
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic");
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOrigins(frontendServiceUrl, frontendIpUrl ) 
                .setHandshakeHandler(new DefaultHandshakeHandler() {
                    @Override
                    protected Principal determineUser(ServerHttpRequest request, WebSocketHandler wsHandler, Map<String, Object> attributes) {
                        String token = resolveTokenFromQuery(request);
                        if (token != null && jwtTokenUtil.validateToken(token)) {
                            String username = jwtTokenUtil.getUsernameFromToken(token);
                            return new JwtTokenPrincipal(username, token);
                        }
                        return null;
                    }
                })
                .addInterceptors(new HandshakeInterceptor() {
                    @Override
                    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) {
                        String token = resolveTokenFromQuery(request);
                        if (token != null) {
                            attributes.put("Authorization", "Bearer " + token);
                        }
                        return true;
                    }

                    @Override
                    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception ex) {
                      
                    }
                })
                .withSockJS();
    }

    private String resolveTokenFromQuery(ServerHttpRequest request) {
        String query = request.getURI().getQuery();
        if (query != null) {
            for (String param : query.split("&")) {
                if (param.startsWith("token=")) {
                    return param.split("=")[1];
                }
            }
        }
        return null;
    }
}
