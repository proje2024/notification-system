package com.example.notification_system.repository;

import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.notification_system.model.Event;

public interface EventRepository extends JpaRepository<Event, Long> {

    @Query("SELECT e FROM Event e WHERE e.notificationTime <= :notificationTime AND e.eventTime > :notificationTime AND e.notificationEnabled = true")
    List<Event> findEventsForNotifications(@Param("notificationTime") OffsetDateTime notificationTime);
    @Query("SELECT e FROM Event e WHERE e.notificationTime <= :notificationTime AND e.eventTime > :notificationTime")
    List<Event> findAllByNotificationTime(@Param("notificationTime") OffsetDateTime notificationTime);
    List<Event> findByUserId(String userId);
}
