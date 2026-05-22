package com.notfound.bookstorenotificationservice.notification;

import com.notfound.bookstorenotificationservice.client.UserContactInfoResponse;
import com.notfound.bookstorenotificationservice.client.UserContactResolver;
import com.notfound.bookstorenotificationservice.model.dto.PromotionCreatedEvent;
import com.notfound.bookstorenotificationservice.model.enums.NotificationChannel;
import com.notfound.bookstorenotificationservice.service.MailDeliveryService;
import com.notfound.bookstorenotificationservice.service.NotificationPreferenceService;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PromotionEmailDeliveryStrategyTest {

    @Test
    void deliver_activePromotion_sendsToPromotionEmailSubscribers() {
        NotificationPreferenceService preferenceService = mock(NotificationPreferenceService.class);
        UserContactResolver userContactResolver = mock(UserContactResolver.class);
        MailDeliveryService mailDeliveryService = mock(MailDeliveryService.class);
        PromotionEmailDeliveryStrategy strategy =
                new PromotionEmailDeliveryStrategy(preferenceService, userContactResolver, mailDeliveryService, 100);
        UUID userId = UUID.randomUUID();
        PromotionCreatedEvent event = activePromotion();
        UserContactInfoResponse contactInfo = new UserContactInfoResponse();
        contactInfo.setUserId(userId);
        contactInfo.setEmail("reader@example.com");

        when(preferenceService.findEnabledUserIds(eq(NotificationChannel.PROMOTION_EMAIL), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(userId), PageRequest.of(0, 100), 1));
        when(userContactResolver.resolveContact(userId)).thenReturn(contactInfo);

        strategy.deliver(event);

        verify(mailDeliveryService)
                .sendHtmlEmail(eq("reader@example.com"), any(String.class), any(String.class), eq("promotion"));
    }

    @Test
    void deliver_inactivePromotion_skipsSubscribers() {
        NotificationPreferenceService preferenceService = mock(NotificationPreferenceService.class);
        UserContactResolver userContactResolver = mock(UserContactResolver.class);
        MailDeliveryService mailDeliveryService = mock(MailDeliveryService.class);
        PromotionEmailDeliveryStrategy strategy =
                new PromotionEmailDeliveryStrategy(preferenceService, userContactResolver, mailDeliveryService, 100);
        PromotionCreatedEvent event = activePromotion();
        event.setStatus("INACTIVE");

        strategy.deliver(event);

        verify(preferenceService, never()).findEnabledUserIds(any(), any());
        verify(mailDeliveryService, never()).sendHtmlEmail(any(), any(), any(), any());
    }

    private PromotionCreatedEvent activePromotion() {
        PromotionCreatedEvent event = new PromotionCreatedEvent();
        event.setPromotionId(UUID.randomUUID());
        event.setCode("SAVE10");
        event.setName("Save 10");
        event.setDiscountValue(10.0);
        event.setStartDate(LocalDate.now());
        event.setEndDate(LocalDate.now().plusDays(7));
        event.setStatus("ACTIVE");
        return event;
    }
}
