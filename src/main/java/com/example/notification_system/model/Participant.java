package com.example.notification_system.model;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "participants")
public class Participant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Kullanıcı ID gereklidir")
    private String toUserId;

    @NotNull(message = "Kullanıcı ID gereklidir")
    private String fromUserId;

    @NotBlank(message = "E-posta gereklidir")
    @Email(message = "Geçerli bir e-posta adresi olmalıdır")
    private String toUserEmail;

    @NotBlank(message = "Durum gereklidir")
    private String status;

    private Boolean notificationEnabled = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    public Long getId() {return id;}

    public void setId(Long id) {this.id = id;}

    public String getToUserId() {return toUserId;}

    public void setToUserId(String toUserId) {this.toUserId = toUserId; }

    public String getFromUserId() {return fromUserId;}

    public void setFromUserId(String fromUserId) {this.fromUserId = fromUserId;}

    public String getToUserEmail() {return toUserEmail;}

    public void setToUserEmail(String toUserEmail) {this.toUserEmail = toUserEmail;}

    public String getStatus() {return status;}

    public void setStatus(String status) {this.status = status;}
    
    public Boolean getNotificationEnabled() {return notificationEnabled;}

    public void setNotificationEnabled(Boolean notificationEnabled) {this.notificationEnabled = notificationEnabled;}

    public Event getEvent() {return event;}

    public void setEvent(Event event) {this.event = event;}
}
