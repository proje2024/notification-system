package com.example.notification_system.dto;

import java.time.OffsetDateTime;
import java.util.List;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public class EventDTO {
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

    private boolean notificationEnabled;

    @NotBlank(message = "Zaman Dilimi zorunludur")
    private String timeZone;
    
    @NotBlank(message = "Kullanıcı ID'si zorunludur")
    private String userId;

    private String fileName;
    private boolean fileDeleted;
    private List<String> invitees;
    private List<ParticipantDTO> participants;


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

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public boolean isFileDeleted() { return fileDeleted; }
    public void setFileDeleted(boolean fileDeleted) { this.fileDeleted = fileDeleted; }

    public List<String> getInvitees() { return invitees; }
    public void setInvitees(List<String> invitees) { this.invitees = invitees; }

    public List<ParticipantDTO> getParticipants() { return participants; }
    public void setParticipants(List<ParticipantDTO> participants) { this.participants = participants; }
}
