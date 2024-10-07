package com.example.notification_system.controller;

import com.example.notification_system.dto.EventDTO;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;

@RestController
public class NotificationController {

    private final SimpMessagingTemplate template;

    @Autowired
    public NotificationController(SimpMessagingTemplate template) {
        this.template = template;
    }

    @PostMapping("/notify")
    public void notify(@RequestBody EventDTO eventDTO) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            template.convertAndSend("/topic/notifications", eventDTO);
        }
    }
}
