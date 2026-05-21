package com.notfound.bookstorenotificationservice.service;

import com.notfound.bookstorenotificationservice.model.dto.NotificationPreferencesResponse;
import com.notfound.bookstorenotificationservice.model.dto.UpdateNotificationPreferencesRequest;
import com.notfound.bookstorenotificationservice.model.entity.NotificationPreference;
import com.notfound.bookstorenotificationservice.model.enums.NotificationChannel;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NotificationPreferenceService {

    NotificationPreference upsertPreference(UUID userId, NotificationChannel channel, boolean enabled);

    NotificationPreferencesResponse getPreferences(UUID userId);

    NotificationPreferencesResponse updatePreferences(UUID userId, UpdateNotificationPreferencesRequest request);

    Page<UUID> findEnabledUserIds(NotificationChannel channel, Pageable pageable);
}

