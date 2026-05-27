package com.notfound.bookstorenotificationservice.model.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public class PasswordResetOtpEvent {

    private UUID eventId;
    private String type;
    private LocalDateTime occurredAt;
    private UUID userId;
    private String email;
    private String displayName;
    private String otp;
    private Integer expiresInMinutes;

    public PasswordResetOtpEvent() {
    }

    public UUID getEventId() {
        return eventId;
    }

    public void setEventId(UUID eventId) {
        this.eventId = eventId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }

    public void setOccurredAt(LocalDateTime occurredAt) {
        this.occurredAt = occurredAt;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }

    public Integer getExpiresInMinutes() {
        return expiresInMinutes;
    }

    public void setExpiresInMinutes(Integer expiresInMinutes) {
        this.expiresInMinutes = expiresInMinutes;
    }

    @Override
    public String toString() {
        return "PasswordResetOtpEvent{"
                + "eventId=" + eventId
                + ", type='" + type + '\''
                + ", occurredAt=" + occurredAt
                + ", userId=" + userId
                + ", email='" + (email != null ? "***" : null) + '\''
                + ", displayName='" + displayName + '\''
                + ", otp=" + (otp != null ? "[present]" : "null")
                + ", expiresInMinutes=" + expiresInMinutes
                + '}';
    }
}
