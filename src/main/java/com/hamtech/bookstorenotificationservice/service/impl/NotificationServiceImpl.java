package com.hamtech.bookstorenotificationservice.service.impl;

import com.hamtech.bookstorenotificationservice.model.dto.NotificationRequestDto;
import com.hamtech.bookstorenotificationservice.model.dto.OrderEventDto;
import com.hamtech.bookstorenotificationservice.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class NotificationServiceImpl implements NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationServiceImpl.class);

    @Override
    public void sendOrderNotification(OrderEventDto orderEvent) {
        logger.info("Processing Order Notification for ID: {}", orderEvent.getOrderId());
        // Logic gửi email thực tế sẽ nằm ở đây
        logger.info("Email to: {}. Content: Hello {}, your order {} is {}.", 
                orderEvent.getCustomerEmail(), 
                orderEvent.getCustomerName(), 
                orderEvent.getOrderId(), 
                orderEvent.getStatus());
    }

    @Override
    public void sendEmailFallback(NotificationRequestDto request) {
        logger.info("Processing Fallback Notification to: {}", request.getTo());
        logger.info("Subject: {}. Message: {}", request.getSubject(), request.getMessage());
    }
}
