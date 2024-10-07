package com.example.notification_system.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.notification_system.dto.ParticipantDTO;
import com.example.notification_system.exception.EventNotFoundException;
import com.example.notification_system.model.Event;
import com.example.notification_system.model.Participant;
import com.example.notification_system.repository.EventRepository;
import com.example.notification_system.repository.ParticipantRepository;
import com.example.notification_system.security.KeycloakUtil;

@Service
public class ParticipantService {

    private final ParticipantRepository participantRepository;
    private final EventRepository eventRepository;
    private final KeycloakUtil keycloakUtil;

    @Autowired
    public ParticipantService(ParticipantRepository participantRepository, EventRepository eventRepository, KeycloakUtil keycloakUtil) {
        this.participantRepository = participantRepository;
        this.eventRepository = eventRepository;
        this.keycloakUtil = keycloakUtil;
    }

    @Transactional(readOnly = true)
    public List<ParticipantDTO> getParticipantsByEventId(Long eventId) {
        return participantRepository.findByEventId(eventId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = false) 
    public ParticipantDTO addParticipant(ParticipantDTO participantDTO, String fromUserId) {
        Event event = eventRepository.findById(participantDTO.getEventId())
                .orElseThrow(() -> new EventNotFoundException("Event bulunamadı: " + participantDTO.getEventId()));

        Participant participant = new Participant();
        participant.setToUserId(keycloakUtil.getUserIdFromEmail(participantDTO.getToUserEmail())); 
        participant.setFromUserId(fromUserId); 
        participant.setToUserEmail(participantDTO.getToUserEmail()); 
        participant.setStatus("true");
        participant.setEvent(event);

        return convertToDTO(participantRepository.save(participant));
    }

    @Transactional(readOnly = false)
    public void removeParticipant(Long eventId, String userId) { 
        participantRepository.deleteByEventIdAndToUserId(eventId, userId);
    }

    @Transactional(readOnly = false)
    public void removeAllParticipantsByEventId(Long eventId) {
        participantRepository.deleteByEventId(eventId);
    }

    private ParticipantDTO convertToDTO(Participant participant) {
        ParticipantDTO participantDTO = new ParticipantDTO();
        participantDTO.setId(participant.getId());
        participantDTO.setToUserId(participant.getToUserId());
        participantDTO.setFromUserId(participant.getFromUserId());
        participantDTO.setToUserEmail(participant.getToUserEmail());
        participantDTO.setStatus(participant.getStatus());
        participantDTO.setEventId(participant.getEvent().getId());
        return participantDTO;
    }
}
