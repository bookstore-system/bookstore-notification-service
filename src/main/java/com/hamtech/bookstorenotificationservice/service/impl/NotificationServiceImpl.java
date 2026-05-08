package com.hamtech.bookstorenotificationservice.service.impl;

import com.hamtech.bookstorenotificationservice.client.UserContactInfoResponse;
import com.hamtech.bookstorenotificationservice.client.UserServiceClient;
import com.hamtech.bookstorenotificationservice.model.dto.NotificationRequestDto;
import com.hamtech.bookstorenotificationservice.model.dto.OrderEventDto;
import com.hamtech.bookstorenotificationservice.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class NotificationServiceImpl implements NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationServiceImpl.class);
    private final UserServiceClient userServiceClient;

    public NotificationServiceImpl(UserServiceClient userServiceClient) {
        this.userServiceClient = userServiceClient;
    }

    @Override
    public void sendOrderNotification(OrderEventDto orderEvent) {
        logger.info("Processing Order Notification for ID: {}", orderEvent.getOrderId());
        String email = orderEvent.getCustomerEmail();
        String customerName = orderEvent.getCustomerName();

        if ((email == null || email.isBlank()) && orderEvent.getUserId() != null) {
            UserContactInfoResponse contactInfo = userServiceClient.getUserContactInfo(orderEvent.getUserId());
            email = contactInfo.getEmail();
            logger.info("Fetched contact-info from user-service. userId={}, email={}, phoneNumber={}, deviceToken={}",
                    contactInfo.getUserId(),
                    contactInfo.getEmail(),
                    contactInfo.getPhoneNumber(),
                    contactInfo.getDeviceToken());
        }

        logger.info("Email to: {}. Content: Hello {}, your order {} is {}.",
                email,
                customerName,
                orderEvent.getOrderId(),
                orderEvent.getStatus());
    }

    @Override
    public void sendEmailFallback(NotificationRequestDto request) {
        logger.info("Processing Fallback Notification to: {}", request.getTo());
        logger.info("Subject: {}. Message: {}", request.getSubject(), request.getMessage());
    }
}
