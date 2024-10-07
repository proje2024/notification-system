package com.example.notification_system.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import com.example.notification_system.dto.EventDTO;
import com.example.notification_system.dto.ParticipantDTO;
import com.example.notification_system.exception.EventNotFoundException;
import com.example.notification_system.exception.EventValidationException;
import com.example.notification_system.model.Event;
import com.example.notification_system.model.Participant;
import com.example.notification_system.repository.EventRepository;
import com.example.notification_system.repository.ParticipantRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;


@Service
public class EventService {

    private final EventRepository eventRepository;
    private final String uploadDir;
    private final ParticipantService participantService;
    private final ParticipantRepository participantRepository;
    private static final Logger logger = Logger.getLogger(EventService.class.getName());
    private static final String STORAGE_DIR = "storage";
    private static final String EVENT_NOT_FOUND_MESSAGE = "Etkinlik bulunamadı: ";
    private final RestTemplate restTemplate;

    @Value("${keycloak.auth-server-url}")
    private String keycloakServerUrl;

    @Value("${keycloak.realm}")
    private String keycloakRealm;

    @Value("${keycloak.resource}")
    private String clientId;

    @Value("${keycloak.credentials.secret}")
    private String clientSecret;

    @Autowired
    public EventService(EventRepository eventRepository, @Value("${file.upload-dir}") String uploadDir, ParticipantService participantService, ParticipantRepository participantRepository) {
        this.eventRepository = eventRepository;
        this.uploadDir = uploadDir;
        this.participantService = participantService;
        this.participantRepository = participantRepository;
        this.restTemplate = new RestTemplate();
    }
    @Transactional(readOnly = false) 
    public Event createEvent(EventDTO eventDTO, MultipartFile file, String userId) {
        validateEvent(eventDTO);

        Event event = convertToEntity(eventDTO);
        event.setUserId(userId); 

        if (file != null && !file.isEmpty()) {
            try {
                byte[] bytes = file.getBytes();
                String originalFileName = file.getOriginalFilename();
                if (originalFileName != null) {
                    String extension = originalFileName.substring(originalFileName.lastIndexOf('.'));
                    String uniqueFileName = UUID.randomUUID().toString() + extension;
                    Path path = Paths.get(uploadDir, uniqueFileName);
                    Files.write(path, bytes);

                    Path localPath = Paths.get(STORAGE_DIR, uniqueFileName);
                    Files.createDirectories(localPath.getParent());
                    Files.write(localPath, bytes);
                    event.setFileName(uniqueFileName);
                }
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Dosya yükleme başarısız", e);
            }
        }

        setInitialNotificationTime(event);
        event = eventRepository.save(event);

        if (eventDTO.getInvitees() != null) {
            for (String email : eventDTO.getInvitees()) {
                ParticipantDTO participantDTO = new ParticipantDTO();
                participantDTO.setToUserEmail(email);
                participantDTO.setEventId(event.getId());
                participantService.addParticipant(participantDTO, userId); 
            }
        }

        return event;
    }
    @Transactional(readOnly = true) 
    public List<Event> getEventsByUserId(String userId) {
        List<Event> userEvents = eventRepository.findByUserId(userId);
        List<Long> participantEventIds = participantRepository.findByToUserId(userId)
                .stream()
                .map(participant -> participant.getEvent().getId())
                .collect(Collectors.toList());

        participantEventIds.removeAll(userEvents.stream().map(Event::getId).collect(Collectors.toList()));
        List<Event> participantEvents = eventRepository.findAllById(participantEventIds);
        userEvents.addAll(participantEvents);

        return userEvents.stream().distinct().collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public boolean getParticipantNotificationEnabled(Long eventId, String userId) {
        Participant participant = participantRepository.findByEventIdAndToUserId(eventId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Katılımcı bulunamadı"));
        return participant.getNotificationEnabled();
    }
    

    @Transactional(readOnly = false) 
    public void deleteEvent(Long eventId, String userId) {
        Optional<Event> eventOptional = eventRepository.findById(eventId);

        if (eventOptional.isPresent()) {
            Event event = eventOptional.get();

            if (event.getUserId().equals(userId)) { 
                if (event.getFileName() != null) {
                    try {
                        Path path = Paths.get(uploadDir, event.getFileName());
                        Files.deleteIfExists(path);

                        Path localPath = Paths.get(STORAGE_DIR, event.getFileName());
                        Files.deleteIfExists(localPath);
                    } catch (IOException e) {
                        logger.log(Level.SEVERE, "Dosya silme başarısız", e);
                    }
                }
                eventRepository.delete(event);
            } else {
                participantRepository.deleteByEventIdAndToUserId(eventId, userId);
            }
        } else {
            throw new EventNotFoundException("Event bulunamadı: " + eventId);
        }
    }
    @Transactional(readOnly = false) 
    public Event updateEvent(Long eventId, EventDTO eventDTO, MultipartFile file) {
        Optional<Event> eventOptional = eventRepository.findById(eventId);
        if (!eventOptional.isPresent()) {
            throw new EventNotFoundException(EVENT_NOT_FOUND_MESSAGE + eventId);
        }

        validateEvent(eventDTO);

        Event event = eventOptional.get();

     
        event.setEventName(eventDTO.getEventName());
        event.setEventTime(eventDTO.getEventTime().withOffsetSameInstant(ZoneOffset.UTC));
        event.setNotificationFrequency(eventDTO.getNotificationFrequency());
        event.setNotificationInterval(eventDTO.getNotificationInterval());
        event.setNotificationStartTime(eventDTO.getNotificationStartTime().withOffsetSameInstant(ZoneOffset.UTC));
        event.setNotificationEnabled(eventDTO.isNotificationEnabled());
        event.setTimeZone(eventDTO.getTimeZone());

        if (eventDTO.isFileDeleted()) {
            if (event.getFileName() != null) {
                try {
                    Path path = Paths.get(uploadDir, event.getFileName());
                    Files.deleteIfExists(path);
                    Path localPath = Paths.get(STORAGE_DIR, event.getFileName());
                    Files.deleteIfExists(localPath);
                } catch (IOException e) {
                    logger.log(Level.SEVERE, "Dosya silme başarısız", e);
                }
                event.setFileName(null);
            }
        } else if (file != null && !file.isEmpty()) {
            try {
                if (event.getFileName() != null) {
                    Path oldPath = Paths.get(uploadDir, event.getFileName());
                    Files.deleteIfExists(oldPath);
                    Path oldLocalPath = Paths.get(STORAGE_DIR, event.getFileName());
                    Files.deleteIfExists(oldLocalPath);
                }

                byte[] bytes = file.getBytes();
                String originalFileName = file.getOriginalFilename();
                if (originalFileName != null) {
                    String extension = originalFileName.substring(originalFileName.lastIndexOf('.'));
                    String uniqueFileName = UUID.randomUUID().toString() + extension;
                    Path path = Paths.get(uploadDir, uniqueFileName);
                    Files.write(path, bytes);
                    Path localPath = Paths.get(STORAGE_DIR, uniqueFileName);
                    Files.createDirectories(localPath.getParent());
                    Files.write(localPath, bytes);
                    event.setFileName(uniqueFileName);
                }
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Dosya yükleme başarısız", e);
            }
        }

        List<Participant> existingParticipants = participantRepository.findByEventId(eventId);
        List<String> newInviteesEmails = eventDTO.getInvitees() != null ? eventDTO.getInvitees() : List.of();

        for (Participant participant : existingParticipants) {
            if (!newInviteesEmails.contains(participant.getToUserEmail())) {
                participantRepository.delete(participant);
            }
        }

        for (String email : newInviteesEmails) {
            Participant existingParticipant = existingParticipants.stream()
                    .filter(participant -> participant.getToUserEmail().equals(email))
                    .findFirst()
                    .orElse(null);

            if (existingParticipant == null) {
                ParticipantDTO participantDTO = new ParticipantDTO();
                participantDTO.setToUserEmail(email);
                participantDTO.setEventId(event.getId());
                participantService.addParticipant(participantDTO, event.getUserId());
            }
        }
        setNextNotificationTime(event);
        return eventRepository.save(event);
        }

    @Transactional(readOnly = false)
    public void stopEventNotifications(Long eventId) {
        Event event = getEventById(eventId);
        event.setNotificationEnabled(false);
        eventRepository.save(event);
    }
    @Transactional(readOnly = false) 
    public void stopParticipantNotifications(Long eventId, String username) {
        Participant participant = participantRepository.findByEventIdAndToUserId(eventId, username)
                .orElseThrow(() -> new IllegalArgumentException("Katılımcı bulunamadı"));
        participant.setNotificationEnabled(false);
        participantRepository.save(participant);
    }
    @Transactional(readOnly = false) 
    public void updateEventNotificationStatus(Long eventId, boolean notificationEnabled) {
        Event event = getEventById(eventId);
        event.setNotificationEnabled(notificationEnabled);
        eventRepository.save(event);
    }
    
    @Transactional(readOnly = false) 
    public void updateParticipantNotificationStatus(Long eventId, String username, boolean notificationEnabled) {
        Participant participant = participantRepository.findByEventIdAndToUserId(eventId, username)
            .orElseThrow(() -> new IllegalArgumentException("Katılımcı bulunamadı"));
        participant.setNotificationEnabled(notificationEnabled);
        participantRepository.save(participant);
    }
    
    @Transactional(readOnly = true) 
    public boolean isEventOwner(Long eventId, String username) {
        Event event = eventRepository.findById(eventId).orElseThrow(() -> new EventNotFoundException("Etkinlik bulunamadı"));
        return event.getUserId().equals(username); 
    }

    public void setInitialNotificationTime(Event event) {
        OffsetDateTime nextNotificationTime = event.getNotificationStartTime();
        nextNotificationTime = calculateNextNotificationTime(nextNotificationTime, event.getNotificationFrequency(), event.getNotificationInterval());
        event.setNotificationTime(nextNotificationTime);
    }

    public void setNextNotificationTime(Event event) {
        if (event.isNotificationEnabled()) {
            OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
            OffsetDateTime nextNotificationTime = event.getNotificationTime();
            while (nextNotificationTime.isBefore(now) || nextNotificationTime.equals(now)) {
                nextNotificationTime = calculateNextNotificationTime(nextNotificationTime, event.getNotificationFrequency(), event.getNotificationInterval());
            }
            event.setNotificationTime(nextNotificationTime);
            eventRepository.save(event);
        }
    }

    public Event getEventById(Long eventId) {
        Optional<Event> optionalEvent = eventRepository.findById(eventId);
        if (optionalEvent.isPresent()) {
            return optionalEvent.get();
        } else {
            throw new EventNotFoundException(EVENT_NOT_FOUND_MESSAGE + eventId);
        }
    }
      public boolean checkUserEmail(String email) {
        String adminToken = getAdminAccessToken();
        String url = keycloakServerUrl + "/admin/realms/" + keycloakRealm + "/users";
        
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url)
                .queryParam("email", email);
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + adminToken);
        
        HttpEntity<String> entity = new HttpEntity<>(headers);
        
        ResponseEntity<String> response = restTemplate.exchange(
                builder.toUriString(), HttpMethod.GET, entity, String.class);
        
     
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            JsonNode root = objectMapper.readTree(response.getBody());

            return root.isArray() && root.size() > 0;
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Keycloak yanıtı çözümlenemed", e);
        }
    }
     private String getAdminAccessToken() {
        String tokenUrl = keycloakServerUrl + "/realms/" + keycloakRealm + "/protocol/openid-connect/token";
    
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
    
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "client_credentials");
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);
    
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);
    
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
            tokenUrl, 
            HttpMethod.POST, 
            request, 
            new ParameterizedTypeReference<Map<String, Object>>() {}
        );
    
        Map<String, Object> responseBody = response.getBody();
        if (responseBody != null && responseBody.containsKey("access_token")) {
            return (String) responseBody.get("access_token");
        } else {
          
            throw new RuntimeException("Keycloak'tan erişim tokenı alınamadı");
        }
    }

    private OffsetDateTime calculateNextNotificationTime(OffsetDateTime currentNotificationTime, Integer frequency, String interval) {
        return switch (interval.toLowerCase()) {
            case "minutes" -> currentNotificationTime.plusMinutes(frequency);
            case "hours" -> currentNotificationTime.plusHours(frequency);
            case "days" -> currentNotificationTime.plusDays(frequency);
            case "weeks" -> currentNotificationTime.plusWeeks(frequency);
            case "months" -> currentNotificationTime.plusMonths(frequency);
            case "years" -> currentNotificationTime.plusYears(frequency);
            default -> throw new IllegalArgumentException("Geçersiz aralık: " + interval);
        };
    }

    @Transactional(readOnly = true)
    public List<Event> findEventsForNotifications() {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        return eventRepository.findEventsForNotifications(now);
    }

    private Event convertToEntity(EventDTO eventDTO) {
        Event event = new Event();
        event.setEventName(eventDTO.getEventName());
        event.setEventTime(eventDTO.getEventTime().withOffsetSameInstant(ZoneOffset.UTC));
        event.setNotificationFrequency(eventDTO.getNotificationFrequency());
        event.setNotificationInterval(eventDTO.getNotificationInterval());
        event.setNotificationStartTime(eventDTO.getNotificationStartTime().withOffsetSameInstant(ZoneOffset.UTC));
        event.setNotificationEnabled(eventDTO.isNotificationEnabled());
        event.setTimeZone(eventDTO.getTimeZone());
        event.setFileName(eventDTO.getFileName());
        return event;
    }

    private void validateEvent(EventDTO eventDTO) {
        validateEventTimes(eventDTO.getEventTime(), eventDTO.getNotificationStartTime());
        validateNotificationFrequency(eventDTO.getNotificationFrequency());
    }

    private void validateEventTimes(OffsetDateTime eventTime, OffsetDateTime notificationStartTime) {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        if (eventTime.isBefore(now)) {
            throw new EventValidationException("Etkinlik tarihi bugünden önce olamaz");
        }
        if (notificationStartTime.isAfter(eventTime)) {
            throw new EventValidationException("Bildirim başlangıç tarihi etkinlik tarihinden sonra olamaz");
        }
    }

    private void validateNotificationFrequency(Integer notificationFrequency) {
        if (notificationFrequency < 1) {
            throw new EventValidationException("Bildirim sıklığı 1'den küçük olamaz");
        }
    }
}
