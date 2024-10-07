package com.example.notification_system.controller;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.notification_system.dto.EventDTO;
import com.example.notification_system.dto.ParticipantDTO;
import com.example.notification_system.model.Event;
import com.example.notification_system.security.JwtTokenUtil;
import com.example.notification_system.security.KeycloakUtil;
import com.example.notification_system.service.EventService;
import com.example.notification_system.service.ParticipantService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@RestController
@RequestMapping("/api/events")
public class EventController {

    private final EventService eventService;
    private final ParticipantService participantService;
    private final KeycloakUtil keycloakUtil;
    private final JwtTokenUtil jwtTokenUtil;
    @Autowired
    public EventController(EventService eventService,JwtTokenUtil jwtTokenUtil,  KeycloakUtil keycloakUtil, ParticipantService participantService) {
        this.eventService = eventService;
        this.keycloakUtil = keycloakUtil;
        this.participantService = participantService;
        this.jwtTokenUtil = jwtTokenUtil;
    }

    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<EventDTO> createEvent(
            @RequestPart("event") @Valid String eventJson,
            @RequestPart(value = "file", required = false) MultipartFile file) throws IOException {

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        EventDTO eventDTO = objectMapper.readValue(eventJson, EventDTO.class);

        String userId = keycloakUtil.getUserIdFromToken();
        Event createdEvent = eventService.createEvent(eventDTO, file, userId);

        EventDTO createdEventDTO = convertToDTO(createdEvent);
        return new ResponseEntity<>(createdEventDTO, HttpStatus.CREATED);
    }

    @GetMapping("/user")
    public ResponseEntity<List<EventDTO>> getEventsByCurrentUser() {
        String userId = keycloakUtil.getUserIdFromToken();
        List<Event> events = eventService.getEventsByUserId(userId);
        List<EventDTO> eventDTOs = events.stream().map(this::convertToDTO).toList();
        return new ResponseEntity<>(eventDTOs, HttpStatus.OK);
    }

    @DeleteMapping("/{eventId}")
    public ResponseEntity<Void> deleteEvent(@PathVariable Long eventId) {
        String userId = keycloakUtil.getUserIdFromToken();
        eventService.deleteEvent(eventId, userId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PutMapping(value = "/{eventId}", consumes = {"multipart/form-data"})
    public ResponseEntity<EventDTO> updateEvent(
            @PathVariable Long eventId,
            @RequestPart("event") @Valid String eventJson,
            @RequestPart(value = "file", required = false) MultipartFile file) throws IOException {

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        EventDTO eventDTO = objectMapper.readValue(eventJson, EventDTO.class);

        Event updatedEvent = eventService.updateEvent(eventId, eventDTO, file);
        EventDTO updatedEventDTO = convertToDTO(updatedEvent);
        return new ResponseEntity<>(updatedEventDTO, HttpStatus.OK);
    }

    @PostMapping("/{eventId}/stop-notifications")
   public ResponseEntity<Void> stopNotificationsPost(@PathVariable Long eventId, @RequestHeader("Authorization") String token) {
        String username = jwtTokenUtil.getUsernameFromToken(token.substring(7));
        boolean isOwner = eventService.isEventOwner(eventId, username);
 
        if (isOwner) {
            eventService.stopEventNotifications(eventId);
        } else {
            eventService.stopParticipantNotifications(eventId, username);
        }
 
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/change-notification-enable/{eventId}")
    public ResponseEntity<Void> changeNotificationEnable(@PathVariable Long eventId, 
                                                        @RequestBody Map<String, Boolean> payload, 
                                                        @RequestHeader("Authorization") String token) {
        String username = jwtTokenUtil.getUsernameFromToken(token.substring(7)); 
        boolean isOwner = eventService.isEventOwner(eventId, username);
        Boolean notificationEnabled = payload.get("notificationEnabled");

        if (isOwner) {
            eventService.updateEventNotificationStatus(eventId, notificationEnabled);
        } else {
            eventService.updateParticipantNotificationStatus(eventId, username, notificationEnabled);
        }

        return new ResponseEntity<>(HttpStatus.OK);
    }



    @GetMapping("/email/{email}")
    public ResponseEntity<Boolean> checkUserEmail(@PathVariable String email) {
        boolean exists = eventService.checkUserEmail(email);
        return new ResponseEntity<>(exists, HttpStatus.OK);
    }

    private EventDTO convertToDTO(Event event) {
        String userId = keycloakUtil.getUserIdFromToken(); 
        boolean notificationEnabled = event.getUserId().equals(userId)
        ? event.isNotificationEnabled()
        : eventService.getParticipantNotificationEnabled(event.getId(), userId);

        EventDTO eventDTO = new EventDTO();
        eventDTO.setId(event.getId());
        eventDTO.setEventName(event.getEventName());
        eventDTO.setEventTime(event.getEventTime());
        eventDTO.setNotificationTime(event.getNotificationTime());
        eventDTO.setNotificationFrequency(event.getNotificationFrequency());
        eventDTO.setNotificationInterval(event.getNotificationInterval());
        eventDTO.setNotificationStartTime(event.getNotificationStartTime());
        eventDTO.setNotificationEnabled(notificationEnabled);
        eventDTO.setTimeZone(event.getTimeZone());
        eventDTO.setFileName(event.getFileName());

        List<ParticipantDTO> participants = participantService.getParticipantsByEventId(event.getId());
        eventDTO.setParticipants(participants);

        return eventDTO;
    }
}
