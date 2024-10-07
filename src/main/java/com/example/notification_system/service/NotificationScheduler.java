package com.example.notification_system.service;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.notification_system.model.Event;
import com.example.notification_system.model.Participant;
import com.example.notification_system.repository.EventRepository;
import com.example.notification_system.repository.ParticipantRepository;

@Service
public class NotificationScheduler {

    private final EventRepository eventRepository;
    private final NotificationService notificationService;
    private final EventService eventService;
     private final ParticipantRepository participantRepository;

    public NotificationScheduler(EventRepository eventRepository, NotificationService notificationService, EventService eventService, ParticipantRepository participantRepository) {
        this.eventRepository = eventRepository;
        this.notificationService = notificationService;
        this.eventService = eventService;
        this.participantRepository = participantRepository;
    }

    @Transactional(readOnly = true) 
    @Scheduled(fixedRate = 60000)
    public void checkAndSendNotifications() {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
     
        List<Event> events = eventRepository.findAllByNotificationTime(now);
    
        for (Event event : events) {
            if (event.isNotificationEnabled()) {
                notificationService.sendNotificationToUser(event);
            }
            List<Participant> participants = participantRepository.findByEventId(event.getId());
            for (Participant participant : participants) {
                if (participant.getNotificationEnabled()) {
                    notificationService.sendNotificationToParticipant(participant, event);
                }
            }
            eventService.setNextNotificationTime(event);
        }
    }
    

}
