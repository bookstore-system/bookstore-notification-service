package com.notfound.bookstorenotificationservice.notification;

import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class NotificationDispatcher {

    private final List<NotificationDeliveryStrategy> strategies;

    public NotificationDispatcher(List<NotificationDeliveryStrategy> strategies) {
        this.strategies = strategies;
    }

    public void deliver(String eventType, Object event) {
        strategies.stream()
                .filter(strategy -> strategy.supports(eventType))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported notification event type: " + eventType))
                .deliver(event);
    }
}

