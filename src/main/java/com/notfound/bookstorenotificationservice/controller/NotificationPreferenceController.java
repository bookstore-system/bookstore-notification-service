package com.notfound.bookstorenotificationservice.controller;

import com.notfound.bookstorenotificationservice.exception.MissingCurrentUserException;
import com.notfound.bookstorenotificationservice.model.dto.NotificationPreferencesResponse;
import com.notfound.bookstorenotificationservice.model.dto.UpdateNotificationPreferencesRequest;
import com.notfound.bookstorenotificationservice.service.NotificationPreferenceService;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/notifications/preferences")
public class NotificationPreferenceController {

    private final NotificationPreferenceService notificationPreferenceService;

    public NotificationPreferenceController(NotificationPreferenceService notificationPreferenceService) {
        this.notificationPreferenceService = notificationPreferenceService;
    }

    @GetMapping("/me")
    public ResponseEntity<NotificationPreferencesResponse> getMyPreferences(
            @RequestHeader(value = "X-User-Id", required = false) String xUserId) {
        UUID userId = resolveUserId(xUserId);
        return ResponseEntity.ok(notificationPreferenceService.getPreferences(userId));
    }

    @PatchMapping("/me")
    public ResponseEntity<NotificationPreferencesResponse> updateMyPreferences(
            @RequestHeader(value = "X-User-Id", required = false) String xUserId,
            @RequestBody UpdateNotificationPreferencesRequest request) {
        UUID userId = resolveUserId(xUserId);
        return ResponseEntity.ok(notificationPreferenceService.updatePreferences(userId, request));
    }

    private UUID resolveUserId(String xUserId) {
        if (xUserId == null || xUserId.isBlank()) {
            throw new MissingCurrentUserException("Missing X-User-Id header");
        }
        try {
            return UUID.fromString(xUserId);
        } catch (IllegalArgumentException e) {
            throw new MissingCurrentUserException("Invalid X-User-Id header");
        }
    }
}

