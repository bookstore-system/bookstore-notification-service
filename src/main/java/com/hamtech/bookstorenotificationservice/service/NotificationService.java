package com.hamtech.bookstorenotificationservice.service;

import com.hamtech.bookstorenotificationservice.model.dto.NotificationRequestDto;
import com.hamtech.bookstorenotificationservice.model.dto.OrderEventDto;

public interface NotificationService {
    void sendOrderNotification(OrderEventDto orderEvent);
    void sendEmailFallback(NotificationRequestDto request);
}
