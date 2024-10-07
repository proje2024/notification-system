package com.example.notification_system.model;

import java.time.OffsetDateTime;

import javax.persistence.Entity;
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
@Table(name = "email_logs")
public class EmailLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "event_id")
    private Event event;

    @Email(message = "Geçerli bir e-posta adresi olmalıdır")
    @NotBlank(message = "E-posta zorunludur")
    private String email;

    @NotBlank(message = "Durum zorunludur")
    private String status;

    @NotNull(message = "Deneme Tarihi zorunludur")
    private OffsetDateTime attemptTime;


    public Event getEvent() {return event; }

    public void setEvent(Event event) {this.event = event;}

    public String getEmail() {return email;}

    public void setEmail(String email) {this.email = email;}

    public String getStatus() {return status;}

    public void setStatus(String status) {this.status = status;}

    public OffsetDateTime getAttemptTime() {return attemptTime;}

    public void setAttemptTime(OffsetDateTime attemptTime) {this.attemptTime = attemptTime;}

    public Long getId() {return id;}

    public void setId(Long id) {this.id = id;}
}
