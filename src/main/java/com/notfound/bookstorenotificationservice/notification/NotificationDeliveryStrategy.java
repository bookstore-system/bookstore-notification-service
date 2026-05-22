package com.notfound.bookstorenotificationservice.notification;

public interface NotificationDeliveryStrategy {

    boolean supports(String eventType);

    void deliver(Object event);
}

