package com.example.notification_system.service;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.notification_system.dto.EventDTO;
import com.example.notification_system.model.Event;
import com.example.notification_system.model.Participant;
import com.example.notification_system.security.KeycloakUtil;
import com.example.notification_system.websocket.WebSocketEventListener;


@Service
public class NotificationService {

    private final SimpMessagingTemplate template;
    private final EmailService emailService;
    private final KeycloakUtil keycloakUtil;

    private final WebSocketEventListener webSocketEventListener;

    public NotificationService(SimpMessagingTemplate template, EmailService emailService,WebSocketEventListener webSocketEventListener, KeycloakUtil keycloakUtil) {
        this.template = template;
        this.emailService = emailService;
        this.keycloakUtil = keycloakUtil;
        this.webSocketEventListener = webSocketEventListener;
    }


    @Transactional(readOnly = false) 
    public void sendNotificationToUser(Event event) {
        String userId = event.getUserId();
   
        if (webSocketEventListener.isUserActive(userId)) {
            // Kullanıcı aktifse WebSocket bildirimi gönder
            EventDTO eventDTO = convertToDTO(event);
            template.convertAndSendToUser(userId, "/topic/notifications", eventDTO);
        } else {
            // Kullanıcı pasifse e-posta bildirimi gönder
            String email = keycloakUtil.getEmailByUserId(event.getUserId());
            emailService.sendEmailToUser(event, email);
        }
   
    }
    @Transactional(readOnly = false)
    public void sendNotificationToParticipant(Participant participant, Event event) {
        if (!participant.getNotificationEnabled()) {
            return;
        }

        String userId = participant.getToUserId();

        if (webSocketEventListener.isUserActive(userId)) {
            EventDTO eventDTO = convertToDTO(event);
            template.convertAndSendToUser(userId, "/topic/notifications", eventDTO);
        } else {
            emailService.sendEmailToParticipant(event, participant);
        }
    }

    private EventDTO convertToDTO(Event event) {
        EventDTO eventDTO = new EventDTO();
        eventDTO.setId(event.getId());
        eventDTO.setEventName(event.getEventName());
        eventDTO.setEventTime(event.getEventTime());
        return eventDTO;
    }
}
