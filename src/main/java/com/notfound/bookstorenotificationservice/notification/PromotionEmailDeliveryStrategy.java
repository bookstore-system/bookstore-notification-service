package com.notfound.bookstorenotificationservice.notification;

import com.notfound.bookstorenotificationservice.client.UserContactInfoResponse;
import com.notfound.bookstorenotificationservice.client.UserContactResolver;
import com.notfound.bookstorenotificationservice.messaging.PromotionEventTypes;
import com.notfound.bookstorenotificationservice.model.dto.PromotionCreatedEvent;
import com.notfound.bookstorenotificationservice.model.enums.NotificationChannel;
import com.notfound.bookstorenotificationservice.service.MailDeliveryService;
import com.notfound.bookstorenotificationservice.service.NotificationPreferenceService;
import com.notfound.bookstorenotificationservice.util.BookstoreNotificationHtmlBuilder;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.concurrent.Executor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class PromotionEmailDeliveryStrategy implements NotificationDeliveryStrategy {

    private static final Logger logger = LoggerFactory.getLogger(PromotionEmailDeliveryStrategy.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final String EMAIL_CONTEXT = "promotion";

    private final NotificationPreferenceService notificationPreferenceService;
    private final UserContactResolver userContactResolver;
    private final MailDeliveryService mailDeliveryService;
    private final Executor promotionEmailExecutor;
    private final RetryTemplate promotionEmailRetryTemplate;
    private final int batchSize;

    public PromotionEmailDeliveryStrategy(
            NotificationPreferenceService notificationPreferenceService,
            UserContactResolver userContactResolver,
            MailDeliveryService mailDeliveryService,
            Executor promotionEmailExecutor,
            @Qualifier("promotionEmailRetryTemplate") RetryTemplate promotionEmailRetryTemplate,
            @Value("${notification.promotion.batch-size:100}") int batchSize) {
        this.notificationPreferenceService = notificationPreferenceService;
        this.userContactResolver = userContactResolver;
        this.mailDeliveryService = mailDeliveryService;
        this.promotionEmailExecutor = promotionEmailExecutor;
        this.promotionEmailRetryTemplate = promotionEmailRetryTemplate;
        this.batchSize = batchSize > 0 ? batchSize : 100;
    }

    @Override
    public boolean supports(String eventType) {
        return PromotionEventTypes.PROMOTION_CREATED.equals(eventType);
    }

    @Override
    public void deliver(Object event) {
        if (!(event instanceof PromotionCreatedEvent promotionCreatedEvent)) {
            throw new IllegalArgumentException("promotion.created payload must be PromotionCreatedEvent");
        }
        if (!"ACTIVE".equalsIgnoreCase(promotionCreatedEvent.getStatus())) {
            logger.info(
                    "Skip promotion email because promotion is not ACTIVE. promotionId={}, status={}",
                    promotionCreatedEvent.getPromotionId(),
                    promotionCreatedEvent.getStatus());
            return;
        }

        String subject = "Khuyến mãi mới: "
                + (StringUtils.hasText(promotionCreatedEvent.getName())
                        ? promotionCreatedEvent.getName()
                        : promotionCreatedEvent.getCode());
        String inner = BookstoreNotificationHtmlBuilder.buildPromotionAnnouncementBody(
                promotionCreatedEvent.getName(),
                promotionCreatedEvent.getCode(),
                promotionCreatedEvent.getDiscountValue(),
                promotionCreatedEvent.getDescription(),
                formatDate(promotionCreatedEvent.getStartDate()),
                formatDate(promotionCreatedEvent.getEndDate()));
        String html = BookstoreNotificationHtmlBuilder.wrapNotificationEmail(subject, inner);

        try {
            promotionEmailExecutor.execute(() -> dispatchPromotionEmails(promotionCreatedEvent, subject, html));
        } catch (RuntimeException e) {
            logger.warn(
                    "Promotion email executor rejected fan-out job for promotionId={}, running inline: {}",
                    promotionCreatedEvent.getPromotionId(),
                    e.getMessage());
            dispatchPromotionEmails(promotionCreatedEvent, subject, html);
        }

        logger.info(
                "Promotion email fan-out queued. promotionId={}",
                promotionCreatedEvent.getPromotionId());
    }

    private String formatDate(java.time.LocalDate date) {
        return date != null ? DATE_FORMATTER.format(date) : null;
    }

    private void dispatchPromotionEmails(PromotionCreatedEvent promotionCreatedEvent, String subject, String html) {
        int queued = 0;
        Pageable pageable = PageRequest.of(0, batchSize);
        Page<UUID> page;
        do {
            page = notificationPreferenceService.findEnabledUserIds(NotificationChannel.PROMOTION_EMAIL, pageable);
            for (UUID userId : page.getContent()) {
                queued++;
                try {
                    promotionEmailExecutor.execute(
                            () -> sendPromotionEmailWithRetry(userId, promotionCreatedEvent, subject, html));
                } catch (RuntimeException e) {
                    logger.warn(
                            "Promotion email executor rejected user job for userId={} promotionId={}, running inline: {}",
                            userId,
                            promotionCreatedEvent.getPromotionId(),
                            e.getMessage());
                    sendPromotionEmailWithRetry(userId, promotionCreatedEvent, subject, html);
                }
            }
            pageable = page.hasNext() ? page.nextPageable() : pageable;
        } while (page.hasNext());

        logger.info(
                "Promotion email dispatch scheduled. promotionId={}, queuedUsers={}",
                promotionCreatedEvent.getPromotionId(),
                queued);
    }

    private void sendPromotionEmailWithRetry(
            UUID userId,
            PromotionCreatedEvent promotionCreatedEvent,
            String subject,
            String html) {
        promotionEmailRetryTemplate.execute(
                context -> {
                    if (context.getRetryCount() > 0) {
                        logger.info(
                                "Retrying promotion email. userId={}, promotionId={}, attempt={}",
                                userId,
                                promotionCreatedEvent.getPromotionId(),
                                context.getRetryCount() + 1);
                    }
                    sendPromotionEmail(userId, promotionCreatedEvent, subject, html);
                    return null;
                },
                context -> {
                    Throwable lastError = context.getLastThrowable();
                    logger.warn(
                            "Skip promotion email for userId={} promotionId={} after {} attempt(s): {}",
                            userId,
                            promotionCreatedEvent.getPromotionId(),
                            context.getRetryCount(),
                            lastError != null ? lastError.getMessage() : "unknown error");
                    return null;
                });
    }

    private void sendPromotionEmail(UUID userId, PromotionCreatedEvent promotionCreatedEvent, String subject, String html) {
        UserContactInfoResponse contactInfo = userContactResolver.resolveContact(userId);
        String email = contactInfo != null ? contactInfo.getEmail() : null;
        if (!StringUtils.hasText(email)) {
            logger.warn(
                    "Skip promotion email: missing email for userId={} promotionId={}",
                    userId,
                    promotionCreatedEvent.getPromotionId());
            return;
        }
        mailDeliveryService.sendHtmlEmail(email, subject, html, EMAIL_CONTEXT);
    }
}
