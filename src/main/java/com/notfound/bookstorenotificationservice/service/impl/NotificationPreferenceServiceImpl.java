package com.notfound.bookstorenotificationservice.service.impl;

import com.notfound.bookstorenotificationservice.model.dto.NotificationPreferencesResponse;
import com.notfound.bookstorenotificationservice.model.dto.UpdateNotificationPreferencesRequest;
import com.notfound.bookstorenotificationservice.model.entity.NotificationPreference;
import com.notfound.bookstorenotificationservice.model.enums.NotificationChannel;
import com.notfound.bookstorenotificationservice.repository.NotificationPreferenceRepository;
import com.notfound.bookstorenotificationservice.service.NotificationPreferenceService;
import com.notfound.bookstorenotificationservice.specification.PromotionEmailEnabledSpecification;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationPreferenceServiceImpl implements NotificationPreferenceService {

    private final NotificationPreferenceRepository notificationPreferenceRepository;

    public NotificationPreferenceServiceImpl(NotificationPreferenceRepository notificationPreferenceRepository) {
        this.notificationPreferenceRepository = notificationPreferenceRepository;
    }

    @Override
    @Transactional
    public NotificationPreference upsertPreference(UUID userId, NotificationChannel channel, boolean enabled) {
        NotificationPreference preference = notificationPreferenceRepository
                .findByUserIdAndChannel(userId, channel)
                .orElseGet(() -> {
                    NotificationPreference created = new NotificationPreference();
                    created.setUserId(userId);
                    created.setChannel(channel);
                    return created;
                });
        preference.setEnabled(enabled);
        return notificationPreferenceRepository.save(preference);
    }

    @Override
    @Transactional(readOnly = true)
    public NotificationPreferencesResponse getPreferences(UUID userId) {
        boolean promotionEmail = notificationPreferenceRepository
                .findByUserIdAndChannel(userId, NotificationChannel.PROMOTION_EMAIL)
                .map(NotificationPreference::isEnabled)
                .orElse(false);
        return new NotificationPreferencesResponse(promotionEmail);
    }

    @Override
    @Transactional
    public NotificationPreferencesResponse updatePreferences(UUID userId, UpdateNotificationPreferencesRequest request) {
        if (request != null && request.getPromotionEmail() != null) {
            upsertPreference(userId, NotificationChannel.PROMOTION_EMAIL, request.getPromotionEmail());
        }
        return getPreferences(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UUID> findEnabledUserIds(NotificationChannel channel, Pageable pageable) {
        Specification<NotificationPreference> specification = channel == NotificationChannel.PROMOTION_EMAIL
                ? PromotionEmailEnabledSpecification.promotionEmailEnabled()
                : PromotionEmailEnabledSpecification.enabledChannel(channel);
        return notificationPreferenceRepository.findAll(specification, pageable)
                .map(NotificationPreference::getUserId);
    }
}

