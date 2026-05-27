package com.notfound.bookstorenotificationservice.notification;

import com.notfound.bookstorenotificationservice.client.UserContactInfoResponse;
import com.notfound.bookstorenotificationservice.client.UserContactResolver;
import com.notfound.bookstorenotificationservice.exception.NotificationDeliveryException;
import com.notfound.bookstorenotificationservice.model.dto.PromotionCreatedEvent;
import com.notfound.bookstorenotificationservice.model.enums.NotificationChannel;
import com.notfound.bookstorenotificationservice.service.MailDeliveryService;
import com.notfound.bookstorenotificationservice.service.NotificationPreferenceService;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executor;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.retry.backoff.NoBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PromotionEmailDeliveryStrategyTest {

    @Test
    void deliver_activePromotion_sendsToPromotionEmailSubscribers() {
        NotificationPreferenceService preferenceService = mock(NotificationPreferenceService.class);
        UserContactResolver userContactResolver = mock(UserContactResolver.class);
        MailDeliveryService mailDeliveryService = mock(MailDeliveryService.class);
        Executor directExecutor = Runnable::run;
        PromotionEmailDeliveryStrategy strategy =
            new PromotionEmailDeliveryStrategy(
                preferenceService, userContactResolver, mailDeliveryService, directExecutor, retryTemplate(), 100);
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
        Executor directExecutor = Runnable::run;
        PromotionEmailDeliveryStrategy strategy =
            new PromotionEmailDeliveryStrategy(
                preferenceService, userContactResolver, mailDeliveryService, directExecutor, retryTemplate(), 100);
        PromotionCreatedEvent event = activePromotion();
        event.setStatus("INACTIVE");

        strategy.deliver(event);

        verify(preferenceService, never()).findEnabledUserIds(any(), any());
        verify(mailDeliveryService, never()).sendHtmlEmail(any(), any(), any(), any());
    }

    @Test
    void deliver_deliveryFailure_retriesPromotionEmailJob() {
        NotificationPreferenceService preferenceService = mock(NotificationPreferenceService.class);
        UserContactResolver userContactResolver = mock(UserContactResolver.class);
        MailDeliveryService mailDeliveryService = mock(MailDeliveryService.class);
        Executor directExecutor = Runnable::run;
        PromotionEmailDeliveryStrategy strategy =
            new PromotionEmailDeliveryStrategy(
                preferenceService, userContactResolver, mailDeliveryService, directExecutor, retryTemplate(), 100);
        UUID userId = UUID.randomUUID();
        PromotionCreatedEvent event = activePromotion();
        UserContactInfoResponse contactInfo = new UserContactInfoResponse();
        contactInfo.setUserId(userId);
        contactInfo.setEmail("reader@example.com");

        when(preferenceService.findEnabledUserIds(eq(NotificationChannel.PROMOTION_EMAIL), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(userId), PageRequest.of(0, 100), 1));
        when(userContactResolver.resolveContact(userId)).thenReturn(contactInfo);
        doThrow(new NotificationDeliveryException("smtp down"))
                .doNothing()
                .when(mailDeliveryService)
                .sendHtmlEmail(eq("reader@example.com"), any(String.class), any(String.class), eq("promotion"));

        strategy.deliver(event);

        verify(mailDeliveryService, times(2))
                .sendHtmlEmail(eq("reader@example.com"), any(String.class), any(String.class), eq("promotion"));
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

    private RetryTemplate retryTemplate() {
        RetryTemplate retryTemplate = new RetryTemplate();
        retryTemplate.setRetryPolicy(new SimpleRetryPolicy(
                3,
                Map.of(NotificationDeliveryException.class, true)));
        retryTemplate.setBackOffPolicy(new NoBackOffPolicy());
        return retryTemplate;
    }
}
