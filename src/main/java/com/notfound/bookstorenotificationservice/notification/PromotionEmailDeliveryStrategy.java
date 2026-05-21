package com.notfound.bookstorenotificationservice.notification;

import com.notfound.bookstorenotificationservice.client.UserContactInfoResponse;
import com.notfound.bookstorenotificationservice.client.UserContactResolver;
import com.notfound.bookstorenotificationservice.exception.NotificationDeliveryException;
import com.notfound.bookstorenotificationservice.messaging.PromotionEventTypes;
import com.notfound.bookstorenotificationservice.model.dto.PromotionCreatedEvent;
import com.notfound.bookstorenotificationservice.model.enums.NotificationChannel;
import com.notfound.bookstorenotificationservice.service.MailDeliveryService;
import com.notfound.bookstorenotificationservice.service.NotificationPreferenceService;
import com.notfound.bookstorenotificationservice.util.BookstoreNotificationHtmlBuilder;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
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
    private final int batchSize;

    public PromotionEmailDeliveryStrategy(
            NotificationPreferenceService notificationPreferenceService,
            UserContactResolver userContactResolver,
            MailDeliveryService mailDeliveryService,
            @Value("${notification.promotion.batch-size:100}") int batchSize) {
        this.notificationPreferenceService = notificationPreferenceService;
        this.userContactResolver = userContactResolver;
        this.mailDeliveryService = mailDeliveryService;
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

        int sent = 0;
        int skipped = 0;
        Pageable pageable = PageRequest.of(0, batchSize);
        Page<UUID> page;
        do {
            page = notificationPreferenceService.findEnabledUserIds(NotificationChannel.PROMOTION_EMAIL, pageable);
            for (UUID userId : page.getContent()) {
                try {
                    UserContactInfoResponse contactInfo = userContactResolver.resolveContact(userId);
                    String email = contactInfo != null ? contactInfo.getEmail() : null;
                    if (!StringUtils.hasText(email)) {
                        skipped++;
                        logger.warn("Skip promotion email: missing email for userId={}", userId);
                        continue;
                    }
                    mailDeliveryService.sendHtmlEmail(email, subject, html, EMAIL_CONTEXT);
                    sent++;
                } catch (NotificationDeliveryException e) {
                    throw e;
                } catch (RuntimeException e) {
                    skipped++;
                    logger.warn(
                            "Skip promotion email for userId={} because delivery failed: {}",
                            userId,
                            e.getMessage());
                }
            }
            pageable = page.hasNext() ? page.nextPageable() : pageable;
        } while (page.hasNext());

        logger.info(
                "Promotion email finished. promotionId={}, sent={}, skipped={}",
                promotionCreatedEvent.getPromotionId(),
                sent,
                skipped);
    }

    private String formatDate(java.time.LocalDate date) {
        return date != null ? DATE_FORMATTER.format(date) : null;
    }
}
