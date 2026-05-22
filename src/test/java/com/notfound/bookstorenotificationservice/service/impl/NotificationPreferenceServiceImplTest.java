package com.notfound.bookstorenotificationservice.service.impl;

import com.notfound.bookstorenotificationservice.model.dto.UpdateNotificationPreferencesRequest;
import com.notfound.bookstorenotificationservice.model.entity.NotificationPreference;
import com.notfound.bookstorenotificationservice.model.enums.NotificationChannel;
import com.notfound.bookstorenotificationservice.repository.NotificationPreferenceRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class NotificationPreferenceServiceImplTest {

    @Test
    void updatePreferences_upsertsPromotionEmailPreference() {
        NotificationPreferenceRepository repository = mock(NotificationPreferenceRepository.class);
        NotificationPreferenceServiceImpl service = new NotificationPreferenceServiceImpl(repository);
        UUID userId = UUID.randomUUID();

        when(repository.findByUserIdAndChannel(userId, NotificationChannel.PROMOTION_EMAIL))
                .thenReturn(Optional.empty());
        when(repository.save(any(NotificationPreference.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UpdateNotificationPreferencesRequest request = new UpdateNotificationPreferencesRequest();
        request.setPromotionEmail(true);

        service.updatePreferences(userId, request);

        verify(repository).save(any(NotificationPreference.class));
    }

    @Test
    void findEnabledUserIds_mapsPreferencesToUserIds() {
        NotificationPreferenceRepository repository = mock(NotificationPreferenceRepository.class);
        NotificationPreferenceServiceImpl service = new NotificationPreferenceServiceImpl(repository);
        UUID userId = UUID.randomUUID();
        NotificationPreference preference = new NotificationPreference();
        preference.setUserId(userId);
        preference.setChannel(NotificationChannel.PROMOTION_EMAIL);
        preference.setEnabled(true);
        PageRequest pageable = PageRequest.of(0, 100);

        when(repository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(preference), pageable, 1));

        assertThat(service.findEnabledUserIds(NotificationChannel.PROMOTION_EMAIL, pageable).getContent())
                .containsExactly(userId);
    }
}

