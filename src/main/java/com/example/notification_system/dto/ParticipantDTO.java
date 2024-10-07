package com.example.notification_system.dto;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

public class ParticipantDTO {
    private Long id;

    @NotBlank(message = "Kullanıcı ID gereklidir")
    private String toUserId;

    @NotBlank(message = "Kullanıcı ID gereklidir")
    private String fromUserId;

    @NotBlank(message = "E-posta gereklidir")
    @Email(message = "Geçerli bir e-posta adresi olmalıdır")
    private String toUserEmail;

    @NotBlank(message = "Durum gereklidir")
    private String status;

    private Long eventId;

    public Long getId() {return id;}

    public void setId(Long id) {this.id = id;}

    public String getToUserId() {return toUserId;}

    public void setToUserId(String toUserId) {this.toUserId = toUserId;}

    public String getFromUserId() {return fromUserId;}

    public void setFromUserId(String fromUserId) {this.fromUserId = fromUserId;}

    public String getToUserEmail() {return toUserEmail;}

    public void setToUserEmail(String toUserEmail) {this.toUserEmail = toUserEmail;}

    public String getStatus() {return status;}

    public void setStatus(String status) {this.status = status;}

    public Long getEventId() {return eventId;}

    public void setEventId(Long eventId) {this.eventId = eventId;}
}
