package com.notfound.bookstorenotificationservice.specification;

import com.notfound.bookstorenotificationservice.model.entity.NotificationPreference;
import com.notfound.bookstorenotificationservice.model.enums.NotificationChannel;
import org.springframework.data.jpa.domain.Specification;

public final class PromotionEmailEnabledSpecification {

    private PromotionEmailEnabledSpecification() {
    }

    public static Specification<NotificationPreference> promotionEmailEnabled() {
        return enabledChannel(NotificationChannel.PROMOTION_EMAIL);
    }

    public static Specification<NotificationPreference> enabledChannel(NotificationChannel channel) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.and(
                criteriaBuilder.equal(root.get("channel"), channel),
                criteriaBuilder.isTrue(root.get("enabled")));
    }
}

