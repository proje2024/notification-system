package com.example.notification_system.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.notification_system.model.Participant;

public interface ParticipantRepository extends JpaRepository<Participant, Long> {
    List<Participant> findByEventId(Long eventId);
    List<Participant> findByToUserId(String toUserId);
    void deleteByEventIdAndToUserId(Long eventId, String toUserId);
    void deleteByEventId(Long eventId); 
    Optional<Participant> findByEventIdAndToUserId(Long eventId, String toUserId);
}
