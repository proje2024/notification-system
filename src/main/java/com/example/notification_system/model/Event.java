package com.example.notification_system.model;

import java.time.OffsetDateTime;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "events")
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Etkinlik Adı zorunludur")
    private String eventName;

    @NotNull(message = "Etkinlik Tarihi zorunludur")
    private OffsetDateTime eventTime;

    private OffsetDateTime notificationTime;

    @NotNull(message = "Bildirim Sıklığı zorunludur")
    private Integer notificationFrequency;

    @NotBlank(message = "Bildirim Aralığı zorunludur")
    private String notificationInterval;

    @NotNull(message = "Bildirim Başlangıç Tarihi zorunludur")
    private OffsetDateTime notificationStartTime;

    private boolean notificationEnabled = true;

    @NotBlank(message = "Zaman Dilimi zorunludur")
    private String timeZone;

    @NotBlank(message = "Kullanıcı ID'si zorunludur")
    private String userId;

    @OneToMany(mappedBy = "event", cascade = CascadeType.REMOVE, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Participant> participants;

    @OneToMany(mappedBy = "event", cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
    private List<EmailLog> emailLogs;

    private String fileName;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEventName() { return eventName; }
    public void setEventName(String eventName) { this.eventName = eventName; }

    public OffsetDateTime getEventTime() { return eventTime; }
    public void setEventTime(OffsetDateTime eventTime) { this.eventTime = eventTime; }

    public OffsetDateTime getNotificationTime() { return notificationTime; }
    public void setNotificationTime(OffsetDateTime notificationTime) { this.notificationTime = notificationTime; }

    public Integer getNotificationFrequency() { return notificationFrequency; }
    public void setNotificationFrequency(Integer notificationFrequency) { this.notificationFrequency = notificationFrequency; }

    public String getNotificationInterval() { return notificationInterval; }
    public void setNotificationInterval(String notificationInterval) { this.notificationInterval = notificationInterval; }

    public OffsetDateTime getNotificationStartTime() { return notificationStartTime; }
    public void setNotificationStartTime(OffsetDateTime notificationStartTime) { this.notificationStartTime = notificationStartTime; }

    public boolean isNotificationEnabled() { return notificationEnabled; }
    public void setNotificationEnabled(boolean notificationEnabled) { this.notificationEnabled = notificationEnabled; }

    public String getTimeZone() { return timeZone; }
    public void setTimeZone(String timeZone) { this.timeZone = timeZone; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public List<EmailLog> getEmailLogs() { return emailLogs; }
    public void setEmailLogs(List<EmailLog> emailLogs) { this.emailLogs = emailLogs; }

    public List<Participant> getParticipants() { return participants; }
    public void setParticipants(List<Participant> participants) { this.participants = participants; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
}
