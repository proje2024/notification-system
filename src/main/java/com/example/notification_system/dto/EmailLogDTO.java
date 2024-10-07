package com.example.notification_system.dto;

import java.time.OffsetDateTime;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public class EmailLogDTO {
    private Long id;

    @Email(message = "Geçerli bir e-posta adresi olmalıdır")
    @NotBlank(message = "E-posta zorunludur")
    private String email;

    @NotBlank(message = "Durum zorunludur")
    private String status;

    @NotNull(message = "Deneme tarihi zorunludur")
    private OffsetDateTime attemptTime;

    public Long getId() {return id;}

    public void setId(Long id) {this.id = id;}

    public String getEmail() {return email;}

    public void setEmail(String email) {this.email = email;}

    public String getStatus() {return status;}

    public void setStatus(String status) {this.status = status;}

    public OffsetDateTime getAttemptTime() {return attemptTime;}

    public void setAttemptTime(OffsetDateTime attemptTime) {this.attemptTime = attemptTime;}
}
