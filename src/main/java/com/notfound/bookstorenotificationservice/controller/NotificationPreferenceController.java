package com.notfound.bookstorenotificationservice.controller;

import com.notfound.bookstorenotificationservice.auth.CurrentUserIdResolver;
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
    private final CurrentUserIdResolver currentUserIdResolver;

    public NotificationPreferenceController(
            NotificationPreferenceService notificationPreferenceService,
            CurrentUserIdResolver currentUserIdResolver) {
        this.notificationPreferenceService = notificationPreferenceService;
        this.currentUserIdResolver = currentUserIdResolver;
    }

    @GetMapping("/me")
    public ResponseEntity<NotificationPreferencesResponse> getMyPreferences(
            @RequestHeader(value = "X-User-Id", required = false) String xUserId,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        UUID userId = currentUserIdResolver.resolve(xUserId, authorizationHeader);
        return ResponseEntity.ok(notificationPreferenceService.getPreferences(userId));
    }

    @PatchMapping("/me")
    public ResponseEntity<NotificationPreferencesResponse> updateMyPreferences(
            @RequestHeader(value = "X-User-Id", required = false) String xUserId,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @RequestBody UpdateNotificationPreferencesRequest request) {
        UUID userId = currentUserIdResolver.resolve(xUserId, authorizationHeader);
        return ResponseEntity.ok(notificationPreferenceService.updatePreferences(userId, request));
    }
}

